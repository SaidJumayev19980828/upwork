package com.nasnav.dao;

import com.nasnav.persistence.ReferralCodeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReferralCodeRepo extends JpaRepository<ReferralCodeEntity, Long> {
        Optional<ReferralCodeEntity> findByReferralCodeAndOrganization_Id(String referralCode, Long OrganizationId);

        Optional<ReferralCodeEntity> findByIdAndOrganization_Id(Long id, Long OrganizationId);
        Page<ReferralCodeEntity> findAllByOrganization_id(Long organizationId, Pageable pageable);
}
