package com.nasnav.dao;

import com.nasnav.persistence.ReferralCodeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ReferralCodeRepo extends JpaRepository<ReferralCodeEntity, Long> {
        Optional<ReferralCodeEntity> findByReferralCodeAndOrganization_id(String referralCode, Long organizationId);
        Optional<ReferralCodeEntity> findByReferralCode(String referralCode);
        Optional<ReferralCodeEntity> findByReferralCodeAndStatus(String referralCode, Integer status);

        Optional<ReferralCodeEntity> findByIdAndOrganization_Id(Long id, Long OrganizationId);
        Page<ReferralCodeEntity> findAllByOrganization_id(Long organizationId, Pageable pageable);

        Optional<ReferralCodeEntity> findByAcceptReferralToken(String acceptToken);
        boolean existsByAcceptReferralToken(String acceptReferralCode);

        boolean existsByReferralCode(String referralCode);

}
