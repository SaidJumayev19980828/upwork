package com.nasnav.dao;

import com.nasnav.persistence.LoyaltyGiftEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface LoyaltyGiftRepository extends JpaRepository<LoyaltyGiftEntity, Long> {

    List<LoyaltyGiftEntity> getByUserFrom_Id(Long userId);
    List<LoyaltyGiftEntity> getByUserFrom_IdAndIsRedeemFalse(Long userId);
    List<LoyaltyGiftEntity> getByUserTo_IdAndIsRedeemTrue(Long userId);

    Optional<LoyaltyGiftEntity> findByEmail(String email);

    @Transactional
    @Modifying
    void deleteByEmail(String email);
}
