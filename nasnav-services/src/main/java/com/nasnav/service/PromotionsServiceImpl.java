package com.nasnav.service;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.commons.utils.EntityUtils.parseLongSafely;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.enumerations.PromotionStatus.*;
import static com.nasnav.enumerations.PromotionType.*;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.persistence.PromotionsEntity.*;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.FLOOR;
import static java.math.RoundingMode.HALF_EVEN;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static javax.persistence.criteria.JoinType.INNER;
import static org.springframework.beans.BeanUtils.copyProperties;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;

import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.dto.response.PromotionResponse;
import com.nasnav.dto.response.navbox.Cart;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.enumerations.PromotionType;
import lombok.Data;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.dto.response.PromotionDTO;
import com.nasnav.enumerations.PromotionStatus;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.PromotionsCodesUsedEntity;
import com.nasnav.persistence.PromotionsEntity;
import com.nasnav.persistence.UserEntity;

import lombok.AllArgsConstructor;




@Service
public class PromotionsServiceImpl implements PromotionsService {

	private Logger logger = LogManager.getLogger();

	@PersistenceContext
	@Autowired
	private EntityManager entityMgr;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private PromotionRepository promoRepo;
	@Autowired
	private SecurityService securityService;
	@Autowired
	private CartService cartService;
	@Autowired
	private PromotionsCodesUsedRepository usedPromoRepo;
	@Autowired
	private ProductRepository productRepo;

	private Map<PromotionType,Function<PromoInfoContainer, PromoCalcResult>> promoCalculators = emptyMap();


	@PostConstruct
	public void init(){
		promoCalculators = Map.of(
				BUY_X_GET_Y_FROM_BRAND, this::getDiscountBasedOnBrandsList,
				BUY_X_GET_Y_FROM_PRODUCT, this::getDiscountBasedOnProductsList,
				BUY_X_GET_Y_FROM_TAG, this::getDiscountBasedOnTagsList,
				TOTAL_CART_ITEMS_QUANTITY, this::calculateDiscountBasedOnTotalQty,
				TOTAL_CART_ITEMS_VALUE, this::calculateDiscountBasedOnTotalValue,
				PROMO_CODE, this::calcPromoDiscount,
				PROMO_CODE_FROM_BRAND, this::calcPromoDiscountFromSpecificBrands,
				PROMO_CODE_FROM_TAG, this::calcPromoDiscountFromSpecificTags,
				PROMO_CODE_FROM_PRODUCT, this::calcPromoDiscountFromSpecificProducts);
	}



	@Override
	public PromotionResponse getPromotions(PromotionSearchParamDTO searchParams) {
		SearchParams params = createSearchParam(searchParams);
		
		CriteriaBuilder builder = entityMgr.getCriteriaBuilder();
		CriteriaQuery<PromotionsEntity> query = builder.createQuery(PromotionsEntity.class);
		Root<PromotionsEntity> root = query.from(PromotionsEntity.class);
		root.fetch("organization", INNER);
		root.fetch("createdBy", INNER);
		
		ArrayList<Predicate> restrictions = createPromotionsQueryPerdicates(builder, root, params);
		query.select(root)
				.where(restrictions.toArray(new Predicate[0]))
				.orderBy(builder.desc(root.get("id")));
		setPromotionDefaultParams(searchParams);

		List<PromotionDTO> promotions = entityMgr
				.createQuery(query)
				.setFirstResult(searchParams.getStart())
				.setMaxResults(searchParams.getCount())
				.getResultList()
				.stream()
				.map(this::createPromotionDTO)
				.collect(toList());
		Long total = getPromotionsCount(builder, restrictions.toArray(new Predicate[0]));
		return new PromotionResponse(total, promotions);
	}



	@Override
	public AppliedPromotionsResponse calcPromoDiscountForCart(String promoCode, Cart cart) {
		BigDecimal cartTotal = cartService.calculateCartTotal(cart);
		var promoItems = toPromoItems(cart.getItems());
		Long orgId = securityService.getCurrentUserOrganizationId();
		return calculateAllApplicablePromos(promoItems,cartTotal, promoCode, orgId);
	}

	@Override
	public AppliedPromotionsResponse calcPromoDiscountForCart(String promoCode) {
		Long userId = securityService.getCurrentUser().getId();
		var cart = cartService.getUserCart(userId);
		BigDecimal cartTotal = cartService.calculateCartTotal(cart);
		var promoItems = toPromoItems(cart.getItems());
		Long orgId = securityService.getCurrentUserOrganizationId();
		return calculateAllApplicablePromos(promoItems,cartTotal, promoCode, orgId);
	}

	private List<PromoItemDto> toPromoItems(List<CartItem> cartItems) {
		return cartItems
				.stream()
				.map(this::toPromoItem)
				.collect(toUnmodifiableList());
	}



	private PromoItemDto toPromoItem(CartItem cartItem) {
		var promoItem = new PromoItemDto();
		copyProperties(cartItem, promoItem);
		return promoItem;
	}


	private void setPromotionDefaultParams(PromotionSearchParamDTO searchParams) {
		if(searchParams.getStart() == null || searchParams.getStart() < 0){
			searchParams.setStart(0);
		}
		if(searchParams.getCount() == null || (searchParams.getCount() < 1)){
			searchParams.setCount(10);
		} else if (searchParams.getCount() > 1000) {
			searchParams.setCount(1000);
		}
	}


	private Long getPromotionsCount(CriteriaBuilder builder, Predicate[] predicatesArr) {
		CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
		countQuery.select(  builder.count(
				(countQuery.from(PromotionsEntity.class))))
							.where(predicatesArr);
		Long count = entityMgr.createQuery(countQuery).getSingleResult();
		return count;
	}
	
	
	private PromotionDTO createPromotionDTO(PromotionsEntity entity) {
		ZoneId zoneId = ZoneId.of("UTC");
		PromotionDTO dto = new PromotionDTO();
		dto.setCode(entity.getCode());
		try {
			dto.setConstrains(readJsonStr(entity.getConstrainsJson()));
		} catch (RuntimeBusinessException e) {}
		dto.setCreatedOn(entity.getCreatedOn());
		try {
			dto.setDiscount(readJsonStrAsMap(entity.getDiscountJson()));
		} catch (RuntimeBusinessException e) {}
		dto.setEndDate(entity.getDateEnd().atZone(zoneId));
		dto.setId(entity.getId());
		dto.setIdentifier(entity.getIdentifier());
		dto.setName(entity.getName());
		dto.setDescription(entity.getDescription());
		dto.setOrganizationId(entity.getOrganization().getId());
		dto.setStartDate(entity.getDateStart().atZone(zoneId));
		dto.setStatus(getPromotionStatusName(entity.getStatus()));
		dto.setUserId(entity.getCreatedBy().getId());
		dto.setUserName(entity.getCreatedBy().getName());
		dto.setTypeId(entity.getTypeId());
		dto.setPriority(entity.getPriority());
		return dto;
	}

	
	private Map<String,Object> readJsonStrAsMap(String jsonStr){
		String rectified = ofNullable(jsonStr).orElse("{}");
		try {
			Map<String,Object> initialData = objectMapper.readValue(rectified, new TypeReference<Map<String,Object>>(){});
			if (initialData == null)
				initialData = new LinkedHashMap<>();
			return setNumbersAsBigDecimals(initialData);
		} catch (Exception e) {
			logger.error(e,e);
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, PROMO$JSON$0001, jsonStr);
		}
	}

	private PromosConstraints readJsonStr(String jsonStr){
		try {
			return objectMapper.readValue(jsonStr, PromosConstraints.class);
		} catch (Exception e) {
			logger.error(e,e);
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, PROMO$JSON$0001, jsonStr);
		}
	}
	
	

	private Map<String, Object> setNumbersAsBigDecimals(Map<String, Object> initialData) {
		return initialData
				.entrySet()
				.stream()
				.map(this::doSetNumbersAsBigDecimals)
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
	
	
	
	
	@SuppressWarnings("unchecked")
	private Map.Entry<String,Object> doSetNumbersAsBigDecimals(Map.Entry<String, Object> entry){
		return ofNullable(entry.getValue())
				.map(Object::toString)
				.filter(NumberUtils::isParsable)
				.map(BigDecimal::new)
				.map(Object.class::cast)
				.map(val -> new SimpleEntry<>(entry.getKey(), val))
				.map(Map.Entry.class::cast)
				.orElse(entry);
	}






	private SearchParams createSearchParam(PromotionSearchParamDTO searchParams) {

		Optional<Integer> status = 
				ofNullable(searchParams)
				.map(PromotionSearchParamDTO::getStatus)
				.flatMap(PromotionStatus::getPromotionStatus)
				.map(PromotionStatus::getValue);
		
		Optional<LocalDateTime> startTime = 
				ofNullable(searchParams)
				.map(PromotionSearchParamDTO::getStartTime)
				.flatMap(EntityUtils::parseTimeString);
		
		Optional<LocalDateTime> endTime = 
				ofNullable(searchParams)
				.map(PromotionSearchParamDTO::getEndTime)
				.flatMap(EntityUtils::parseTimeString);
		
		Optional<Long> id = 
				ofNullable(searchParams)
				.map(PromotionSearchParamDTO::getId);
		
		return new SearchParams(status, startTime, endTime, id);
	}






	private ArrayList<Predicate> createPromotionsQueryPerdicates(CriteriaBuilder builder
			, Root<PromotionsEntity> root ,SearchParams searchParams) {

		Long orgId = securityService.getCurrentUserOrganizationId();

		ArrayList<Predicate> predicates = new ArrayList<>();

		if(searchParams.status.isPresent()) {
			Predicate predicate = builder.equal(root.get("status"), searchParams.status.get());
			predicates.add(predicate);
		}
		
		if(searchParams.id.isPresent()) {
			Predicate predicate = builder.equal(root.get("id"), searchParams.id.get());
			predicates.add(predicate);
		}

		predicates.add(builder.equal(root.get("organization").get("id"), orgId));
		
		Predicate isPromotionInTimeWindowPredicate = 
				createPromotionInTimeWindowPerdicate(builder, root, searchParams);
		
		predicates.add(isPromotionInTimeWindowPredicate);
		
		return predicates;
	}





	/**
	 * assuming a promotion is active for period between SP and DP, and we search in time
	 * window starting from SW to DW.
	 * The condition for taking a promotions that is active in the search window is
	 * NOT(DP < SW OR DW < SP)
	 * */
	private Predicate createPromotionInTimeWindowPerdicate(CriteriaBuilder builder, Root<PromotionsEntity> root,
			SearchParams searchParams) {
		LocalDateTime searchWindowStart = searchParams.startTime.orElse(now().minusYears(1000));
		LocalDateTime searchWindowEnd = searchParams.endTime.orElse(now().plusYears(1000));
		Path<LocalDateTime> promotionPeriodStart = root.get("dateStart");
		Path<LocalDateTime> promotionPeriodEnd = root.get("dateEnd");
		
		Predicate noIntersectionCondition1 = 
				builder.lessThan(promotionPeriodEnd, searchWindowStart);
		Predicate noIntersectionCondition2 = 
				builder.lessThan(builder.literal(searchWindowEnd), promotionPeriodStart);
		Predicate isPromotionInTimeWindowPredicate = 
				builder.not(builder.or(noIntersectionCondition1, noIntersectionCondition2));
		return isPromotionInTimeWindowPredicate;
	}






	@Override
	public Long updatePromotion(PromotionDTO promotion) {
		if (promotion.getTypeId() == null) {
			promotion.setTypeId(0);
		}
		validatePromotion(promotion);
		
		PromotionsEntity entity = createPromotionsEntity(promotion);
		return promoRepo.save(entity).getId();
	}

	private void validatePromotionDiscount(PromotionDTO promotion, PromotionType promoType) {
		if (!asList(BUY_X_GET_Y_FROM_BRAND, BUY_X_GET_Y_FROM_TAG, BUY_X_GET_Y_FROM_PRODUCT).contains(promoType)) {
			Map<String, Object> discountsMap = promotion.getDiscount();
			if (discountsMap.get(DISCOUNT_PERCENT) == null && discountsMap.get(DISCOUNT_AMOUNT) == null) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, PROMO$PARAM$0015, "amount or percentage", promotion.getIdentifier());
			}
		}
	}

	private void validatePromotionConstraints(PromotionDTO promotion) {
		PromotionType promoType = PromotionType.getPromotionType(promotion.getTypeId());
		switch (promoType) {
			case PROMO_CODE:
			case TOTAL_CART_ITEMS_VALUE:
			case SHIPPING:
			{
				validatePromoCodeConstraint(promotion);
				break;
			}
			case PROMO_CODE_FROM_BRAND: {
				validatePromoBrandsConstraint(promotion);
				break;
			}
			case PROMO_CODE_FROM_TAG: {
				validatePromoTagsConstraint(promotion);
				break;
			}
			case PROMO_CODE_FROM_PRODUCT: {
				validatePromoProductsConstraint(promotion);
				break;
			}
			case BUY_X_GET_Y_FROM_BRAND: {
				validatePromoBuyXGetYConstraints(promotion);
				validatePromoBrandsConstraint(promotion);
				break;
			}
			case BUY_X_GET_Y_FROM_TAG: {
				validatePromoBuyXGetYConstraints(promotion);
				validatePromoTagsConstraint(promotion);
				break;
			}
			case BUY_X_GET_Y_FROM_PRODUCT: {
				validatePromoBuyXGetYConstraints(promotion);
				validatePromoProductsConstraint(promotion);
				break;
			}
			case TOTAL_CART_ITEMS_QUANTITY: {
				validateTotalCartItemsConstraint(promotion);
				break;
			}
		}
	}

	private void validatePromoCartAmountMinConstraint(PromotionDTO promotion) {
		BigDecimal cartAmountMin = promotion.getConstrains().getCartAmountMin();
		if (isBlankOrNull(cartAmountMin)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, PROMO$PARAM$0015, MIN_AMOUNT_PROP, promotion.getIdentifier());
		}
	}
	private void validatePromoDiscountValueMaxConstraint(PromotionDTO promotion) {
		BigDecimal discountValueMax = promotion.getConstrains().getDiscountValueMax();
		if (isBlankOrNull(discountValueMax)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, PROMO$PARAM$0015, DISCOUNT_AMOUNT_MAX, promotion.getIdentifier());
		}
	}

	private void validatePromoCodeConstraint(PromotionDTO promotion) {
		validatePromoCartAmountMinConstraint(promotion);
		validatePromoDiscountValueMaxConstraint(promotion);
	}

	private void validateTotalCartItemsConstraint(PromotionDTO promotion) {
		Long cartQuantityMin = promotion.getConstrains().getCartQuantityMin();
		if (isBlankOrNull(cartQuantityMin)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, PROMO$PARAM$0015, MIN_QUANTITY_PROP, promotion.getIdentifier());
		}
		validatePromoDiscountValueMaxConstraint(promotion);
	}

	private void validatePromoBrandsConstraint(PromotionDTO promotion) {
		Set<Long> brands = promotion.getConstrains().getBrands();
		if (isBlankOrNull(brands)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, PROMO$PARAM$0015, ALLOWED_BRANDS, promotion.getIdentifier());
		}
		validatePromoCodeConstraint(promotion);
	}

	private void validatePromoTagsConstraint(PromotionDTO promotion) {
		Set<Long> tags = promotion.getConstrains().getTags();
		if (isBlankOrNull(tags)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, PROMO$PARAM$0015, ALLOWED_TAGS, promotion.getIdentifier());
		}
		validatePromoCodeConstraint(promotion);
	}

	private void validatePromoProductsConstraint(PromotionDTO promotion) {
		Set<Long> products = promotion.getConstrains().getProducts();
		if (isBlankOrNull(products)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, PROMO$PARAM$0015, ALLOWED_PRODUCTS, promotion.getIdentifier());
		}
		validatePromoCodeConstraint(promotion);
	}

	private void validatePromoBuyXGetYConstraints(PromotionDTO promotion) {
		Integer productsToGive = promotion.getConstrains().getProductToGive();
		Integer productQuantityMin = promotion.getConstrains().getProductQuantityMin();

		if (isBlankOrNull(productsToGive)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, PROMO$PARAM$0015, PRODUCTS_TO_GIVE, promotion.getIdentifier());
		}
		if (isBlankOrNull(productQuantityMin)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, PROMO$PARAM$0015, PRODUCT_QUANTITY_MIN, promotion.getIdentifier());
		}
	}




	private PromotionsEntity createPromotionsEntity(PromotionDTO promotion) {
		PromotionsEntity entity = getOrCreatePromotionEntity(promotion);
		
		if(isUpdateOperation(promotion)	&& !isInactivePromo(entity)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE
						, PROMO$PARAM$0005, promotion.getId());
		}
		
		EmployeeUserEntity user = (EmployeeUserEntity)securityService.getCurrentUser();
		OrganizationEntity organization = securityService.getCurrentUserOrganization();
		
		Integer status = getPromotionStatus(promotion.getStatus());

		Integer type = PromotionType.getPromotionType(promotion.getTypeId()).getValue();
		Integer priority =
				ofNullable(promotion.getPriority())
				.orElse(0);

		String codeUpperCase = 
				ofNullable(promotion.getCode())
				.map(String::toUpperCase)
				.orElse(null);


		entity.setCode(codeUpperCase);
		entity.setConstrainsJson(serializeDTO(promotion.getConstrains()));
		entity.setCreatedBy(user);
		entity.setDateEnd(promotion.getEndDate().toLocalDateTime());
		entity.setDateStart(promotion.getStartDate().toLocalDateTime());
		entity.setDiscountJson(serializeMap(promotion.getDiscount()));
		entity.setIdentifier(promotion.getIdentifier());
		entity.setName(promotion.getName());
		entity.setDescription(promotion.getDescription());
		entity.setBanner(promotion.getBanner());
		entity.setCover(promotion.getCover());
		entity.setOrganization(organization);
		entity.setStatus(status);
		entity.setUserRestricted(0);
		entity.setTypeId(type);
		entity.setPriority(priority);
		return entity;
	}

	private Integer getPromotionStatus(String statusString) {
		return PromotionStatus
				.getPromotionStatus(statusString)
				.map(PromotionStatus::getValue)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE
						, PROMO$PARAM$0001, statusString));
	}

	private PromotionsEntity getOrCreatePromotionEntity(PromotionDTO promotion) {
		return ofNullable(promotion)
						.map(PromotionDTO::getId)
						.map(this::getExistingPromotion)
						.orElseGet(PromotionsEntity::new);
	}
	
	
	private PromotionsEntity getExistingPromotion(Long id){
		return ofNullable(id)
				.flatMap(promoRepo::findById)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE
											, PROMO$PARAM$0007, id));
	} 





	private String serializeMap(Map<String, Object> map) {
		try {
			return objectMapper.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			logger.error(e,e);
			return "{}";
		}
	}

	private String serializeDTO(PromosConstraints dto) {
		try {
			return objectMapper.writeValueAsString(dto);
		} catch (JsonProcessingException e) {
			logger.error(e,e);
			return "{}";
		}
	}




	private void validatePromotion(PromotionDTO promotion) {
		
		if(anyIsNull(promotion, promotion.getDiscount()
				, promotion.getEndDate(), promotion.getIdentifier()
				, promotion.getStartDate(), promotion.getStatus()
				, promotion.getTypeId())) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE
					, PROMO$PARAM$0002, promotion.toString());
		}
		if(promotion.getEndDate().isBefore(promotion.getStartDate())) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE
					, PROMO$PARAM$0003);
		}
		if(promotion.getEndDate().isBefore(ZonedDateTime.now())) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE
					, PROMO$PARAM$0004);
		}
		PromotionType promoType = PromotionType.getPromotionType(promotion.getTypeId());
		if (asList(PROMO_CODE, PROMO_CODE_FROM_PRODUCT, PROMO_CODE_FROM_TAG, PROMO_CODE_FROM_BRAND).contains(promoType)) {
			if (isBlankOrNull(promotion.getCode())) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, PROMO$PARAM$0013, promotion.getTypeId());
			}
			if (isCodeRepeated(promotion)) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE
						, PROMO$PARAM$0006, promotion.getCode());
			}
		}

		validatePromotionConstraints(promotion);
		validatePromotionDiscount(promotion, promoType);
	}



	private boolean isCodeRepeated(PromotionDTO promotion) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		if(isUpdateOperation(promotion)) {
			return promoRepo.existsByCodeAndOrganization_IdAndIdNotAndActiveNow(
					 promotion.getCode(), orgId, promotion.getId());
		}else {
			return promoRepo.existsByCodeAndOrganization_IdAndActiveNow(
					 promotion.getCode(), orgId);
		}
	}



	private boolean isUpdateOperation(PromotionDTO promotion) {
		return nonNull(promotion.getId());
	}


	@Override
	public AppliedPromotionsResponse calculateAllApplicablePromos(List<PromoItemDto> items, BigDecimal totalCartValue,
																  String promoCode, Long orgId) {
		var calculators =  getPromoCalculators(promoCode, orgId);
		var discountAccumulator = ZERO;
		var itemsState = new HashSet<>(items);
		List<AppliedPromo> appliedPromosData = new ArrayList<>();
		for(var calc: calculators){
			var info = new PromoInfoContainer(calc.getPromoEntity(), itemsState, totalCartValue, promoCode);
			var result = calc.getCalcFunction().apply(info);
			var calculatorDiscount =
					ofNullable(result)
					.map(PromoCalcResult::getDiscount)
					.orElse(getTotalDiscount(result));

			if(calculatorWasApplied(result, calculatorDiscount)){
				removeConsumedItems(itemsState, result);
				discountAccumulator = discountAccumulator.add(calculatorDiscount);
				String promoName = ofNullable(calc.getPromoEntity().getCode())
						.orElse(calc.getPromoEntity().getIdentifier());
				AppliedPromo appliedPromoData = new AppliedPromo();
				appliedPromoData.setEntity(calc.getPromoEntity());
				appliedPromoData.setPromoName(promoName);
				appliedPromoData.setAppliedItems(result.items.stream().map(PromoItemDiscount::getItem).map(PromoItemDto::getStockId).collect(toSet()));
				appliedPromoData.setDiscount(calculatorDiscount);
				appliedPromosData.add(appliedPromoData);
			}

			var isStopOtherPromos = false; //should be fetched later from the promotion entity
			if(isStopOtherPromos){
				break;
			}
		}
		AppliedPromotionsResponse response = new AppliedPromotionsResponse();

		response.setTotalDiscount(discountAccumulator);
		response.setAppliedPromos(appliedPromosData);
		return response;
	}



	private boolean calculatorWasApplied(PromoCalcResult result, BigDecimal calculatorDiscount) {
		return anyNonZeroDiscounts(calculatorDiscount) && itemConsumed(result);
	}


	private boolean itemConsumed(PromoCalcResult result) {
		return ofNullable(result)
				.map(PromoCalcResult::getItems)
				.map(items -> !items.isEmpty())
				.orElse(false);
	}


	private boolean anyNonZeroDiscounts(BigDecimal discount) {
		return discount.compareTo(ZERO) > 0;
	}


	private BigDecimal getTotalDiscount(PromoCalcResult result) {
		return ofNullable(result.items)
				.orElse(emptySet())
				.stream()
				.map(PromoItemDiscount::getDiscount)
				.reduce(ZERO, BigDecimal::add);
	}



	private void removeConsumedItems(Set<PromoItemDto> itemsState, PromoCalcResult result) {
		ofNullable(result.items)
				.orElse(emptySet())
				.stream()
				.map(PromoItemDiscount::getItem)
				.forEach(itemsState::remove);
	}



	private List<PromoCalculator> getPromoCalculators(String promoCode, Long orgId) {
		var normalizedPromoCode = ofNullable(promoCode).map(String::toLowerCase).orElse("");
		var promos = promoRepo
				.findByOrganization_IdAndTypeIdNotIn(orgId, asList(SHIPPING.getValue()), normalizedPromoCode);
		if (promos.isEmpty()) {
			promos = promoRepo
				.findByOrganization_IdAndTypeIdNotIn(securityService.getCurrentUserOrganizationId(),
						asList(SHIPPING.getValue()),
						normalizedPromoCode);
		}
		if (promoCode != null && !promoCode.equals("")&& promos.size() == 0) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, PROMO$PARAM$0008, promoCode);
		}
		return promos.stream().map(this::getCalculator).collect(toList());
	}



	private  PromoCalculator getCalculator(PromotionsEntity promoEntity){
		return ofNullable(promoEntity)
				.map(PromotionsEntity::getTypeId)
				.map(PromotionType::getPromotionType)
				.map(promoCalculators::get)
				.map(fn -> new PromoCalculator(fn, promoEntity))
				.orElse(new PromoCalculator(this::doNothingCalc, promoEntity));
	}


	private Long calcTotalCartQuantity(Set<PromoItemDto> items) {
		return items
				.stream()
				.map(PromoItemDto::getQuantity)
				.filter(Objects::nonNull)
				.map(Integer::longValue)
				.reduce(0L, Long::sum);
	}


	private BigDecimal calcDiscount(PromotionsEntity promo, BigDecimal cartTotal) {
		var discountData = readJsonStrAsMap(promo.getDiscountJson());
		var constrains = readJsonStrAsMap(promo.getConstrainsJson());
		var percent =
				getOptionalBigDecimal(discountData, DISCOUNT_PERCENT)
				.orElse(ZERO);

		var maxDiscount = getOptionalBigDecimal(constrains, DISCOUNT_AMOUNT_MAX);

		var discount =
				percent
				.multiply(new BigDecimal("0.01"))
				.multiply(cartTotal);

		return maxDiscount
				.filter(max -> discount.compareTo(max) >= 0)
				.orElse(discount)
				.setScale(2, HALF_EVEN);
	}






	private void validatePromoCode(String promoCode, PromotionsEntity promo, BigDecimal cartTotal) {
		if(!isPromoValidForTheCart(promo, cartTotal)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE
						, PROMO$PARAM$0009, promoCode);
		}else if(promoAlreadyUsedByUser(promo)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE
						, PROMO$PARAM$0010, promoCode);
		}
	}






	private boolean promoAlreadyUsedByUser(PromotionsEntity promo) {
		UserEntity user = (UserEntity)securityService.getCurrentUser();
		return usedPromoRepo.existsByPromotionAndUser(promo, user);
	}






	private boolean isPromoValidForTheCart(PromotionsEntity promo, BigDecimal cartTotal) {
		Map<String,Object> constrains = readJsonStrAsMap(promo.getConstrainsJson());
		BigDecimal minAmount = 
				getOptionalBigDecimal(constrains, MIN_AMOUNT_PROP)
				.orElse(ZERO);
		return cartTotal.compareTo(minAmount) >= 0;
	}






	private Optional<BigDecimal> getOptionalBigDecimal(Map<String, Object> map, String key) {
		return ofNullable(map.get(key))
				.map(BigDecimal.class::cast);
	}



	private PromoCalcResult calcPromoDiscount(PromoInfoContainer info) {
		var promoCode = info.promoCode;
		var subTotal = info.totalItemsValue;
		PromotionsEntity promo = info.promo;

		if (isBlankOrNull(promoCode)) {
			return emptyResult();
		}

		validatePromoCode(promoCode, promo, subTotal);
		validatePromoApplicableToCurrentUser(promo);

		var discount = getDiscount(subTotal, promo);
		var consumedItems = consumeAllItems(info);
		return new PromoCalcResult(discount, consumedItems);
	}

	private PromoCalcResult calcPromoDiscountFromSpecificBrands(PromoInfoContainer info) {
		validatePromoApplicableToCurrentUser(info.promo);
		var constraints = getPromoConstraints(info.promo);
		var allowedProducts= getAllowedProductsPerBrand(info, constraints)
				.stream()
				.map(PromoItemDto::getProductId)
				.collect(toSet());
		return calcPromoDiscountForSpecificItems(info, allowedProducts);
	}

	private PromoCalcResult calcPromoDiscountFromSpecificTags(PromoInfoContainer info) {
		validatePromoApplicableToCurrentUser(info.promo);
		var constraints = getPromoConstraints(info.promo);
		var allowedTags = ofNullable(constraints.getTags()).orElse(emptySet());
		var allowedProducts = productRepo.getProductIdsByTagsList(allowedTags);
		return calcPromoDiscountForSpecificItems(info, allowedProducts);
	}

	private PromoCalcResult calcPromoDiscountFromSpecificProducts(PromoInfoContainer info) {
		validatePromoApplicableToCurrentUser(info.promo);
		var constraints = getPromoConstraints(info.promo);
		var allowedProducts = constraints.getProducts();
		return calcPromoDiscountForSpecificItems(info, allowedProducts);
	}

	private PromoCalcResult calcPromoDiscountForSpecificItems(PromoInfoContainer info, Set<Long> allowedProducts) {
		var applicableItems = getAllowedProductsPerProducts(info, allowedProducts);

		var promoCode = info.promoCode;
		var subTotal = applicableItems.stream().map(this::calcTotalValue).reduce(ZERO, BigDecimal::add);
		if (isBlankOrNull(promoCode)) {
			return emptyResult();
		}
		PromotionsEntity promo = info.promo;
		validatePromoCode(promoCode, promo, subTotal);

		var discount = getDiscount(subTotal, promo);

		var consumedItems = consumeSubsetItems(info, applicableItems);
		return new PromoCalcResult(discount, consumedItems);
	}

	private void validatePromoApplicableToCurrentUser(PromotionsEntity promo) {
		PromosConstraints promosConstraints = getPromoConstraints(promo);
		Set<Long> applicableUsers = promosConstraints.getUsers();

		if(!containCurrentUser(applicableUsers))
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, PROMO$PARAM$0016, promo.getCode());
	}

	private boolean containCurrentUser(Set<Long> users){
		Long currentUser = securityService.getCurrentUser().getId();
		return users == null || users.contains(currentUser);
	}

	@Override
	public void setPromotionAsUsed(PromotionsEntity promotion, UserEntity user) {
		PromotionsCodesUsedEntity usedPromotion = new PromotionsCodesUsedEntity();
		usedPromotion.setPromotion(promotion);
		usedPromotion.setUser(user);
		usedPromotion.setTime(now());		
		usedPromoRepo.save(usedPromotion);
	}






	@Override
	public void redeemUsedPromotion(PromotionsEntity promotion, UserEntity user) {
		usedPromoRepo.deleteByPromotion_IdAndUser_Id(promotion.getId(), user.getId());
	}




	@Override
	public void removePromotion(Long promotionId) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		PromotionsEntity promo =
				promoRepo
					.findByIdAndOrganization_Id(promotionId, orgId)
						.orElseThrow(()-> new RuntimeBusinessException(NOT_ACCEPTABLE
								, PROMO$PARAM$0007, promotionId));

		if(isInactivePromo(promo)) {
			promoRepo.delete(promo);
		}else if (isTerminatedPromo(promo)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, PROMO$PARAM$0011, promotionId);
		}else{
			if(isStartDateInFuture(promo)){
				promo.setDateEnd(promo.getDateStart());
			}else{
				promo.setDateEnd(now());
			}
			promo.setStatus(TERMINATED.getValue());
			promoRepo.save(promo);
		}
	}


	private boolean isInactivePromo(PromotionsEntity promo) {
		return Objects.equals(INACTIVE.getValue(), promo.getStatus());
	}

	private boolean isTerminatedPromo(PromotionsEntity promo) {
		return Objects.equals(TERMINATED.getValue(), promo.getStatus());
	}


	private boolean isStartDateInFuture(PromotionsEntity promo) {
		return promo.getDateStart().isAfter(now());
	}




	private PromoCalcResult getDiscountBasedOnBrandsList(PromoInfoContainer promoInfoContainer) {
		PromosConstraints constraints = getPromoConstraints(promoInfoContainer.promo);
		var applicableItems= getAllowedProductsPerBrand(promoInfoContainer, constraints);
		return getPromoCalcResult(constraints, applicableItems);
	}

	private List<PromoItemDto> getAllowedProductsPerBrand(PromoInfoContainer promoInfoContainer, PromosConstraints constraints) {
		var allowedBrands = constraints.getBrands();
		return promoInfoContainer
						.items
						.stream()
						.filter(i -> allowedBrands.contains(i.getBrandId()))
						.collect(toList());
	}

	private List<PromoItemDto> getAllowedProductsPerProducts(PromoInfoContainer promoInfoContainer, Set<Long> allowedProducts) {
		return promoInfoContainer
						.items
						.stream()
						.filter(i -> allowedProducts.contains(i.getProductId()))
						.collect(toList());
	}


	private PromoCalcResult getPromoCalcResult(PromosConstraints constraints, List<PromoItemDto> applicableItems) {
		var discount = calculateBuyXGetYDiscountValue(applicableItems, constraints);
		var consumedItems = getConsumedItems(applicableItems, discount);
		return new PromoCalcResult(discount, consumedItems);
	}



	private Set<PromoItemDiscount> getConsumedItems(List<PromoItemDto> applicableItems, BigDecimal discount) {
		return applicableItems
				.stream()
				.filter(i -> discount.compareTo(ZERO) > 0)
				.map(item -> new PromoItemDiscount(item, ZERO))
				.collect(toSet());
	}


	private BigDecimal calcTotalValue(PromoItemDto item) {
		var price = ofNullable(item.getPrice()).orElse(ZERO);
		var discount = ofNullable(item.getDiscount()).orElse(ZERO);
		var qty = ofNullable(item.getQuantity()).map(Object::toString).map(BigDecimal::new).orElse(ZERO);
		return price.subtract(discount).multiply(qty);
	}


	private BigDecimal getMinProductPrice(List<PromoItemDto> applicableItems) {
		return applicableItems
				.stream()
				.map(this::calcPriceAfterDiscount)
				.min(BigDecimal::compareTo)
				.orElse(ZERO);
	}



	private PromosConstraints getPromoConstraints(PromotionsEntity promo) {
		PromosConstraints constraints = new PromosConstraints();
		try {
			constraints = objectMapper.readValue(promo.getConstrainsJson(), PromosConstraints.class);
		} catch (IOException e) {
			logger.error(e, e);
		}
		return constraints;
	}

	private Integer getProductQuantityMin(PromosConstraints constraints) {
		return ofNullable(constraints.getProductQuantityMin()).orElse(Integer.MAX_VALUE);
	}

	private Integer getProductQuantityToGive(PromosConstraints constraints) {
		return ofNullable(constraints.getProductToGive()).orElse(0);
	}


	private BigDecimal getProductQuantityToGiveInDiscount(Integer quantity, Integer productQuantityMin, Integer productToGive) {
		return new BigDecimal((quantity / productQuantityMin) * productToGive).setScale(0, FLOOR);
	}



	private PromoCalcResult getDiscountBasedOnTagsList(PromoInfoContainer promoInfoContainer) {
		PromosConstraints constraints = getPromoConstraints(promoInfoContainer.promo);
		var allowedTags = ofNullable(constraints.getTags()).orElse(emptySet());
		var allowedProducts = productRepo.getProductIdsByTagsList(allowedTags);
		var applicableItems = getAllowedProductsPerProducts(promoInfoContainer, allowedProducts);
		return getPromoCalcResult(constraints, applicableItems);
	}



	private PromoCalcResult getDiscountBasedOnProductsList(PromoInfoContainer promoInfoContainer) {
		var constraints = getPromoConstraints(promoInfoContainer.promo);
		var allowedProducts = constraints.getProducts();
		var applicableItems = getAllowedProductsPerProducts(promoInfoContainer, allowedProducts);
		return getPromoCalcResult(constraints, applicableItems);
	}



	private BigDecimal calculateBuyXGetYDiscountValue(List<PromoItemDto> applicableItems, PromosConstraints constrains) {
		var minQtyToApplyPromo = getProductQuantityMin(constrains);
		var giftQty = getProductQuantityToGive(constrains);
		var minPrice = getMinProductPrice(applicableItems);
		return applicableItems
				.stream()
				.map(PromoItemDto::getQuantity)
				.reduce(Integer::sum)
				.map(totalQty -> getProductQuantityToGiveInDiscount(totalQty, minQtyToApplyPromo, giftQty).multiply(minPrice))
				.orElse(ZERO);
	}



	private BigDecimal calcPriceAfterDiscount(PromoItemDto item) {
		var price = ofNullable(item.getPrice()).orElse(ZERO);
		var discount = ofNullable(item.getDiscount()).orElse(ZERO);
		return price.subtract(discount);
	}



	@Override
	public BigDecimal calculateShippingPromoDiscount(BigDecimal totalShippingValue, BigDecimal totalCartValue){
		Long orgId = securityService.getCurrentUserOrganizationId();
		return promoRepo
				.findByOrganization_IdAndTypeIdIn(orgId, asList(SHIPPING.getValue()))
				.stream()
				.filter(promo -> isPromoValidForTheCart(promo, totalCartValue))
				.map(promo -> getDiscount(totalShippingValue, promo))
				.max(BigDecimal::compareTo)
				.orElse(ZERO);
	}



	private BigDecimal getDiscount(BigDecimal value, PromotionsEntity promo) {
		var discountData = readJsonStrAsMap(promo.getDiscountJson());
		return getOptionalBigDecimal(discountData, DISCOUNT_AMOUNT)
				.orElse(calcDiscount(promo, value));
	}



	private void decreasePromoUsageLimit(PromotionsEntity promo) {
		Map<String,Object> constrains = readJsonStrAsMap(promo.getConstrainsJson());
		BigDecimal newUsageLimit = ofNullable(constrains.get(USAGE_LIMIT))
				.map(BigDecimal.class::cast)
				.orElse(ZERO);
		if (newUsageLimit.compareTo(ZERO) == 0) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, PROMO$PARAM$0014);
		}
		newUsageLimit = newUsageLimit.subtract(ONE);
		constrains.put(USAGE_LIMIT, newUsageLimit);
		promo.setConstrainsJson(serializeMap(constrains));
	}



	public PromoCalcResult calculateDiscountBasedOnTotalValue(PromoInfoContainer promo) {
		BigDecimal totalCartValue = promo.totalItemsValue;
		var consumedItems = consumeAllItems(promo);
		return ofNullable(promo)
				.filter(i -> isPromoValidForTheCart(i.promo, totalCartValue))
				.map(i -> getDiscount(totalCartValue, i.promo))
				.map(discount -> new PromoCalcResult(discount, consumedItems))
				.orElse(emptyResult());
	}



	public PromoCalcResult calculateDiscountBasedOnTotalQty(PromoInfoContainer info) {
		var totalCartQuantity = calcTotalCartQuantity(info.items);
		var consumedItems = consumeAllItems(info);
		return ofNullable(info.totalItemsValue)
				.filter(val -> isValidPromoForCartQuantity(info.promo, totalCartQuantity))
				.map(val -> getDiscount(val, info.promo))
				.map(discount -> new PromoCalcResult(discount, consumedItems))
				.orElse(emptyResult());
	}



	private PromoCalcResult emptyResult() {
		return new PromoCalcResult(ZERO, emptySet());
	}


	private Set<PromoItemDiscount> consumeAllItems(PromoInfoContainer info) {
		return info.items
				.stream()
				.map(item -> new PromoItemDiscount(item, ZERO))
				.collect(toSet());
	}

	private Set<PromoItemDiscount> consumeSubsetItems(PromoInfoContainer info, List<PromoItemDto> appliedItems) {
		return info.items
				.stream()
				.filter(item -> appliedItems.contains(item))
				.map(item -> new PromoItemDiscount(item, ZERO))
				.collect(toSet());
	}


	private boolean isValidPromoForCartQuantity(PromotionsEntity promo, Long totalCartQuantity) {
		Map<String,Object> constrains = readJsonStrAsMap(promo.getConstrainsJson());
		Long minAmount = parseLongSafely(constrains.get(MIN_QUANTITY_PROP)).orElse(0L);
		return totalCartQuantity.compareTo(minAmount) >= 0;
	}



	private PromoCalcResult doNothingCalc(PromoInfoContainer info){
		return emptyResult();
	}

}


class PromoInfoContainer {
	PromotionsEntity promo;
	Set<PromoItemDto> items;
	BigDecimal totalItemsValue;
	String promoCode;

	public PromoInfoContainer(PromotionsEntity promo, Set<PromoItemDto> items, BigDecimal totalItemsValue, String promoCode) {
		this.promo = promo;
		this.items = ofNullable(items).orElse(emptySet());
		this.totalItemsValue = ofNullable(totalItemsValue).orElse(ZERO);
		this.promoCode = promoCode;
	}
}


@AllArgsConstructor
@Data
class PromoCalcResult {
	BigDecimal discount;
	Set<PromoItemDiscount> items;
}




@AllArgsConstructor
class SearchParams {
	public Optional<Integer> status;
	public Optional<LocalDateTime> startTime;
	public Optional<LocalDateTime> endTime;
	public Optional<Long> id;
}


@Data
class PromoCalculator{
	private Function<PromoInfoContainer, PromoCalcResult> calcFunction;
	//expected to be used , as it is available in Magento, but currently not persisted in promotions table
	private PromotionsEntity promoEntity;

	public PromoCalculator(Function<PromoInfoContainer, PromoCalcResult> calcFunction, PromotionsEntity promoEntity) {
		this.calcFunction = calcFunction;
		this.promoEntity = promoEntity;
	}
}
