package com.nasnav.dao;

import com.nasnav.persistence.ReferralWallet;
import com.nasnav.persistence.ReferralWalletTransaction;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferralWalletTransactionRepository extends JpaRepository<ReferralWalletTransaction, Long> {

    PageImpl<ReferralWalletTransaction> findByReferralWallet(ReferralWallet referral, Pageable pageable);
}