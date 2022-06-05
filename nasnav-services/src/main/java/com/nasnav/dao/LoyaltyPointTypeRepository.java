package com.nasnav.dao;

import com.nasnav.persistence.LoyaltyPointTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface LoyaltyPointTypeRepository extends JpaRepository<LoyaltyPointTypeEntity, Long> {
    Optional<LoyaltyPointTypeEntity> findByName(String name);

    @Transactional
    @Modifying
    void deleteByName(String name);
}
