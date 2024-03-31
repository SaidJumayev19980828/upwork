package com.nasnav.service;

import com.nasnav.enumerations.ReferralTransactionsType;
import com.nasnav.persistence.ReferralCodeEntity;
import com.nasnav.persistence.ReferralWallet;
import com.nasnav.persistence.ReferralTransactions;
import com.nasnav.persistence.UserEntity;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;

public interface ReferralWalletService {

    ReferralWallet create(ReferralCodeEntity referralCode, BigDecimal openingBalance);

    ReferralWallet getWalletByUserId(Long userId);

    PageImpl<ReferralWallet> getAll(int start, int count);

    ReferralWallet deposit(Long orderId, BigDecimal amount, ReferralCodeEntity parentReferralCode, ReferralCodeEntity referralCode,ReferralTransactionsType type);

    ReferralWallet deposit(Long orderId, BigDecimal amount, UserEntity user, ReferralTransactionsType type);

    ReferralWallet withdraw(UserEntity user, Long orderId, BigDecimal amount, ReferralTransactionsType type);

    Long addReferralTransaction(UserEntity user, BigDecimal amount, Long orderId, ReferralCodeEntity referralCodeEntity, ReferralTransactionsType transactionType, boolean description);


}
