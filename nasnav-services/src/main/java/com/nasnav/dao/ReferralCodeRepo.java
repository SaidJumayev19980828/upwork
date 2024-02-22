package com.nasnav.dao;

import com.nasnav.persistence.ReferralCodeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ReferralCodeRepo extends JpaRepository<ReferralCodeEntity, Long> {
        Optional<ReferralCodeEntity> findByReferralCodeAndOrganization_id(String referralCode, Long organizationId);
        Optional<ReferralCodeEntity> findByReferralCode(String referralCode);
        Optional<ReferralCodeEntity> findByUser_IdAndOrganization_Id(Long userId, Long organizationId);

        boolean existsByUser_IdAndOrganization_Id(Long userId, Long organizationId);

        Optional<ReferralCodeEntity> findByReferralCodeAndStatus(String referralCode, Integer status);

        Optional<ReferralCodeEntity> findByIdAndOrganization_Id(Long id, Long OrganizationId);
        Page<ReferralCodeEntity> findAllByOrganization_id(Long organizationId, Pageable pageable);

        Optional<ReferralCodeEntity> findByAcceptReferralToken(String acceptToken);
        boolean existsByAcceptReferralToken(String acceptReferralCode);

        boolean existsByReferralCode(String referralCode);

        @Query(value = "SELECT count(*) from referral_codes refCode1 " +
                "JOIN referral_codes refCode2 " +
                "ON refCode1.referral_code = refCode2.parent_referral_code" +
                "where refCode1.user_id = :userId and refCode2.status = :status",
                nativeQuery = true)
        Long countChildReferralCodesByUserIdAndIsActive(Long userId, int status);

}
