package com.nasnav.service.cart.optimizers;

import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.dao.AddressRepository;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.AddressesEntity;
import com.nasnav.persistence.AreasEntity;
import com.nasnav.persistence.CitiesEntity;
import com.nasnav.persistence.dto.query.result.CartItemStock;
import com.nasnav.service.CartService;
import com.nasnav.service.OrderService;
import com.nasnav.service.cart.optimizers.parameters.EmptyParams;
import com.nasnav.service.cart.optimizers.parameters.SameCityCartOptimizerParameters;
import com.nasnav.service.model.cart.ShopFulfillingCart;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.nasnav.commons.utils.EntityUtils.noneIsNull;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0010;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0011;
import static com.nasnav.service.cart.optimizers.OptimizationStratigiesNames.SAME_CITY;
import static com.nasnav.shipping.services.PickupFromShop.SHOP_ID;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;
import static java.util.Comparator.reverseOrder;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;




@Service(SAME_CITY)
public class SameCityCartOptimizer implements CartOptimizer<SameCityCartOptimizerParameters, EmptyParams> {
	
	private Logger logger = LogManager.getLogger();
	
	@Autowired
	private OrderService orderService;

	@Autowired
	private CartService cartService;
	
	@Autowired
	private AddressRepository addressRepo;
	
	
	@Override
	public Optional<OptimizedCart> createOptimizedCart(Optional<SameCityCartOptimizerParameters> parameters, EmptyParams config,  Cart cart ) {
		Long customerCityId = getCustomerCityId(parameters);
		
		Optional<Long> givenShopId = parameters.map(SameCityCartOptimizerParameters::getShopId);
		
		List<ShopFulfillingCart> shopsOrderedByPriority = getShopsOrderedByPriority(customerCityId, givenShopId);
		
		logger.info(
				format("Optimizing cart using parameters [%s], selecting shops by the priority list : [%s]"
						, parameters.toString(),  shopsOrderedByPriority.toString()));
		
		return cart
				.getItems()
				.stream()
				.map(item -> createOptimizedCartItem(item, shopsOrderedByPriority))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(collectingAndThen(
							toList()
							, items -> Optional.of(new OptimizedCart(items))));
	}


	
	private Long getCustomerCityId(Optional<SameCityCartOptimizerParameters> parameters) {
		return parameters
				.map(SameCityCartOptimizerParameters::getCustomerAddressId)
				.flatMap(this::getAddressCityId)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0010));
	}


	private List<ShopFulfillingCart> getShopsOrderedByPriority(Long customerCityId, Optional<Long> givenShopId) {
		return cartService
				.getShopsThatCanProvideCartItems()
				.stream()
				.sorted(createShopPriorityComparator(givenShopId, customerCityId))
				.collect(toList());
	}


	private Optional<OptimizedCartItem> createOptimizedCartItem(CartItem item, List<ShopFulfillingCart> shopsOrderedByPriority) {
		Optional<OptimizedCartItem> optimizedItem = 
				getCartItemStockFromHighestPriorityShop(item, shopsOrderedByPriority)
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
			return new OptimizedCartItem(item, false, false);
		}
		BigDecimal itemPrice = ofNullable(item.getPrice()).orElse(ZERO);
		BigDecimal itemDiscount = ofNullable(item.getDiscount()).orElse(ZERO);
		BigDecimal stkPrice = ofNullable(itemStk.getStockPrice()).orElse(ZERO);
		BigDecimal stkDiscount = ofNullable(itemStk.getDiscount()).orElse(ZERO);
		BigDecimal weight = ofNullable(item.getWeight()).orElse(ZERO);
		boolean priceChanged = 
				itemPrice.compareTo(stkPrice) != 0 
					|| itemDiscount.compareTo(stkDiscount) != 0;
		boolean itemChanged = !item.getStockId().equals(itemStk.getStockId());
		optimized.setPrice(itemStk.getStockPrice());
		optimized.setStockId(itemStk.getStockId());
		optimized.setDiscount(itemStk.getDiscount());
		optimized.setWeight(weight);
		return new OptimizedCartItem(optimized, priceChanged, itemChanged);
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
	public Class<? extends SameCityCartOptimizerParameters> getCartParametersClass() {
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





	@Override
	public Optional<SameCityCartOptimizerParameters> createCartOptimizationParameters(CartCheckoutDTO checkoutDto) {
		SameCityCartOptimizerParameters params = 
				new SameCityCartOptimizerParameters();
		
		params.setCustomerAddressId(checkoutDto.getAddressId());
		
		ofNullable(checkoutDto)
		.map(CartCheckoutDTO::getAdditionalData)
		.map(data -> data.get(SHOP_ID))
		.flatMap(EntityUtils::parseLongSafely)
		.ifPresent(shopId -> params.setShopId(shopId));
		
		return Optional.of(params);
	}





	@Override
	public Boolean areCartParametersValid(SameCityCartOptimizerParameters parameters) {
		return noneIsNull(parameters
				, parameters.getCustomerAddressId()
				, parameters.getShopId());
	}



	@Override
	public String getOptimizerName() {
		return SAME_CITY;
	}


	@Override
	public Class<? extends EmptyParams> getConfigurationClass() {
		return EmptyParams.class;
	}





	@Override
	public Boolean isConfigValid(EmptyParams parameters) {
		return true;
	}
}



