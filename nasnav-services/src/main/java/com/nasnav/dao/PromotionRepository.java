package com.nasnav.dao;

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
			@Param("code")String code
			, @Param("orgId")Long orgId
			, @Param("id")Long id);

	
	@Query("SELECT COUNT(promo) > 0 "
			+ " FROM PromotionsEntity promo "
			+ " LEFT JOIN promo.organization org "
			+ " WHERE UPPER(promo.code) = UPPER(:code) "
			+ " AND org.id = :orgId"
			+ " AND now() between promo.dateStart and promo.dateEnd ")
	boolean existsByCodeAndOrganization_IdAndActiveNow(
			@Param("code")String code
			,@Param("orgId") Long orgId);
	
	
	@Query("SELECT promo "
			+ " FROM PromotionsEntity promo "
			+ " LEFT JOIN promo.organization org "
			+ " WHERE UPPER(promo.code) = UPPER(:code) "
			+ " AND org.id = :orgId"
			+ " AND now() between promo.dateStart and promo.dateEnd ")
	Optional<PromotionsEntity> findByCodeAndOrganization_IdAndActiveNow(
			@Param("code")String code
			,@Param("orgId") Long orgId);

    Optional<PromotionsEntity> findByIdAndOrganization_Id(Long promotionId, Long orgId);

	@Query("select promo "
			+ " FROM PromotionsEntity promo "
			+ " LEFT JOIN promo.organization org "
			+ " where org.id = :orgId "
			+ " AND promo.typeId in :typeIds "
			+ " AND now() between promo.dateStart and promo.dateEnd"
			+ " order by priority desc")
	List<PromotionsEntity> findByOrganization_IdAndTypeIdIn(@Param("orgId") Long orgId,
															@Param("typeIds") List<Integer> typeIds);
}
