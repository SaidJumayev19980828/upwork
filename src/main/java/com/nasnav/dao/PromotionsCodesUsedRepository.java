package com.nasnav.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.persistence.PromotionsCodesUsedEntity;
import com.nasnav.persistence.PromotionsEntity;
import com.nasnav.persistence.UserEntity;

public interface PromotionsCodesUsedRepository
				extends JpaRepository<PromotionsCodesUsedEntity, Long>{
	boolean existsByPromotionAndUser(PromotionsEntity promotion,UserEntity user);

	
	@Modifying
    @Transactional
	@Query(value = 
			"DELETE FROM PromotionsCodesUsedEntity used "
			+ " WHERE used in "
			+ " (SELECT u FROM PromotionsCodesUsedEntity u "
			+ "   LEFT JOIN u.promotion promo "
			+ "   LEFT JOIN u.user usr"
			+ "   WHERE promo.id = :promoId "
			+ "   AND usr.id = :userId)")
	void deleteByPromotion_IdAndUser_Id(@Param("promoId")Long promoId, @Param("userId")Long userId);


	boolean existsByPromotion_IdAndUser_Id(Long promoId, Long userId);
}
