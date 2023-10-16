package com.nasnav.dao;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nasnav.persistence.PromotionsEntity;

public interface PromotionRepository extends JpaRepository<PromotionsEntity, Long> {


	@Query("SELECT COUNT(promo) > 0 "
			+ " FROM PromotionsEntity promo "
			+ " LEFT JOIN promo.organization org "
			+ " WHERE UPPER(promo.code) = UPPER(:code) "
			+ " AND promo.id != :id "
			+ " AND org.id = :orgId "
			+ " AND now() between promo.dateStart and promo.dateEnd ")
	boolean existsByCodeAndOrganization_IdAndIdNotAndActiveNow(
			@Param("code") String code
			, @Param("orgId") Long orgId
			, @Param("id") Long id);


	@Query("SELECT COUNT(promo) > 0 "
			+ " FROM PromotionsEntity promo "
			+ " LEFT JOIN promo.organization org "
			+ " WHERE UPPER(promo.code) = UPPER(:code) "
			+ " AND org.id = :orgId"
			+ " AND now() between promo.dateStart and promo.dateEnd ")
	boolean existsByCodeAndOrganization_IdAndActiveNow(
			@Param("code") String code
			, @Param("orgId") Long orgId);


	@Query("SELECT promo "
			+ " FROM PromotionsEntity promo "
			+ " LEFT JOIN promo.organization org "
			+ " WHERE UPPER(promo.code) = UPPER(:code) "
			+ " AND org.id = :orgId"
			+ " AND now() between promo.dateStart and promo.dateEnd ")
	Optional<PromotionsEntity> findByCodeAndOrganization_IdAndActiveNow(
			@Param("code") String code
			, @Param("orgId") Long orgId);

	Optional<PromotionsEntity> findByIdAndOrganization_Id(Long promotionId, Long orgId);


	@Query("select promo "
			+ " FROM PromotionsEntity promo "
			+ " LEFT JOIN promo.organization org "
			+ " where org.id = :orgId "
			+ " AND promo.typeId in :typeIds "
			+ " AND now() between promo.dateStart and promo.dateEnd"
			+ " AND promo.status = 1"
			+ " order by promo.priority desc")
	List<PromotionsEntity> findByOrganization_IdAndTypeIdIn(@Param("orgId") Long orgId,
															@Param("typeIds") List<Integer> typeIds);

	@Query("select promo "
			+ " FROM PromotionsEntity promo "
			+ " LEFT JOIN promo.organization org "
			+ " where org.id = :orgId "
			+ " AND promo.typeId not in :typeIds "
			+ " AND now() between promo.dateStart and promo.dateEnd"
			+ " AND (promo.code is null OR LOWER(promo.code) = :promoCode)"
			+ " AND promo.status = 1"
			+ " order by promo.priority desc")
	List<PromotionsEntity> findByOrganization_IdAndTypeIdNotIn(@Param("orgId") Long orgId,
															   @Param("typeIds") List<Integer> typeIds,
															   @Param("promoCode") String promoCode);

	@Query("select promo "
			+ " FROM PromotionsEntity promo "
			+ " where promo.typeId in :typeIds "
			+ " AND now() between promo.dateStart and promo.dateEnd"
			+ " AND promo.status = 1"
			+ " order by promo.priority desc")
	List<PromotionsEntity> findActivePromosByTypeIdIn(@Param("typeIds") Collection<Integer> typeIds);

	@Query("select promo "
			+ " FROM PromotionsEntity promo "
			+ " WHERE promo.organization.id in :orgIds"
			+ " AND promo.typeId in :typeIds "
			+ " AND now() between promo.dateStart and promo.dateEnd"
			+ " AND promo.status = 1"
			+ " order by promo.priority desc")
	List<PromotionsEntity> findActivePromosByOrgIdInAndTypeIdIn(@Param("orgIds") Collection<Long> orgIds,
			@Param("typeIds") Collection<Integer> typeIds);

	@Query("select promo "
			+ " FROM PromotionsEntity promo "
			+ " where promo.organization.id in :orgIds "
			+ " AND now() between promo.dateStart and promo.dateEnd"
			+ " AND promo.status = 1"
			+ " order by promo.priority desc")
	List<PromotionsEntity> findActivePromosByOrgIdIn(@Param("orgIds") Collection<Long> orgIds);

	@Query("select promo "
		   + " FROM PromotionsEntity promo "
	       + " where promo.id in :promoIds"
	       + " AND promo.organization.id = :orgId"
	       + " AND now() between promo.dateStart and promo.dateEnd"
	       + " AND promo.status = 1")
	List<PromotionsEntity> findByIdsAndOrgId(@Param("promoIds") List<Long> promoIds, @Param("orgId") Long orgId);

	@Query("select promo "
	      + " FROM PromotionsEntity promo "
	      + " WHERE now() between promo.dateStart and promo.dateEnd"
	      + " AND promo.status = 1")
	List<PromotionsEntity> findAllActivePromotions();

	@Query("select promo"
	+ " FROM PromotionsEntity promo"
	+ " WHERE promo.id in :promoIds"
	+ " AND now() between promo.dateStart and promo.dateEnd"
	+ " AND promo.status = 1")
	List<PromotionsEntity> findActivePromosByIds(@Param("promoIds") Collection<Long> promoIds);



}