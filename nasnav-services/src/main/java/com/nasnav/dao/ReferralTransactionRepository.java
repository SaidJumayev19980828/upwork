package com.nasnav.dao;

import com.nasnav.persistence.ReferralWallet;
import com.nasnav.persistence.ReferralTransactions;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ReferralTransactionRepository extends JpaRepository<ReferralTransactions, Long> {

    List<ReferralTransactions> findByReferralWallet_Id(Long referralWalletId);

    List<ReferralTransactions> findByOrderId(Long orderId);

    @Query("SELECT SUM(referralTransactions.amount) FROM ReferralTransactions referralTransactions " +
            "WHERE referralTransactions.type = :type" +
            " AND referralTransactions.user.id = :userId")
    BigDecimal sumAmountByTypeAndUser_Id(@Param("type") String type, @Param("userId") Long userId);

}