package com.nasnav.service;

import com.nasnav.persistence.ReferralWallet;
import com.nasnav.persistence.ReferralWalletTransaction;
import com.nasnav.persistence.UserEntity;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;

public interface ReferralWalletService {

    ReferralWallet create(UserEntity user, BigDecimal openingBalance);

    ReferralWallet getWalletByUser(UserEntity user);

    PageImpl<ReferralWallet> getAll(int start, int count);

    PageImpl<ReferralWalletTransaction> getTransactions(UserEntity user, int start, int count);

    ReferralWallet deposit(UserEntity user, BigDecimal amount);

    ReferralWallet withdraw(UserEntity user, BigDecimal amount);



}
