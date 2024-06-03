package com.nasnav.dao;

import com.nasnav.enumerations.ReferralType;
import com.nasnav.persistence.ReferralWallet;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReferralWalletRepository extends JpaRepository<ReferralWallet, Long> {
    Optional<ReferralWallet> findByUserIdAndReferralType(Long userId, ReferralType referralType);

    PageImpl<ReferralWallet> findAllByReferralType(ReferralType referralType, Pageable page);

}