package com.nasnav.service;

import static com.nasnav.commons.utils.EntityUtils.firstExistingValueOf;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.exceptions.ErrorCodes.G$PRAM$0001;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0006;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0007;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0008;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0009;
import static com.nasnav.exceptions.ErrorCodes.O$CRT$0012;
import static com.nasnav.service.cart.optimizers.CartOptimizationStrategy.DEFAULT_OPTIMIZER;
import static com.nasnav.service.cart.optimizers.CartOptimizationStrategy.isValidStrategy;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
		
		try {
			CartOptimizer<T,P> optimizer = getCartOptimizer(strategy.getValue());
			Optional<T> parameters = optimizer.createCartOptimizationParameters(dto);
			Cart cart = orderService.getCart();
			
			return optimizer.createOptimizedCart(parameters, cart);
		}catch(Throwable t) {
			logger.error(t,t);
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0009, strategy.getValue());
		}
	}




	@SuppressWarnings("unchecked")
	private <T,P> CartOptimizer<T,P> getCartOptimizer(String strategy) throws Throwable{
		return context.getBean(strategy, CartOptimizer.class);
	}





	private CartOptimizationStrategy getOptimizationStrategyForCart(CartCheckoutDTO dto) {
		String shippingServiceId = dto.getServiceId();
		
		Optional<String> shippingServiceOptimizer = 
				getCartOptimizationStrategyForShippingService(shippingServiceId);
		Optional<String> organizationCartOptimizer = 
				getCartOptimizationStrategyForOrganization();
		Optional<String> defaultOptimizer = Optional.of(DEFAULT_OPTIMIZER.name());
		
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
		
		String parameters = ofNullable(settingDto.getParameters()).orElse("{}");
		validateCommonParametersJson(strategy, parameters);
		
		persistCartOptimizationInfo(settingDto, strategy, parameters);
	}





	private void persistCartOptimizationInfo(CartOptimizationSettingDTO settingDto, String strategy,
			String parameters) {
		String shippingServiceId = settingDto.getShippingServiceId();
		OrganizationEntity org = securityService.getCurrentUserOrganization();
		OrganizationCartOptimizationEntity parametersEntity = 
				orgCartOptimizerRepo
				.findByOptimizationStrategyAndShippingServiceIdAndOrganization_Id(strategy, shippingServiceId, org.getId())
				.orElseGet(() -> getOrganizationCartOptimizationParameters(settingDto));
		
		parametersEntity.setOptimizationStrategy(strategy);
		parametersEntity.setOrganization(org);
		parametersEntity.setParameters(parameters);
		parametersEntity.setShippingServiceId(shippingServiceId);
	}





	private <T,P> void validateCommonParametersJson(String strategy, String parameters) {
		try {
			CartOptimizer<T,P> optimizer = getCartOptimizer(strategy);
			P parametersObj = objectMapper.readValue(parameters, optimizer.getCommonParametersClass());
			if(!optimizer.areCommonParametersValid(parametersObj)) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0012, parameters.toString());
			};
		} catch (Throwable e) {
			logger.error(e,e);
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0008, parameters.toString());
		}
	}





	private OrganizationCartOptimizationEntity getOrganizationCartOptimizationParameters(CartOptimizationSettingDTO settingDto) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		String strategy = settingDto.getStrategyName(); 
		return orgCartOptimizerRepo
				.findByOptimizationStrategyAndOrganization_Id(strategy, orgId)
				.orElse(new OrganizationCartOptimizationEntity());
	}
	


}
