package com.nasnav.dao;

import com.nasnav.persistence.LoyaltyPointConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LoyaltyPointConfigRepository extends JpaRepository<LoyaltyPointConfigEntity, Long> {

    boolean existsByIdAndOrganization_IdAndIsActive(Long id, Long orgId, Boolean isActive);

    Optional<LoyaltyPointConfigEntity> findByIdAndOrganization_IdAndIsActive(Long id, Long orgId, Boolean isActive);

    List<LoyaltyPointConfigEntity> findByOrganization_IdOrderByCreatedAtDesc(Long orgId);

    Optional<LoyaltyPointConfigEntity> findByOrganization_IdAndIsActive(@Param("orgId") Long orgId, Boolean isActive);
}
