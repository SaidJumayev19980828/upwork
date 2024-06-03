package com.nasnav.dao;

import com.nasnav.enumerations.ReferralType;
import com.nasnav.persistence.ReferralCodeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReferralCodeRepo extends JpaRepository<ReferralCodeEntity, Long> {
        Optional<ReferralCodeEntity> findByReferralCodeAndReferralTypeAndOrganizationId(String referralCode, ReferralType referralType, Long organizationId);
        Optional<ReferralCodeEntity> findByReferralCodeAndReferralType(String referralCode, ReferralType referralType);
        Optional<ReferralCodeEntity> findByUserIdAndReferralTypeAndOrganizationId(Long userId, ReferralType referralType, Long organizationId);
        Optional<ReferralCodeEntity> findByUserIdAndReferralTypeAndOrganizationIdAndStatus(Long userId, ReferralType referralType, Long organizationId, Integer status);

        boolean existsByUserIdAndReferralTypeAndOrganizationIdAndStatus(Long userId, ReferralType referralType, Long organizationId, Integer status);

        Optional<ReferralCodeEntity> findByReferralCodeAndReferralTypeAndStatus(String referralCode, ReferralType referralType, Integer status);

        Page<ReferralCodeEntity> findAllByReferralTypeAndOrganizationId(ReferralType referralType, Long organizationId, Pageable pageable);

        boolean existsByAcceptReferralToken(String acceptReferralCode);

        boolean existsByReferralCodeAndReferralType(String referralCode, ReferralType referralType);

        @Query(value = "SELECT count(*) FROM referral_codes refCode1 " +
                "JOIN referral_codes refCode2 " +
                "ON refCode1.referral_code = refCode2.parent_referral_code " +
                "where refCode1.user_id = :userId " +
                "AND refCode1.referral_type = :referralType " +
                "AND refCode2.status = :status",
                nativeQuery = true)
        Long countChildReferralCodesByUserIdAndReferralTypeAndIsActive(Long userId, String referralType, int status);

        boolean existsByPhoneNumber(String phoneNumber);

}
