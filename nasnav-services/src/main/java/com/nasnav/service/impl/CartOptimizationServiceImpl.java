package com.nasnav.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.*;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.organization.CartOptimizationSettingDTO;
import com.nasnav.dto.response.CartOptimizationStrategyDTO;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.dto.response.navbox.CartOptimizeResponseDTO;
import com.nasnav.enumerations.ReferralCodeType;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.service.*;
import com.nasnav.service.cart.optimizers.*;
import com.nasnav.shipping.services.mylerz.webclient.dto.Zone;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static com.nasnav.commons.utils.EntityUtils.firstExistingValueOf;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.service.cart.optimizers.CartOptimizationStrategy.*;
import static com.nasnav.service.cart.optimizers.OptimizationStratigiesNames.WAREHOUSE;
import static java.util.Arrays.stream;
import static java.util.Collections.*;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;


@Service
public class CartOptimizationServiceImpl implements CartOptimizationService {
	
	private Logger logger = LogManager.getLogger();
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private CartService cartService;
	@Autowired
	private PromotionsService promoService;
	
	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private OrganizationCartOptimizationRepository orgCartOptimizerRepo;

	@Autowired
	private CartOptimizationHelper helper;

	@Autowired
	private UserAddressRepository userAddressRepo;
	@Autowired
	private ProductVariantsRepository variantsRepo;
	@Autowired
	private CartItemRepository cartItemRepo;

	@Autowired
	private LoyaltyPointsService loyaltyPointsService;
    @Autowired
	private CartItemAddonDetailsRepository cartItemAddonDetailsRepo;

	@Autowired
	private  UserRepository userRepository;

	@Autowired
	private ReferralCodeService referralCodeService;

	@Autowired
	private ShopsRepository shopsRepository;

	@Override
	public CartOptimizeResponseDTO validateAndOptimizeCart(CartCheckoutDTO dto, boolean yeshteryCart) {
		validateAndAssignUserAddress(dto);
		checkCartItemsFromCheckoutStore(dto);
		checkIfCartHasEmptyStock(dto.getCustomerId());
		return optimizeCart(dto, yeshteryCart);
	}

	private void checkCartItemsFromCheckoutStore(CartCheckoutDTO dto) {
		if(!dto.isTwoStepVerified()) {
			return;
		}

		if(cartItemRepo.countByUser_IdAndStock_ShopsEntity_IdNot(dto.getCustomerId(), securityService.getCurrentUserShopId()) > 0) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0020);
		}
	}

	@Override
	@Transactional(rollbackFor = Throwable.class)
	public CartOptimizeResponseDTO optimizeCart(CartCheckoutDTO dto, boolean yeshteryCart) {
		var optimizedCart = createOptimizedCart(dto);
		boolean itemsRemoved = isItemsRemoved(optimizedCart, dto.getPromoCode(),dto);
		var anyPriceChanged = isAnyItemPriceChangedAfterOptimization(optimizedCart) || itemsRemoved;
		var anyItemChanged = isAnyItemChangedAfterOptimization(optimizedCart) || itemsRemoved;
		var returnedCart = getCartObject(optimizedCart, dto, yeshteryCart);
		return new CartOptimizeResponseDTO(anyPriceChanged, anyItemChanged, returnedCart);
	}

	private void validateAndAssignUserAddress(CartCheckoutDTO dto) {
		if (dto.getAddressId() == null) {
			Long addressId = userAddressRepo
					.findFirstByUser_IdOrderByPrincipalDesc(securityService.getCurrentUser().getId())
					.map(UserAddressEntity::getAddress)
					.map(AddressesEntity::getId)
					.orElse(-1L);
			dto.setAddressId(addressId);
		}
	}

	private void checkIfCartIsEmpty(Cart cart) {
		Integer totalQuantity = cart.getItems()
				.stream()
				.map(CartItem::getQuantity)
				.reduce(0, Integer::sum);
		if(totalQuantity == 0) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0018);
		}
	}

	@Deprecated
	private boolean isItemsRemoved(Optional<OptimizedCart> optimizedCart, String promoCode) {
		return optimizedCart.get().getCartItems().size() != cartService.getCart(promoCode, emptySet(), false).getItems().size();
	}

	private boolean isItemsRemoved(Optional<OptimizedCart> optimizedCart, String promoCode , CartCheckoutDTO dto) {
		return optimizedCart.get().getCartItems().size() != cartService.getCart(dto,promoCode, BigDecimal.ZERO, false).getItems().size();
	}

	private Cart getCartObject(Optional<OptimizedCart> optimizedCart, CartCheckoutDTO dto, boolean yeshteryCart) {
		var returnedCart =  optimizedCart
				.map(OptimizedCart::getCartItems)
				.orElse(emptyList())
				.stream()
				.map(OptimizedCartItem::getCartItem)
				.collect(collectingAndThen(toList(), Cart::new));

		checkIfCartIsEmpty(returnedCart);

		returnedCart.setSubtotal(cartService.calculateCartTotal(returnedCart));
		returnedCart.setPromos(promoService.calcPromoDiscountForCart(dto.getPromoCode(), returnedCart));
		returnedCart.setPoints(loyaltyPointsService.calculateCartPointsDiscount(returnedCart.getItems(), dto.getRequestedPoints(), yeshteryCart));
		returnedCart.setDiscount(returnedCart.getPromos().getTotalDiscount().add(returnedCart.getPoints().getTotalDiscount()));
		if(StringUtils.isNotEmpty(dto.getReferralCode()) && !dto.isPayFromReferralBalance()) {
			returnedCart.setDiscount(
					returnedCart.getDiscount()
							.add(referralCodeService.calculateReferralDiscountForCartItems(dto.getReferralCode(), returnedCart.getItems(), securityService.getCurrentUser().getId())));
		}
		Long userId = securityService.getCurrentUser().getId();
		if(dto.isPayFromReferralBalance() && referralCodeService.checkIntervalDateForCurrentOrganization(ReferralCodeType.PAY_WITH_REFERRAL_WALLET)) {
			returnedCart.setDiscount(
					returnedCart.getDiscount().add(referralCodeService.calculateTheWithdrawValueFromReferralBalance(userId, returnedCart.getSubtotal().subtract(returnedCart.getDiscount()))));
		}
		returnedCart.setTotal(returnedCart.getSubtotal().subtract(returnedCart.getDiscount()));
		return returnedCart;
	}



	private boolean isAnyItemPriceChangedAfterOptimization(Optional<OptimizedCart> optimizedCart) {
		return optimizedCart
				.map(OptimizedCart::getCartItems)
				.orElse(emptyList())
				.stream()
				.anyMatch(OptimizedCartItem::getPriceChanged);
	}

	private boolean isAnyItemChangedAfterOptimization(Optional<OptimizedCart> optimizedCart) {
		return optimizedCart
				.map(OptimizedCart::getCartItems)
				.orElse(emptyList())
				.stream()
				.anyMatch(OptimizedCartItem::getItemChanged);
	}


	
	private <T, Config> Optional<OptimizedCart> createOptimizedCart(CartCheckoutDTO dto) {
		var optimizerData = getOptimizerData(dto);
		var strategy = optimizerData.getStrategy();

		CartOptimizer<T, Config> optimizer = getCartOptimizer(strategy.getValue());
		Config config = null;

		var parameters = optimizer.createCartOptimizationParameters(dto);
		var cart = cartService.getCart(dto,dto.getPromoCode(), BigDecimal.ZERO, false);

		if(dto.isTwoStepVerified()) {
			cart.setCustomerId(dto.getCustomerId());
		}
		config = helper.getOptimizerConfig(optimizerData.getConfigurationJson(), optimizer);

		return optimizer.createOptimizedCart(parameters, config, cart);
	}

	private void checkIfCartHasEmptyStock() {
		Long userId = securityService.getCurrentUser().getId();
		int cartItemsCount = cartItemRepo.findCurrentCartItemsByUser_Id(userId).size();
		List<CartItemEntity> outOfStockCartItems = cartItemRepo.findUserOutOfStockCartItems(userId);
		List<CartItemEntity> movedItems = new ArrayList<>();
		if (!outOfStockCartItems.isEmpty()) {
			for (CartItemEntity item : outOfStockCartItems) {
				boolean isEmpty = item
					.getStock()
					.getProductVariantsEntity()
					.getStocks()
					.stream()
					.filter(s -> s.getQuantity() != null && s.getQuantity() > 0)
					.findFirst()
					.isEmpty();
				if (isEmpty) {
					movedItems.add(item);
				}
			}
			if (!movedItems.isEmpty() && movedItems.size() == cartItemsCount) {
				cartService.moveCartItemsToWishlist(movedItems);
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0019);
			}
			
		}
		List<CartItemAddonDetailsEntity> addonsOutOfStock=cartItemAddonDetailsRepo.findOutOfStockCartItemsAddons(userId);
		if(addonsOutOfStock.size()!=0) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$ADDON$0004);

		}
	}

	private void checkIfCartHasEmptyStock(Long customerId) {
		Long userId = customerId != null? customerId :securityService.getCurrentUser().getId();
		int cartItemsCount = cartItemRepo.findCurrentCartItemsByUser_Id(userId).size();
		List<CartItemEntity> outOfStockCartItems = cartItemRepo.findUserOutOfStockCartItems(userId);
		List<CartItemEntity> movedItems = new ArrayList<>();
		if (!outOfStockCartItems.isEmpty()) {
			for (CartItemEntity item : outOfStockCartItems) {
				boolean isEmpty = item
						.getStock()
						.getProductVariantsEntity()
						.getStocks()
						.stream()
						.filter(s -> s.getQuantity() != null && s.getQuantity() > 0)
						.findFirst()
						.isEmpty();
				if (isEmpty) {
					movedItems.add(item);
				}
			}
			if (!movedItems.isEmpty() && movedItems.size() == cartItemsCount) {
				cartService.moveCartItemsToWishlist(movedItems);
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0019);
			}

		}
		List<CartItemAddonDetailsEntity> addonsOutOfStock=cartItemAddonDetailsRepo.findOutOfStockCartItemsAddons(userId);
		if(addonsOutOfStock.size()!=0) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$ADDON$0004);

		}
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
		Long orgId = retrieveOrgId(dto.getCustomerId());
		var shippingServiceOptimizer =
				getCartOptimizationStrategyForShippingService(shippingServiceId,orgId);
		var organizationCartOptimizer =
				getCartOptimizationStrategyForOrganization(orgId);
		if(dto.isTwoStepVerified()) {
			organizationCartOptimizer = Optional.of(new Optimizer(SHOP_PICKUP, "{}"));
		}
		var defaultOptimizer = Optional.of(new Optimizer(DEFAULT_OPTIMIZER, "{}"));
		
		return firstExistingValueOf(
				shippingServiceOptimizer
				, organizationCartOptimizer 
				, defaultOptimizer)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$CRT$0006));
	}
	
public Long retrieveOrgId(Long userId){

	BaseUserEntity userAuthed = securityService.getCurrentUser();
	if(userAuthed instanceof EmployeeUserEntity) {
		UserEntity userEntity = userRepository.findById(userId).orElseThrow(()-> new RuntimeBusinessException(NOT_FOUND, U$0001,userId));
		return  userEntity.getOrganizationId() ;
	}
		return  userAuthed.getOrganizationId();
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

	private Optional<Optimizer> getCartOptimizationStrategyForOrganization(Long orgId){
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
	public Optional<Optimizer> getCartOptimizationStrategyForShippingService(String shippingServiceId,Long orgId){
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