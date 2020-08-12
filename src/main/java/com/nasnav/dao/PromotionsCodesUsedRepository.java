package com.nasnav.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.PromotionsCodesUsedEntity;
import com.nasnav.persistence.PromotionsEntity;
import com.nasnav.persistence.UserEntity;

public interface PromotionsCodesUsedRepository
				extends JpaRepository<PromotionsCodesUsedEntity, Long>{
	boolean existsByPromotionAndUser(PromotionsEntity promotion,UserEntity user);
}
