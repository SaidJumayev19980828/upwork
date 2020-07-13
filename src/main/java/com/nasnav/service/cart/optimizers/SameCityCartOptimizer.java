package com.nasnav.service.cart.optimizers;

import static com.nasnav.exceptions.ErrorCodes.O$CRT$0010;
import static com.nasnav.service.cart.optimizers.OptimizationStratigiesNames.SAME_CITY;
import static java.util.Arrays.asList;
import static java.util.Comparator.reverseOrder;
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
import com.nasnav.dto.response.navbox.Cart;
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
	public Optional<Cart> createOptimizedCart(Optional<SameCityCartOptimizerParameters> parameters) {
		
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
							, items -> Optional.of(new Cart(items))));
	}

	
	
	
	
	private Optional<CartItem> createOptimizedCartItem(CartItem item, List<ShopFulfillingCart> shopsOrderdByPriority) {
		CartItem optimized = new CartItem();
		try {
			BeanUtils.copyProperties(optimized, item);			
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.error(e,e);
			return Optional.of(item);
		}
		
		getItemFromHighestPriorityShop(item, shopsOrderdByPriority)
		.ifPresent(optimized::setStockId);
		
		return Optional.of(optimized);
	}





	private Optional<Long> getItemFromHighestPriorityShop(CartItem item,
			List<ShopFulfillingCart> shopsOrderdByPriority) {
		return shopsOrderdByPriority
				.stream()
				.map(shop -> getItemStockInShop(shop, item))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst();
	}
	
	
	
	
	
	private Optional<Long> getItemStockInShop(ShopFulfillingCart shop, CartItem item) {
		return shop
				.getCartItems()
				.stream()
				.filter(itemInShop -> Objects.equals(itemInShop.getVariantId(), item.getVariantId()))
				.map(CartItemStock::getStockId)
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
				.map(AddressesEntity::getAreasEntity)
				.map(AreasEntity::getCitiesEntity)
				.map(CitiesEntity::getId)
				.findFirst();
	}
}



