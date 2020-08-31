package com.nasnav.service;

import static com.nasnav.commons.utils.EntityUtils.firstExistingValueOf;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.exceptions.ErrorCodes.G$PRAM$0001;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0006;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0007;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0009;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0012;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0014;
import static com.nasnav.service.cart.optimizers.CartOptimizationStrategy.DEFAULT_OPTIMIZER;
import static com.nasnav.service.cart.optimizers.CartOptimizationStrategy.isValidStrategy;
import static com.nasnav.service.cart.optimizers.OptimizationStratigiesNames.WAREHOUSE;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.OrganizationCartOptimizationRepository;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.organization.CartOptimizationSettingDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartOptimizeResponseDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.OrganizationCartOptimizationEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.service.cart.optimizers.CartOptimizationStrategy;
import com.nasnav.service.cart.optimizers.CartOptimizer;
import com.nasnav.service.cart.optimizers.OptimizedCart;
import com.nasnav.service.cart.optimizers.OptimizedCartItem;


@Service
public class CartOptimizationServiceImpl implements CartOptimizationService {
	
	private Logger logger = LogManager.getLogger();
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private OrganizationCartOptimizationRepository orgCartOptimizerRepo;
	
	
	
	@Override
	@Transactional(rollbackFor = Throwable.class)
	public CartOptimizeResponseDTO optimizeCart(CartCheckoutDTO dto) {
		Optional<OptimizedCart> optimizedCart = createOptimizedCart(dto);
		boolean anyPriceChanged = isAnyItemPriceChangedAfterOptimization(optimizedCart);
		Cart returnedCart = getCartObject(optimizedCart);
		return new CartOptimizeResponseDTO(anyPriceChanged, returnedCart);
	}





	private Cart getCartObject(Optional<OptimizedCart> optimizedCart) {
		return optimizedCart
				.map(OptimizedCart::getCartItems)
				.orElse(emptyList())
				.stream()
				.map(OptimizedCartItem::getCartItem)
				.collect(collectingAndThen(toList(), Cart::new));
	}





	private boolean isAnyItemPriceChangedAfterOptimization(Optional<OptimizedCart> optimizedCart) {
		return optimizedCart
				.map(OptimizedCart::getCartItems)
				.orElse(emptyList())
				.stream()
				.map(OptimizedCartItem::getPriceChanged)
				.anyMatch(isPriceChanged -> isPriceChanged);
	}




	
	private <T,P> Optional<OptimizedCart> createOptimizedCart(CartCheckoutDTO dto) {
		CartOptimizationStrategy strategy = getOptimizationStrategyForCart(dto);
	
		CartOptimizer<T,P> optimizer = getCartOptimizer(strategy.getValue());
		Optional<T> parameters = optimizer.createCartOptimizationParameters(dto);
		Cart cart = orderService.getCart();
		
		return optimizer.createOptimizedCart(parameters, cart);
		
	}




	@SuppressWarnings("unchecked")
	private <T,P> CartOptimizer<T,P> getCartOptimizer(String strategy){
		try {
			return context.getBean(strategy, CartOptimizer.class);
		}catch(Throwable t) {
			logger.error(t,t);
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, O$CRT$0009, strategy);
		}
	}





	private CartOptimizationStrategy getOptimizationStrategyForCart(CartCheckoutDTO dto) {
		String shippingServiceId = dto.getServiceId();
		
		Optional<String> shippingServiceOptimizer = 
				getCartOptimizationStrategyForShippingService(shippingServiceId);
		Optional<String> organizationCartOptimizer = 
				getCartOptimizationStrategyForOrganization();
		Optional<String> defaultOptimizer = Optional.of(DEFAULT_OPTIMIZER.getValue());
		
		return firstExistingValueOf(
				shippingServiceOptimizer
				, organizationCartOptimizer 
				, defaultOptimizer)
				.map(this::validateCartOptimizationStrategy)
				.flatMap(CartOptimizationStrategy::getCartOptimizationStrategy)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0006));
	}
	
	
	
	
	private String validateCartOptimizationStrategy(String strategy) {
		if(isBlankOrNull(strategy)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0006);
		}else if(!isValidStrategy(strategy)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0007, strategy);
		}
		return strategy;
	}
	
	
	
	@Override
	public Optional<String> getCartOptimizationStrategyForOrganization(){
		Long orgId = securityService.getCurrentUserOrganizationId();
		return orgCartOptimizerRepo
				.findOrganizationDefaultOptimizationStrategy(orgId);
	}
	
	
	
	
	public Optional<String> getCartOptimizationStrategyForShippingService(String shippingServiceId){
		Long orgId = securityService.getCurrentUserOrganizationId();
		return orgCartOptimizerRepo
				.findByShippingServiceIdAndOrganization_Id(shippingServiceId, orgId)
				.map(OrganizationCartOptimizationEntity::getOptimizationStrategy);
	}
	
	
	
	
	

	@Override
	public <T> void  setCartOptimizationStrategy(CartOptimizationSettingDTO settingDto) {
		String strategy = settingDto.getStrategyName(); 
		if(isBlankOrNull(strategy)|| !isValidStrategy(strategy)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$PRAM$0001, settingDto.toString());
		}
		
		Map<String,Object> parameters = ofNullable(settingDto.getParameters()).orElse(emptyMap());
		validateCommonParametersJson(strategy, parameters);
		
		persistCartOptimizationInfo(settingDto, strategy, parameters);
	}





	private void persistCartOptimizationInfo(CartOptimizationSettingDTO settingDto, String strategy,
			Map<String,Object> parameters) {
		String shippingServiceId = settingDto.getShippingServiceId();
		OrganizationEntity org = securityService.getCurrentUserOrganization();
		String parametersJsonStr;
		try {
			parametersJsonStr = objectMapper.writeValueAsString(parameters);
		} catch (JsonProcessingException e) {
			Long orgId = securityService.getCurrentUserOrganizationId();
			logger.error(e,e);
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0014, orgId, WAREHOUSE);
		}
		
		OrganizationCartOptimizationEntity parametersEntity = 
				orgCartOptimizerRepo
				.findByOptimizationStrategyAndShippingServiceIdAndOrganization_Id(strategy, shippingServiceId, org.getId())
				.orElseGet(() -> getOrganizationCartOptimizationParameters(settingDto));
		
		parametersEntity.setOptimizationStrategy(strategy);
		parametersEntity.setOrganization(org);
		parametersEntity.setParameters(parametersJsonStr);
		parametersEntity.setShippingServiceId(shippingServiceId);
		
		orgCartOptimizerRepo.save(parametersEntity);
	}





	private <T,P> void validateCommonParametersJson(String strategy, Map<String,Object> parameters) {
		try {
			CartOptimizer<T,P> optimizer = getCartOptimizer(strategy);
			P parametersObj = objectMapper.convertValue(parameters, optimizer.getCommonParametersClass());
			if(!optimizer.areCommonParametersValid(parametersObj)) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0012, parameters.toString());
			};
		} catch (Throwable e) {
			logger.error(e,e);
			Long orgId = securityService.getCurrentUserOrganizationId();
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0014, orgId, WAREHOUSE);
		}
	}





	private OrganizationCartOptimizationEntity getOrganizationCartOptimizationParameters(CartOptimizationSettingDTO settingDto) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		String strategy = settingDto.getStrategyName(); 
		return orgCartOptimizerRepo
				.findByOptimizationStrategyAndOrganization_Id(strategy, orgId)
				.orElse(new OrganizationCartOptimizationEntity());
	}





	@Override
	public List<CartOptimizationSettingDTO> getCartOptimizationStrategy() {
		Long orgId = securityService.getCurrentUserOrganizationId();
		return orgCartOptimizerRepo
				.findByOrganization_Id(orgId)
				.stream()
				.map(this::createSettingDTO)
				.collect(toList());
	}
	

	
	
	
	private CartOptimizationSettingDTO createSettingDTO(OrganizationCartOptimizationEntity entity) {
		String parametersJson = ofNullable(entity.getParameters()).orElse("{}");
		Map<String,Object> params = emptyMap();
		try {
			 params = objectMapper.readValue(parametersJson, new TypeReference<Map<String,Object>>(){});
		} catch (IOException e) {
			logger.error(e,e);
		}
		
		CartOptimizationSettingDTO settingDto = new CartOptimizationSettingDTO();
		settingDto.setShippingServiceId(entity.getShippingServiceId());
		settingDto.setStrategyName(entity.getOptimizationStrategy());
		settingDto.setParameters(params);
		return settingDto;
	}

}
