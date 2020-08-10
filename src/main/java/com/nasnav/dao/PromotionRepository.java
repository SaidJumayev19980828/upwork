package com.nasnav.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nasnav.persistence.PromotionsEntity;

public interface PromotionRepository extends JpaRepository<PromotionsEntity, Long> {

	
	@Query("SELECT COUNT(promo) > 0 "
			+ " FROM PromotionsEntity promo "
			+ " LEFT JOIN promo.organization org "
			+ " WHERE promo.code = :code "
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
			+ " WHERE promo.code = :code "
			+ " AND org.id = :orgId"
			+ " AND now() between promo.dateStart and promo.dateEnd ")
	boolean existsByCodeAndOrganization_IdAndActiveNow(
			@Param("code")String code
			,@Param("orgId") Long orgId);

}
