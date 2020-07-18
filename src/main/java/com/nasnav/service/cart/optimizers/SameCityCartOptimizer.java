package com.nasnav.service.cart.optimizers;

import static com.nasnav.exceptions.ErrorCodes.O$CRT$0010;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0011;
import static com.nasnav.service.cart.optimizers.OptimizationStratigiesNames.SAME_CITY;
import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;
import static java.util.Comparator.reverseOrder;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nasnav.dao.AddressRepository;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.AddressesEntity;
import com.nasnav.persistence.AreasEntity;
import com.nasnav.persistence.CitiesEntity;
import com.nasnav.persistence.dto.query.result.CartItemStock;
import com.nasnav.service.OrderService;
import com.nasnav.service.cart.optimizers.parameters.SameCityCartOptimizerParameters;
import com.nasnav.service.model.cart.ShopFulfillingCart;




@Service(SAME_CITY)
public class SameCityCartOptimizer implements CartOptimizer<SameCityCartOptimizerParameters> {
	
	private Logger logger = LogManager.getLogger();
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private AddressRepository addressRepo;
	
	
	@Override
	public Optional<OptimizedCart> createOptimizedCart(Optional<SameCityCartOptimizerParameters> parameters) {
		
		Long customerCityId = 
				parameters
				.map(SameCityCartOptimizerParameters::getCustomerAddressId)
				.flatMap(this::getAddressCityId)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0010));
		
		Optional<Long> givenShopId = parameters.map(SameCityCartOptimizerParameters::getShopId);
		
		List<ShopFulfillingCart> shopsOrderdByPriority = 
				orderService
				.getShopsThatCanProvideCartItems() 
				.stream()
				.sorted(createShopPriorityComparator(givenShopId, customerCityId))
				.collect(toList());
		
		return orderService
				.getCart()
				.getItems()
				.stream()
				.map(item -> createOptimizedCartItem(item, shopsOrderdByPriority))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(collectingAndThen(
							toList()
							, items -> Optional.of(new OptimizedCart(items))));
	}

	
	
	
	
	private Optional<OptimizedCartItem> createOptimizedCartItem(CartItem item, List<ShopFulfillingCart> shopsOrderdByPriority) {
		Optional<OptimizedCartItem> optimizedItem = 
				getCartItemStockFromHighestPriorityShop(item, shopsOrderdByPriority)
				.map(itemStk -> createOptimizedCartItem(itemStk, item));
		if(!optimizedItem.isPresent()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0011, item.getId(), item.getStockId());
		}
		return optimizedItem;
	}


	
	
	private OptimizedCartItem createOptimizedCartItem(CartItemStock itemStk, CartItem item) {
		CartItem optimized = new CartItem();
		try {
			BeanUtils.copyProperties(optimized, item);			
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.error(e,e);
			return new OptimizedCartItem(item, false);
		}
		boolean priceChanged = 
				ofNullable(item.getPrice())
				.orElse(ZERO)
				.compareTo(itemStk.getStockPrice()) != 0;
		optimized.setPrice(itemStk.getStockPrice());
		optimized.setStockId(itemStk.getStockId());
		return new OptimizedCartItem(optimized, priceChanged);
	}



	private Optional<CartItemStock> getCartItemStockFromHighestPriorityShop(CartItem item,
			List<ShopFulfillingCart> shopsOrderdByPriority) {
		return shopsOrderdByPriority
				.stream()
				.map(shop -> getCartItemStockInShop(shop, item))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst();
	}
	
	
	
	
	
	private Optional<CartItemStock> getCartItemStockInShop(ShopFulfillingCart shop, CartItem item) {
		return shop
				.getCartItems()
				.stream()
				.filter(itemInShop -> Objects.equals(itemInShop.getVariantId(), item.getVariantId()))
				.findFirst();
	}
	
	
	
	
	
	private Comparator<ShopFulfillingCart> createShopPriorityComparator(
			 Optional<Long> givenShopId, Long customerCityId){
		return Comparator
				.<ShopFulfillingCart>comparingInt(shop -> {return givenShopTakesTopPriority(shop, givenShopId);})
				.thenComparing(shop -> {return shopInTheCustomerCityTakesPriority(shop, customerCityId);})
				.thenComparing(this::getNumberOfItemsProvidedByShop, reverseOrder())
				.thenComparing(this::getAverageAvailableStockForItems, reverseOrder());
	}
	
	
	
	
	private Double getAverageAvailableStockForItems(ShopFulfillingCart shop) {
		return shop
				.getCartItems()
				.stream()
				.collect(averagingDouble(CartItemStock::getStockQuantity));
	}
	
	
	
	
	private int getNumberOfItemsProvidedByShop(ShopFulfillingCart shop) {
		return shop.getCartItems().size();
	}
	
	
	
	
	
	private int givenShopTakesTopPriority(ShopFulfillingCart shop, Optional<Long> givenShopId) {
		return givenShopId
				.map(id -> Objects.equals(id, shop.getShopId()) ? Integer.MIN_VALUE : 0)
				.orElse(0);
	}
	
	
	
	
	private int shopInTheCustomerCityTakesPriority(ShopFulfillingCart shop, Long customerCityId) {
		return Objects.equals(shop.getShopCityId(), customerCityId)? 0 : 1; 
	}
	

	
	
	@Override
	public Class<? extends SameCityCartOptimizerParameters> getParameterClass() {
		return SameCityCartOptimizerParameters.class;
	}

	
	
	
	
	private Optional<Long> getAddressCityId(Long addressId) {
		return addressRepo
				.findByIdIn(asList(addressId))
				.stream()
				.findFirst()
				.map(AddressesEntity::getAreasEntity)
				.map(AreasEntity::getCitiesEntity)
				.map(CitiesEntity::getId);
	}
}



