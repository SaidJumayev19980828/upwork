package com.nasnav.service.cart.optimizers;

import static com.nasnav.exceptions.ErrorCodes.O$CRT$0010;
import static com.nasnav.service.cart.optimizers.OptimizationStratigiesNames.SAME_CITY;
import static java.util.Arrays.asList;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.google.common.collect.Comparators;
import com.nasnav.dao.AddressRepository;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.AddressesEntity;
import com.nasnav.persistence.AreasEntity;
import com.nasnav.persistence.CitiesEntity;
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
		
		ShopFulfillingCart selecteShop = 
				orderService
				.getShopsThatCanProvideWholeCart()
				.stream()
				.filter(shop -> Objects.equals(shop.getShopCityId(), customerCityId))
				.sorted(Comparator.comparing(keyExtractor))
		
		return null;
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
