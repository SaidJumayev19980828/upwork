package com.nasnav.dao;

import com.nasnav.enumerations.ReferralTransactionsType;
import com.nasnav.enumerations.ReferralType;
import com.nasnav.persistence.ReferralTransactions;
import org.springframework.data.domain.Page;
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
            " AND referralTransactions.userId = :userId" +
            " AND referralTransactions.referralType = :referralType")
    BigDecimal sumAmountByTypeAndUserIdAndReferralType(@Param("type") ReferralTransactionsType type, @Param("userId") Long userId, ReferralType referralType);

    @Query(value = "SElECT refTrans FROM ReferralTransactions refTrans " +
            "JOIN ReferralCodeEntity refCode1 on refTrans.referralCodeEntity.id = refCode1.id " +
            "JOIN ReferralCodeEntity refCode2 on refCode1.parentReferralCode = refCode2.referralCode " +
            "where refCode2.userId = :userId and refTrans.type = :type" +
            " AND refCode1.referralType = :referralType ")
    Page<ReferralTransactions> getChildsReferralsByTransactionTypeAndReferralType(Long userId, ReferralTransactionsType type, ReferralType referralType, Pageable pageable);

    @Query(value = "SElECT refTrans FROM ReferralTransactions refTrans " +
            "JOIN ReferralCodeEntity refCode1 on refTrans.referralCodeEntity.id = refCode1.id " +
            "JOIN ReferralCodeEntity refCode2 on refCode1.parentReferralCode = refCode2.referralCode " +
            "where refCode2.userId = :userId and refTrans.type = :type" +
            " AND refCode1.referralType = :referralType" +
            " AND refTrans.createdAt BETWEEN :dateFrom and :dateTo")
    Page<ReferralTransactions> getChildsReferralsByTransactionTypeAndReferralType(Long userId, ReferralTransactionsType type, ReferralType referralType, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable);
}