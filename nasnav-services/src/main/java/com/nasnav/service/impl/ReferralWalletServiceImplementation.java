package com.nasnav.service.impl;

import com.nasnav.commons.utils.CustomOffsetAndLimitPageRequest;
import com.nasnav.dao.ReferralWalletRepository;
import com.nasnav.dao.ReferralWalletTransactionRepository;
import com.nasnav.enumerations.WalletTransactions;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.ReferralWallet;
import com.nasnav.persistence.ReferralWalletTransaction;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.ReferralWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.nasnav.exceptions.ErrorCodes.$001$REFERRAL$;
import static com.nasnav.exceptions.ErrorCodes.BANK$TRANS$0001;

@Service
@RequiredArgsConstructor
public class ReferralWalletServiceImplementation implements ReferralWalletService {

    private final ReferralWalletRepository referralWalletRepository;
    private final ReferralWalletTransactionRepository referralWalletTransactionRepository;
    @Override
    public ReferralWallet create(UserEntity user, BigDecimal openingBalance) {
        ReferralWallet referralWallet = new ReferralWallet();
        referralWallet.setUser(user);
        referralWallet.depositBalance(openingBalance);
        referralWallet.addTransactions(buildDepositTransaction(openingBalance,WalletTransactions.DEPOSIT,true));
        return referralWalletRepository.save(referralWallet);
    }

    @Override
    public ReferralWallet getWalletByUser(UserEntity user) {
        return referralWalletRepository.findByUserId(user).orElseThrow(()-> new RuntimeBusinessException(HttpStatus.NOT_FOUND,$001$REFERRAL$,user.getId()));
    }

    @Override
    public PageImpl<ReferralWallet> getAll(int start, int count) {
        Pageable page = new CustomOffsetAndLimitPageRequest(start, count);
        return referralWalletRepository.findAll(page);
    }

    @Override
    public PageImpl<ReferralWalletTransaction> getTransactions(UserEntity user, int start, int count) {
        Pageable page = new CustomOffsetAndLimitPageRequest(start, count);
        return referralWalletTransactionRepository.findByReferralWallet(getWalletByUser(user),page);
    }

    @Override
    public ReferralWallet deposit(UserEntity user, BigDecimal amount) {
        ReferralWallet wallet = getWalletByUser(user);
        wallet.depositBalance(amount);
        wallet.addTransactions(buildDepositTransaction(amount, WalletTransactions.DEPOSIT, false));
        return referralWalletRepository.save(wallet);
    }

    @Override
    public ReferralWallet withdraw(UserEntity user, BigDecimal amount) {
        ReferralWallet wallet = getWalletByUser(user);
        validateWithdraw(amount, wallet.getBalance());
        wallet.withdrawBalance(amount);
        wallet.addTransactions(buildDepositTransaction(amount, WalletTransactions.WITHDRAWAL, false));
        return referralWalletRepository.save(wallet);
    }


    private ReferralWalletTransaction buildDepositTransaction(BigDecimal amount, WalletTransactions transactionType, boolean openingBalance) {
        ReferralWalletTransaction transaction = new ReferralWalletTransaction();
        transaction.setAmount(amount);
        transaction.setType(transactionType);
        transaction.setDescription(openingBalance);
        return transaction;
    }


    private void validateWithdraw(BigDecimal amount, BigDecimal balance) {
        if (amount.compareTo(balance) > 0) {
            throw new RuntimeBusinessException(HttpStatus.FORBIDDEN,BANK$TRANS$0001);
        }
    }
}
