package com.nasnav.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.OrganizationCartOptimizationRepository;
import com.nasnav.dao.UserAddressRepository;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.organization.CartOptimizationSettingDTO;
import com.nasnav.dto.response.CartOptimizationStrategyDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartOptimizeResponseDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.AddressesEntity;
import com.nasnav.persistence.OrganizationCartOptimizationEntity;
import com.nasnav.persistence.UserAddressEntity;
import com.nasnav.service.cart.optimizers.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.nasnav.commons.utils.EntityUtils.firstExistingValueOf;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.service.cart.optimizers.CartOptimizationStrategy.DEFAULT_OPTIMIZER;
import static com.nasnav.service.cart.optimizers.CartOptimizationStrategy.isValidStrategy;
import static com.nasnav.service.cart.optimizers.OptimizationStratigiesNames.WAREHOUSE;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;


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
	private CartService cartService;
	
	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private OrganizationCartOptimizationRepository orgCartOptimizerRepo;

	@Autowired
	private CartOptimizationHelper helper;

	@Autowired
	private UserAddressRepository userAddressRepo;
	
	
	
	@Override
	@Transactional(rollbackFor = Throwable.class)
	public CartOptimizeResponseDTO optimizeCart(CartCheckoutDTO dto) {
		if (dto.getAddressId() == null) {
			Long addressId = userAddressRepo
					.findByUser_Id(securityService.getCurrentUser().getId())
					.stream()
					.findFirst()
					.map(UserAddressEntity::getAddress)
					.map(AddressesEntity::getId)
					.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE,O$RET$0019)); 
			dto.setAddressId(addressId);
		}
		var optimizedCart = createOptimizedCart(dto);
		var anyPriceChanged = isAnyItemPriceChangedAfterOptimization(optimizedCart);
		var returnedCart = getCartObject(optimizedCart);
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
				.anyMatch(OptimizedCartItem::getPriceChanged);
	}


	
	private <T, Config> Optional<OptimizedCart> createOptimizedCart(CartCheckoutDTO dto) {
		var optimizerData = getOptimizerData(dto);
		var strategy = optimizerData.getStrategy();

		CartOptimizer<T, Config> optimizer = getCartOptimizer(strategy.getValue());

		var config = helper.getOptimizerConfig(optimizerData.getConfigurationJson(), optimizer);
		var parameters = optimizer.createCartOptimizationParameters(dto);
		var cart = cartService.getCart();
		
		return optimizer.createOptimizedCart(parameters, config, cart);
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



	private Optimizer getOptimizerData(CartCheckoutDTO dto) {
		var shippingServiceId = dto.getServiceId();

		var shippingServiceOptimizer =
				getCartOptimizationStrategyForShippingService(shippingServiceId);
		var organizationCartOptimizer =
				getCartOptimizationStrategyForOrganization();
		var defaultOptimizer = Optional.of(new Optimizer(DEFAULT_OPTIMIZER, "{}"));
		
		return firstExistingValueOf(
				shippingServiceOptimizer
				, organizationCartOptimizer 
				, defaultOptimizer)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0006));
	}
	
	

	private Optional<Optimizer> createOptimizerData(OrganizationCartOptimizationEntity entity){
		return ofNullable(entity)
				.map(Optimizer.BasicData::new)
				.flatMap(this::createOptimizer);
	}



	private Optional<Optimizer> createOptimizer(Optimizer.BasicData data){
		return getStrategy(data.getStrategy())
				.map(strategy -> new Optimizer(strategy, data.getConfigurationJson()));
	}


	private Optional<CartOptimizationStrategy> getStrategy(String name){
		return ofNullable(name)
				.map(this::validateCartOptimizationStrategy)
				.flatMap(CartOptimizationStrategy::getCartOptimizationStrategy);
	}


	
	private String validateCartOptimizationStrategy(String strategy) {
		if(isBlankOrNull(strategy)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0006);
		}else if(!isValidStrategy(strategy)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0007, strategy);
		}
		return strategy;
	}

	

	private Optional<Optimizer> getCartOptimizationStrategyForOrganization(){
		var orgId = securityService.getCurrentUserOrganizationId();
		return orgCartOptimizerRepo
				.findOrganizationDefaultOptimizationStrategy(orgId)
				.flatMap(this::createOptimizerData);
	}

	
	
	public Optional<Optimizer> getCartOptimizationStrategyForShippingService(String shippingServiceId){
		var orgId = securityService.getCurrentUserOrganizationId();
		return orgCartOptimizerRepo
				.findByShippingServiceIdAndOrganization_Id(shippingServiceId, orgId)
				.flatMap(this::createOptimizerData);
	}



	@Override
	public <T> void  setCartOptimizationStrategy(CartOptimizationSettingDTO settingDto) {
		var strategy = settingDto.getStrategyName();
		if(isBlankOrNull(strategy)|| !isValidStrategy(strategy)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$PRAM$0001, settingDto.toString());
		}

		var parameters = ofNullable(settingDto.getParameters()).orElse(emptyMap());
		validateConfigJson(strategy, parameters);
		
		persistCartOptimizationInfo(settingDto, strategy, parameters);
	}



	private void persistCartOptimizationInfo(CartOptimizationSettingDTO settingDto, String strategy,
			Map<String,Object> parameters) {
		var shippingServiceId = settingDto.getShippingServiceId();
		var org = securityService.getCurrentUserOrganization();
		String parametersJsonStr;
		try {
			parametersJsonStr = objectMapper.writeValueAsString(parameters);
		} catch (JsonProcessingException e) {
			var orgId = securityService.getCurrentUserOrganizationId();
			logger.error(e,e);
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0014, orgId, WAREHOUSE);
		}

		var parametersEntity =
				orgCartOptimizerRepo
				.findFirstByOptimizationStrategyAndShippingServiceIdAndOrganization_IdOrderByIdDesc(strategy, shippingServiceId, org.getId())
				.orElseGet(() -> getCartOptimizationParameters(settingDto));
		
		parametersEntity.setOptimizationStrategy(strategy);
		parametersEntity.setOrganization(org);
		parametersEntity.setParameters(parametersJsonStr);
		parametersEntity.setShippingServiceId(shippingServiceId);
		
		orgCartOptimizerRepo.save(parametersEntity);
	}



	private <T,P> void validateConfigJson(String strategy, Map<String,Object> configJson) {
		try {
			CartOptimizer<T,P> optimizer = getCartOptimizer(strategy);
			var config = objectMapper.convertValue(configJson, optimizer.getConfigurationClass());
			if(!optimizer.isConfigValid(config)) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0012, configJson.toString());
			};
		} catch (Throwable e) {
			logger.error(e,e);
			var orgId = securityService.getCurrentUserOrganizationId();
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0014, orgId, strategy);
		}
	}



	private OrganizationCartOptimizationEntity getCartOptimizationParameters(CartOptimizationSettingDTO settingDto) {
		var orgId = securityService.getCurrentUserOrganizationId();
		var strategy = settingDto.getStrategyName();
		return orgCartOptimizerRepo
				.findFirstByOptimizationStrategyAndOrganization_IdOrderByIdDesc(strategy, orgId)
				.orElse(new OrganizationCartOptimizationEntity());
	}





	@Override
	public List<CartOptimizationSettingDTO> getCartOptimizationStrategy() {
		var orgId = securityService.getCurrentUserOrganizationId();
		return orgCartOptimizerRepo
				.findByOrganization_Id(orgId)
				.stream()
				.map(this::createSettingDTO)
				.collect(toList());
	}
	

	
	
	
	private CartOptimizationSettingDTO createSettingDTO(OrganizationCartOptimizationEntity entity) {
		var parametersJson = ofNullable(entity.getParameters()).orElse("{}");
		Map<String,Object> params = emptyMap();
		try {
			 params = objectMapper.readValue(parametersJson, new TypeReference<Map<String,Object>>(){});
		} catch (IOException e) {
			logger.error(e,e);
		}

		var settingDto = new CartOptimizationSettingDTO();
		settingDto.setShippingServiceId(entity.getShippingServiceId());
		settingDto.setStrategyName(entity.getOptimizationStrategy());
		settingDto.setParameters(params);
		return settingDto;
	}





	@Override
	public List<CartOptimizationStrategyDTO> listAllCartOptimizationStrategies() {
		return stream(CartOptimizationStrategy.values())
				.map(CartOptimizationStrategy::getValue)
				.distinct()
				.map(this::createCartOptimizationStrategyDTO)
				.collect(toList());
	}



	@Override
	@Transactional
	public void deleteCartOptimizationStrategy(String strategyName, String shippingService) {
		var orgId = securityService.getCurrentUserOrganizationId();
		if(isNull(shippingService)){
			orgCartOptimizerRepo.deleteByOptimizationStrategy(strategyName, orgId);
		}else{
			orgCartOptimizerRepo.deleteByOptimizationStrategy(strategyName, shippingService, orgId);
		}
	}


	private <CartParams, CommonParams> CartOptimizationStrategyDTO createCartOptimizationStrategyDTO(
											String strategyName) {
		CartOptimizer<CartParams, CommonParams> optimizer = getCartOptimizer(strategyName);
		var params = createParamsObject(optimizer);
		return new CartOptimizationStrategyDTO(strategyName, params);
	}





	private <CartParams, CommonParams> CommonParams createParamsObject(CartOptimizer<CartParams, CommonParams> optimizer) {
		try {
			return optimizer.getConfigurationClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error(e,e);
			return null;
		}
	}
}



@Data
@AllArgsConstructor
class Optimizer {
	private CartOptimizationStrategy strategy;
	private String configurationJson;


	@Data
	static class BasicData{
		private String strategy;
		private String configurationJson;

		BasicData(OrganizationCartOptimizationEntity entity){
			this.strategy = entity.getOptimizationStrategy();
			this.configurationJson = entity.getParameters();
		}
	}
}