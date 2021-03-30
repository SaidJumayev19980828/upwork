package com.nasnav.service.cart.optimizers;

import static com.nasnav.commons.utils.EntityUtils.firstExistingValueOf;
import static com.nasnav.commons.utils.EntityUtils.noneIsNull;
import static com.nasnav.service.cart.optimizers.OptimizationStratigiesNames.WAREHOUSE;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.OrganizationCartOptimizationRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.service.SecurityService;
import com.nasnav.service.cart.optimizers.parameters.WarehouseOptimizerConfig;
import com.nasnav.service.cart.optimizers.parameters.WarehouseOptimizerCartParameters;


@Service(WAREHOUSE)
public class WareHouseCartOptimizer implements CartOptimizer<WarehouseOptimizerCartParameters, WarehouseOptimizerConfig> {
	private Logger logger = LogManager.getLogger();
	
	
	@Autowired
	private OrganizationCartOptimizationRepository orgCartOptimizationRepo;
	
	@Autowired
	private SecurityService securityService;

	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private StockRepository stockRepo;

	@Autowired
	private ShopsRepository shopRepo;

	@Autowired
	private CartOptimizationHelper helper;



	@Override
	public Optional<WarehouseOptimizerCartParameters> createCartOptimizationParameters(CartCheckoutDTO dto) {
		WarehouseOptimizerCartParameters params = new WarehouseOptimizerCartParameters();
		params.setShippingServiceId(dto.getServiceId());
		return Optional.of(params);
	}


	
	@Override
	public Optional<OptimizedCart> createOptimizedCart(Optional<WarehouseOptimizerCartParameters> parameters, WarehouseOptimizerConfig config, Cart cart) {
		Long warehouseId = config.getWarehouseId();
		Map<Long, StocksEntity> stocks = helper.getCartItemsStockInShop(cart, warehouseId);
		
		return helper.createOptimizedCart(cart, stocks);
	}



	@Override
	public Class<? extends WarehouseOptimizerCartParameters> getCartParametersClass() {
		return WarehouseOptimizerCartParameters.class;
	}


	
	@Override
	public Class<? extends WarehouseOptimizerConfig> getConfigurationClass() {
		return WarehouseOptimizerConfig.class;
	}

	

	@Override
	public Boolean isConfigValid(WarehouseOptimizerConfig config) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		Long warehouseId = 
				ofNullable(config)
				.map(WarehouseOptimizerConfig::getWarehouseId)
				.orElse(-1L);
		ShopsEntity shop = 
				shopRepo
				.findByIdAndOrganizationEntity_IdAndRemoved(warehouseId, orgId, 0);
		return nonNull(shop);
	}


	
	@Override
	public Boolean areCartParametersValid(WarehouseOptimizerCartParameters parameters) {
		return noneIsNull(parameters, parameters.getShippingServiceId());
	}



	@Override
	public String getOptimizerName() {
		return WAREHOUSE;
	}

}
