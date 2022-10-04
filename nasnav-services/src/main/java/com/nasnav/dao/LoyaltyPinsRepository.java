package com.nasnav.dao;

import com.nasnav.persistence.LoyaltyPinsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoyaltyPinsRepository extends JpaRepository<LoyaltyPinsEntity, Long> {
    Optional<LoyaltyPinsEntity> findByUser_IdAndShop_IdAndPin(Long userId, Long shopId, String pinCode);

    Optional<LoyaltyPinsEntity> findByPin(String pin);
}
