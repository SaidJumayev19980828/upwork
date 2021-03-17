package com.nasnav.service;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.enumerations.PromotionStatus.*;
import static com.nasnav.enumerations.PromotionType.*;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.persistence.PromotionsEntity.*;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static javax.persistence.criteria.JoinType.INNER;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;

import com.nasnav.dao.CartItemRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dto.PromosConstraints;
import com.nasnav.dto.response.PromotionResponse;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.enumerations.PromotionType;
import com.nasnav.persistence.dto.query.result.CartItemData;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.dao.PromotionRepository;
import com.nasnav.dao.PromotionsCodesUsedRepository;
import com.nasnav.dto.PromotionSearchParamDTO;
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
	
	
	@Autowired
	private EntityManager entityMgr;
	
	
	@Autowired
	private ObjectMapper objectMapper;
	
	
	@Autowired
	private PromotionRepository promoRepo;
	
	
	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private OrderService orderService;

	@Autowired
	private CartService cartService;
	
	@Autowired
	private PromotionsCodesUsedRepository usedPromoRepo;
	@Autowired
	private CartItemRepository cartRepo;
	@Autowired
	private ProductRepository productRepo;
	
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
		dto.setConstrains(readJsonStrAsMap(entity.getConstrainsJson()));
		dto.setCreatedOn(entity.getCreatedOn());
		dto.setDiscount(readJsonStrAsMap(entity.getDiscountJson()));
		dto.setEndDate(entity.getDateEnd().atZone(zoneId));
		dto.setId(entity.getId());
		dto.setIdentifier(entity.getIdentifier());
		dto.setOrganizationId(entity.getOrganization().getId());
		dto.setStartDate(entity.getDateStart().atZone(zoneId));
		dto.setStatus(getPromotionStatusName(entity.getStatus()));
		dto.setUserId(entity.getCreatedBy().getId());
		dto.setUserName(entity.getCreatedBy().getName());
		dto.setTypeId(entity.getTypeId());
		dto.setClassId(entity.getClassId());
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
		validatePromotion(promotion);
		
		PromotionsEntity entity = createPromotionsEntity(promotion);
		return promoRepo.save(entity).getId();
	}







	private PromotionsEntity createPromotionsEntity(PromotionDTO promotion) {
		PromotionsEntity entity = 
				ofNullable(promotion)
				.map(PromotionDTO::getId)
				.map(this::getExistingPromotion)
				.orElseGet(PromotionsEntity::new);
		
		if(isUpdateOperation(promotion)	&& !isInactivePromo(entity)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE
						, PROMO$PARAM$0005, promotion.getId());
		}
		
		EmployeeUserEntity user = (EmployeeUserEntity)securityService.getCurrentUser();
		OrganizationEntity organization = securityService.getCurrentUserOrganization();
		
		Integer status = 
				PromotionStatus
				.getPromotionStatus(promotion.getStatus())
				.map(PromotionStatus::getValue)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE
										, PROMO$PARAM$0001, promotion.getStatus()));

		Integer type = PromotionType.getPromotionType(promotion.getTypeId()).getValue();

		String codeUpperCase = 
				ofNullable(promotion.getCode())
				.map(String::toUpperCase)
				.orElse(null);
		
		entity.setCode(codeUpperCase);
		entity.setConstrainsJson(serializeMap(promotion.getConstrains()));
		entity.setCreatedBy(user);
		entity.setDateEnd(promotion.getEndDate().toLocalDateTime());
		entity.setDateStart(promotion.getStartDate().toLocalDateTime());
		entity.setDiscountJson(serializeMap(promotion.getDiscount()));
		entity.setIdentifier(promotion.getIdentifier());
		entity.setOrganization(organization);
		entity.setStatus(status);
		entity.setUserRestricted(0);
		entity.setTypeId(type);
		entity.setClassId(promotion.getClassId());
		return entity;
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






	private void validatePromotion(PromotionDTO promotion) {
		
		if(anyIsNull(promotion, promotion.getDiscount()
				, promotion.getEndDate(), promotion.getIdentifier()
				, promotion.getStartDate(), promotion.getStatus()
				, promotion.getTypeId(), promotion.getClassId())) {
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
		if (Objects.equals(PromotionType.getPromotionType(promotion.getTypeId()), PromotionType.PROMO_CODE)) {
			if (promotion.getCode() == null) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, PROMO$PARAM$0013, promotion.getTypeId());
			}
			if (isCodeRepeated(promotion)) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE
						, PROMO$PARAM$0006, promotion.getCode());
			}
		}
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
	public BigDecimal calcPromoDiscountForCart(String promoCode) {
		BigDecimal cartTotal = cartService.calculateCartTotal();
		
		return calcPromoDiscount(promoCode, cartTotal);
	}






	private BigDecimal calcDiscount(PromotionsEntity promo, BigDecimal cartTotal) {
		Map<String,Object> discountData = readJsonStrAsMap(promo.getDiscountJson());
		Map<String,Object> constrains = readJsonStrAsMap(promo.getConstrainsJson());

		BigDecimal percent =
				getOptionalBigDecimal(discountData, DISCOUNT_PERCENT)
				.orElse(ZERO);

		Optional<BigDecimal> maxDiscount = getOptionalBigDecimal(constrains, DISCOUNT_AMOUNT_MAX);

		BigDecimal discount =
				percent
				.multiply(new BigDecimal("0.01"))
				.multiply(cartTotal);

		return maxDiscount
				.map( max -> discount.compareTo(max) >=0 ? max: discount)
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


	private Optional<Long> getOptionalLong(Map<String, Object> map, String key) {
		return ofNullable(map.get(key))
				.map(val -> val.toString())
				.map(Long::parseLong);
	}



	@Override
	public BigDecimal calcPromoDiscount(String promoCode, BigDecimal subTotal) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		PromotionsEntity promo = 
				promoRepo
				.findByCodeAndOrganization_IdAndActiveNow(promoCode, orgId)
				.orElseThrow(()-> new RuntimeBusinessException(NOT_ACCEPTABLE
										, PROMO$PARAM$0008, promoCode));
		
		validatePromoCode(promoCode, promo, subTotal);
		
		Map<String,Object> discountData = readJsonStrAsMap(promo.getDiscountJson());
		return getOptionalBigDecimal(discountData, DISCOUNT_AMOUNT)
				.orElse(calcDiscount(promo, subTotal));
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

	@Override
	public BigDecimal calculateBuyXGetYPromoDiscount( List<CartItemData> items) {
		BigDecimal discount = ZERO;
		List<PromotionsEntity> promosList = promoRepo
				.findByOrganization_IdAndTypeIdIn(securityService.getCurrentUserOrganizationId(), asList(BUY_X_GET_Y.getValue()));

		PromosConstraints constraints;
		for(PromotionsEntity promo : promosList) {
            try {
                constraints = objectMapper.readValue(promo.getConstrainsJson(), PromosConstraints.class);
            } catch (IOException e) {
                logger.error(e,e);
                return discount;
            }
            if ( constraints.getBrands() != null) {
                List<Long> allowedBrands = constraints.getBrands().getIds();
                Integer productQuantityMin = constraints.getProductQuantityMin();
                Integer productToGive = constraints.getProductToGive();
                discount.add(items
                        .stream()
                        .filter(i -> allowedBrands.contains(i.getBrandId()))
                        .filter(i -> i.getQuantity() >= productQuantityMin)
                        .map(i -> new BigDecimal((i.getQuantity() / productQuantityMin) * productToGive).multiply(i.getPrice()))
                        .reduce(ZERO, BigDecimal::add));
            }
            else if (constraints.getTags() != null) {
                List<Long> allowedTags = constraints.getTags().getIds();
                List<Long> allowedProducts = productRepo.getProductIdsByTagsList(allowedTags);
                discount.add(getDiscountBasedOnProductsList(constraints, items, allowedProducts));
            }
            else if (constraints.getProducts() != null) {
                List<Long> allowedProducts = constraints.getProducts().getIds();
                discount.add(getDiscountBasedOnProductsList(constraints, items, allowedProducts));
            }  else {
                Integer productQuantityMin = constraints.getProductQuantityMin();
                Integer productToGive = constraints.getProductToGive();
                discount.add(items
                        .stream()
                        .filter(i -> i.getQuantity() >= productQuantityMin)
                        .map(i -> new BigDecimal((i.getQuantity() / productQuantityMin) * productToGive).multiply(i.getPrice()))
                        .reduce(ZERO, BigDecimal::add));
            }
		}
		return discount;
	}

	private BigDecimal getDiscountBasedOnProductsList(PromosConstraints constraints, List<CartItemData> items,
													  List<Long> allowedProducts) {
		Integer productQuantityMin = constraints.getProductQuantityMin();
		Integer productToGive = constraints.getProductToGive();
		return items
				.stream()
				.filter(i -> allowedProducts.contains(i.getProductId()))
				.filter(i -> i.getQuantity() >= productQuantityMin)
				.map(i -> new BigDecimal((i.getQuantity() / productQuantityMin) * productToGive).multiply(i.getPrice()))
				.reduce(ZERO, BigDecimal::add);
	}

	@Override
	public BigDecimal calculateShippingPromoDiscount(BigDecimal totalShippingValue){
		BigDecimal discount = ZERO;
		BigDecimal tmpDiscount;
		BigDecimal totalCartValue = cartRepo.findTotalCartValueByUser_Id(securityService.getCurrentUser().getId());
		List<PromotionsEntity> promoList = promoRepo
				.findByOrganization_IdAndTypeIdIn(securityService.getCurrentUserOrganizationId(), asList(SHIPPING.getValue()));
		for(PromotionsEntity promo : promoList) {
			if (isPromoValidForTheCart(promo, totalCartValue)) {
				Map<String, Object> discountData = readJsonStrAsMap(promo.getDiscountJson());
				tmpDiscount = getOptionalBigDecimal(discountData, DISCOUNT_AMOUNT)
						.orElse(calcDiscount(promo, totalShippingValue));
				if (tmpDiscount.compareTo(discount) > 0) {
					discount = tmpDiscount;
				}
			}
		}
		return discount;
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

	@Override
	public BigDecimal calculateTotalCartDiscount() {
		BigDecimal discount = ZERO;
		BigDecimal tmpDiscount;
		BigDecimal totalCartValue = cartRepo.findTotalCartValueByUser_Id(securityService.getCurrentUser().getId());
		Long totalCartQuantity = cartRepo.findTotalCartQuantityByUser_Id(securityService.getCurrentUser().getId());
		List<PromotionsEntity> promoList = promoRepo
				.findByOrganization_IdAndTypeIdIn(securityService.getCurrentUserOrganizationId(), asList(TOTAL_CART_ITEMS_VALUE.getValue(), TOTAL_CART_ITEMS_QUANTITY.getValue()));
		for(PromotionsEntity promo : promoList) {
			if (promo.getTypeId().equals(TOTAL_CART_ITEMS_VALUE.getValue()) && !isPromoValidForTheCart(promo, totalCartValue) ||
				promo.getTypeId().equals(TOTAL_CART_ITEMS_QUANTITY.getValue()) && !isValidPromoForCartQuantity(promo, totalCartQuantity)) {
				continue;
			}
			Map<String, Object> discountData = readJsonStrAsMap(promo.getDiscountJson());
			tmpDiscount = getOptionalBigDecimal(discountData, DISCOUNT_AMOUNT)
					.orElse(calcDiscount(promo, totalCartValue));
			if (tmpDiscount.compareTo(discount) > 0) {
				discount = tmpDiscount;
			}
		}
		return discount;
	}

	private boolean isValidPromoForCartQuantity(PromotionsEntity promo, Long totalCartQuantity) {
		Map<String,Object> constrains = readJsonStrAsMap(promo.getConstrainsJson());
		Long minAmount = getOptionalLong(constrains, MIN_QUANTITY_PROP).orElse(0L);
		return totalCartQuantity.compareTo(minAmount) >= 0;
	}

}







@AllArgsConstructor
class SearchParams {
	public Optional<Integer> status;
	public Optional<LocalDateTime> startTime;
	public Optional<LocalDateTime> endTime;
	public Optional<Long> id;
}
