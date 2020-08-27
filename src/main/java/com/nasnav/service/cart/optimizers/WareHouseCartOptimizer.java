package com.nasnav.service.cart.optimizers;

import static com.nasnav.commons.utils.EntityUtils.firstExistingValueOf;
import static com.nasnav.commons.utils.EntityUtils.noneIsNull;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0011;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0013;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0014;
import static com.nasnav.service.cart.optimizers.OptimizationStratigiesNames.WAREHOUSE;
import static java.math.BigDecimal.ZERO;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.OrganizationCartOptimizationRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.OrganizationCartOptimizationEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.service.SecurityService;
import com.nasnav.service.cart.optimizers.parameters.WarehouseOptimizerCommonParameters;
import com.nasnav.service.cart.optimizers.parameters.WarhouseOptimizerCartParameters;


@Service(WAREHOUSE)
public class WareHouseCartOptimizer implements CartOptimizer<WarhouseOptimizerCartParameters, WarehouseOptimizerCommonParameters> {
	private Logger logger = LogManager.getLogger();
	
	
	@Autowired
	private OrganizationCartOptimizationRepository orgCartOptimizationRepo;
	
	
	@Autowired
	private SecurityService securityService;
	
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private StockRepository stockRepo;
	
	@Override
	public Optional<WarhouseOptimizerCartParameters> createCartOptimizationParameters(CartCheckoutDTO dto) {
		WarhouseOptimizerCartParameters params = new WarhouseOptimizerCartParameters();
		params.setShippingServiceId(dto.getServiceId());
		return Optional.of(params);
	}

	
	
	
	
	@Override
	public Optional<OptimizedCart> createOptimizedCart(Optional<WarhouseOptimizerCartParameters> parameters, Cart cart) {
		OrganizationCartOptimizationEntity optimizationSettings = getOptimizationSettingsEntity(parameters);
		WarehouseOptimizerCommonParameters commonParams = parseCommonParametersJson(optimizationSettings);
		validateCommonParams(commonParams);
		
		Long warehouseId = commonParams.getWarehouseId();
		Map<Long, StocksEntity> stocks = getCartItemsStockInWarehouse(cart, warehouseId);
		
		return cart
				.getItems()
				.stream()
				.map(item -> createOptimizedCartItem(item, stocks))
				.collect(collectingAndThen(
						toList()
						, items -> Optional.of(new OptimizedCart(items))));
	}





	private void validateCommonParams(WarehouseOptimizerCommonParameters commonParams) {
		if(!areCommonParametersValid(commonParams)) {
			Long orgId = securityService.getCurrentUserOrganizationId();
			throw new RuntimeBusinessException( 
					INTERNAL_SERVER_ERROR, O$CRT$0013, orgId, WAREHOUSE);
		};
	}





	private Map<Long, StocksEntity> getCartItemsStockInWarehouse(Cart cart, Long warehouseId) {
		List<Long> variantIds = 
				cart
				.getItems()
				.stream()
				.map(CartItem::getVariantId)
				.collect(toList());
		return stockRepo
				.findByProductVariantsEntity_IdInAndShopsEntity_Id(variantIds, warehouseId)
				.stream()
				.collect(
						toMap(stock -> stock.getProductVariantsEntity().getId()
								, stock -> stock));
	}


	
	
	private OptimizedCartItem createOptimizedCartItem(CartItem item, Map<Long,StocksEntity> stocks) {
		CartItem optimized = new CartItem();
		try {
			BeanUtils.copyProperties(optimized, item);			
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.error(e,e);
			return new OptimizedCartItem(item, false);
		}
		StocksEntity stock = 
				ofNullable(item.getVariantId())
				.map(stocks::get)
				.filter(stk -> canFulfillRequiredQuantitiy(stk, item))
				.orElseThrow(
						() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0011, item.getId(), item.getStockId()));
		
		boolean priceChanged = isPriceChanged(item, stock);
		optimized.setPrice(stock.getPrice());
		optimized.setStockId(stock.getId());
		optimized.setDiscount(stock.getDiscount());
		return new OptimizedCartItem(optimized, priceChanged);
	}





	private boolean isPriceChanged(CartItem item, StocksEntity stock) {
		BigDecimal itemPrice = ofNullable(item.getPrice()).orElse(ZERO);
		BigDecimal itemDiscount = ofNullable(item.getDiscount()).orElse(ZERO);
		BigDecimal stkPrice = ofNullable(stock.getPrice()).orElse(ZERO);
		BigDecimal stkDiscount = ofNullable(stock.getDiscount()).orElse(ZERO);
		
		boolean priceChanged = 
				itemPrice.compareTo(stkPrice) != 0 
					|| itemDiscount.compareTo(stkDiscount) != 0;
		return priceChanged;
	}



	
	
	private boolean canFulfillRequiredQuantitiy(StocksEntity stock, CartItem item) {
		return stock.getQuantity() >= item.getQuantity();
	}





	private WarehouseOptimizerCommonParameters parseCommonParametersJson(
			OrganizationCartOptimizationEntity optimizationParams) {
		WarehouseOptimizerCommonParameters commonParams;
		try{
			commonParams = 
					objectMapper.readValue(optimizationParams.getParameters(), getCommonParametersClass());
		}catch(Throwable e) {
			Long orgId = securityService.getCurrentUserOrganizationId();
			logger.error(e,e);
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, O$CRT$0014, orgId, WAREHOUSE);
		}
		return commonParams;
	}





	private OrganizationCartOptimizationEntity getOptimizationSettingsEntity(
			Optional<WarhouseOptimizerCartParameters> parameters) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		Optional<OrganizationCartOptimizationEntity> shippingServiceOptimizationParams = 
				parameters
				.map(WarhouseOptimizerCartParameters::getShippingServiceId)
				.flatMap(this::getOptimizationParamsForShippingService);

		Optional<OrganizationCartOptimizationEntity> orgOptimizationParams = 
				orgCartOptimizationRepo
				.findByOptimizationStrategyAndOrganization_Id(WAREHOUSE, orgId);
		
		return firstExistingValueOf(
						shippingServiceOptimizationParams
						,orgOptimizationParams)
				.orElseThrow(() -> new RuntimeBusinessException( 
						INTERNAL_SERVER_ERROR, O$CRT$0013, orgId, WAREHOUSE));
	}
	
	
	
	
	private Optional<OrganizationCartOptimizationEntity> getOptimizationParamsForShippingService(String shippingServiceId){
		Long orgId = securityService.getCurrentUserOrganizationId();
		return orgCartOptimizationRepo
				.findByOptimizationStrategyAndShippingServiceIdAndOrganization_Id(
						WAREHOUSE, shippingServiceId, orgId);
	}
	
	
	
	

	@Override
	public Class<? extends WarhouseOptimizerCartParameters> getCartParametersClass() {
		return WarhouseOptimizerCartParameters.class;
	}

	
	
	
	@Override
	public Class<? extends WarehouseOptimizerCommonParameters> getCommonParametersClass() {
		return WarehouseOptimizerCommonParameters.class;
	}

	
	
	@Override
	public Boolean areCommonParametersValid(WarehouseOptimizerCommonParameters parameters) {
		return noneIsNull(parameters, parameters.getWarehouseId());
	}

	
	
	@Override
	public Boolean areCartParametersValid(WarhouseOptimizerCartParameters parameters) {
		return noneIsNull(parameters, parameters.getShippingServiceId());
	}

}
