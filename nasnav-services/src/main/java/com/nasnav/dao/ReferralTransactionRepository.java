package com.nasnav.dao;

import com.nasnav.enumerations.ReferralTransactionsType;
import com.nasnav.persistence.ReferralWallet;
import com.nasnav.persistence.ReferralTransactions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ReferralTransactionRepository extends JpaRepository<ReferralTransactions, Long> {

    List<ReferralTransactions> findByReferralWallet_Id(Long referralWalletId);

    List<ReferralTransactions> findByOrderId(Long orderId);

    @Query("SELECT SUM(referralTransactions.amount) FROM ReferralTransactions referralTransactions " +
            "WHERE referralTransactions.type = :type" +
            " AND referralTransactions.user.id = :userId")
    BigDecimal sumAmountByTypeAndUser_Id(@Param("type") ReferralTransactionsType type, @Param("userId") Long userId);

    @Query(value = "SElECT refTrans FROM ReferralTransactions refTrans " +
            "JOIN ReferralCodeEntity refCode1 on refTrans.referralCodeEntity.id = refCode1.id " +
            "JOIN ReferralCodeEntity refCode2 on refCode1.parentReferralCode = refCode2.referralCode " +
            "where refCode2.user.id = :userId and refTrans.type = :type ")
    Page<ReferralTransactions> getChildsReferralsByTransactionType(Long userId, ReferralTransactionsType type, Pageable pageable);

    @Query(value = "SElECT refTrans FROM ReferralTransactions refTrans " +
            "JOIN ReferralCodeEntity refCode1 on refTrans.referralCodeEntity.id = refCode1.id " +
            "JOIN ReferralCodeEntity refCode2 on refCode1.parentReferralCode = refCode2.referralCode " +
            "where refCode2.user.id = :userId and refTrans.type = :type and refTrans.createdAt BETWEEN :dateFrom and :dateTo")
    Page<ReferralTransactions> getChildsReferralsByTransactionType(Long userId, ReferralTransactionsType type, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable);
}