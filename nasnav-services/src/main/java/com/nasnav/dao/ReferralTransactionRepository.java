package com.nasnav.dao;

import com.nasnav.persistence.ReferralWallet;
import com.nasnav.persistence.ReferralTransactions;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReferralTransactionRepository extends JpaRepository<ReferralTransactions, Long> {

    List<ReferralTransactions> findByReferralWallet_Id(Long referralWalletId);

    List<ReferralTransactions> findByOrderId(Long orderId);
}