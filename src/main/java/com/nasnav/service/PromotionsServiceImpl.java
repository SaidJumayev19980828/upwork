package com.nasnav.service;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.enumerations.PromotionStatus.INACTIVE;
import static com.nasnav.enumerations.PromotionStatus.getPromotionStatusName;
import static com.nasnav.exceptions.ErrorCodes.PROMO$JSON$0001;
import static com.nasnav.exceptions.ErrorCodes.PROMO$PARAM$0001;
import static com.nasnav.exceptions.ErrorCodes.PROMO$PARAM$0002;
import static com.nasnav.exceptions.ErrorCodes.PROMO$PARAM$0003;
import static com.nasnav.exceptions.ErrorCodes.PROMO$PARAM$0004;
import static com.nasnav.exceptions.ErrorCodes.PROMO$PARAM$0005;
import static com.nasnav.exceptions.ErrorCodes.PROMO$PARAM$0006;
import static com.nasnav.exceptions.ErrorCodes.PROMO$PARAM$0007;
import static java.time.LocalDateTime.now;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static javax.persistence.criteria.JoinType.INNER;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.dao.PromotionRepository;
import com.nasnav.dto.PromotionSearchParamDTO;
import com.nasnav.dto.response.PromotionDTO;
import com.nasnav.enumerations.PromotionStatus;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.PromotionsEntity;

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
	
	@Override
	public List<PromotionDTO> getPromotions(PromotionSearchParamDTO searchParams) {
		
		SearchParams params = createSearchParam(searchParams);
		
		CriteriaBuilder builder = entityMgr.getCriteriaBuilder();
		CriteriaQuery<PromotionsEntity> query = builder.createQuery(PromotionsEntity.class);
		Root<PromotionsEntity> root = query.from(PromotionsEntity.class);
		root.fetch("organization", INNER);
		root.fetch("createdBy", INNER);
		
		ArrayList<Predicate> restrictions = createPromotionsQueryPerdicates(builder, root, params);
		query.select(root).where(restrictions.toArray(new Predicate[0]));
		
		return entityMgr
				.createQuery(query)
				.getResultList()
				.stream()
				.map(this::createPromotionDTO)
				.collect(toList());
	}


	
	
	
	
	private PromotionDTO createPromotionDTO(PromotionsEntity entity) {
		PromotionDTO dto = new PromotionDTO();
		dto.setCode(entity.getCode());
		dto.setConstrains(readJsonStrAsMap(entity.getConstrainsJson()));
		dto.setCreatedOn(entity.getCreatedOn());
		dto.setDiscount(readJsonStrAsMap(entity.getDiscountJson()));
		dto.setEndDate(entity.getDateEnd());
		dto.setId(entity.getId());
		dto.setIdentifier(entity.getIdentifier());
		dto.setOrganizationId(entity.getOrganization().getId());
		dto.setStartDate(entity.getDateStart());
		dto.setStatus(getPromotionStatusName(entity.getStatus()));
		dto.setUserId(entity.getCreatedBy().getId());
		dto.setUserName(entity.getCreatedBy().getName());
		return dto;
	}
	
	
	
	
	
	
	private Map<String,Object> readJsonStrAsMap(String jsonStr){
		String rectified = ofNullable(jsonStr).orElse("{}");
		try {
			return objectMapper.readValue(rectified, new TypeReference<Map<String,Object>>(){});
		} catch (Exception e) {
			logger.error(e,e);
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, PROMO$JSON$0001, jsonStr);
		}
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
		ArrayList<Predicate> predicates = new ArrayList<>();
		
		if(searchParams.status.isPresent()) {
			Predicate predicate = builder.equal(root.get("status"), searchParams.status.get());
			predicates.add(predicate);
		}
		
		if(searchParams.id.isPresent()) {
			Predicate predicate = builder.equal(root.get("id"), searchParams.id.get());
			predicates.add(predicate);
		}
		
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
		
		if(isUpdateOperation(promotion)
				&& !Objects.equals(entity.getStatus(), INACTIVE.getValue())) {
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
		
		entity.setCode(promotion.getCode());
		entity.setConstrainsJson(serializeMap(promotion.getConstrains()));
		entity.setCreatedBy(user);
		entity.setDateEnd(promotion.getEndDate());
		entity.setDateStart(promotion.getStartDate());
		entity.setDiscountJson(serializeMap(promotion.getDiscount()));
		entity.setIdentifier(promotion.getIdentifier());
		entity.setOrganization(organization);
		entity.setStatus(status);
		entity.setUserRestricted(0);
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
		
		if(anyIsNull(promotion, promotion.getCode(), promotion.getDiscount()
				, promotion.getEndDate(), promotion.getIdentifier()
				, promotion.getStartDate(), promotion.getStatus())) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE
					, PROMO$PARAM$0002, promotion.toString());
		}
		if(promotion.getEndDate().isBefore(promotion.getStartDate())) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE
					, PROMO$PARAM$0003);
		}
		if(promotion.getEndDate().isBefore(now())
				|| promotion.getStartDate().isBefore(now())) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE
					, PROMO$PARAM$0004);
		}
		if(isCodeRepeated(promotion)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE
					, PROMO$PARAM$0006, promotion.getCode());
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
		BigDecimal cartTotal = orderService.calculateCartTotal();
		return cartTotal.divide(new BigDecimal("10"));
	}

}







@AllArgsConstructor
class SearchParams{
	public Optional<Integer> status;
	public Optional<LocalDateTime> startTime;
	public Optional<LocalDateTime> endTime;
	public Optional<Long> id;
}
