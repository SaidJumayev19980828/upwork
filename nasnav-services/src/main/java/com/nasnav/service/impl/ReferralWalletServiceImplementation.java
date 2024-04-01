package com.nasnav.service.impl;

import com.nasnav.commons.utils.CustomPaginationPageRequest;
import com.nasnav.dao.ReferralWalletRepository;
import com.nasnav.dao.ReferralTransactionRepository;
import com.nasnav.enumerations.ReferralTransactionsType;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.ReferralCodeEntity;
import com.nasnav.persistence.ReferralWallet;
import com.nasnav.persistence.ReferralTransactions;
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
    private final ReferralTransactionRepository referralTransactionRepository;

    @Override
    public ReferralWallet create(ReferralCodeEntity referralCode, BigDecimal openingBalance) {
        ReferralWallet referralWallet = new ReferralWallet();
        referralWallet.setUser(referralCode.getUser());
        referralWallet.depositBalance(openingBalance);
        ReferralTransactions referralTransactions = buildDepositTransaction(referralCode.getUser(), openingBalance, null, referralCode, referralWallet, ReferralTransactionsType.ACCEPT_REFERRAL_CODE);
        referralTransactionRepository.save(referralTransactions);
        return referralWallet;
    }

    @Override
    public ReferralWallet getWalletByUserId(Long userId) {
        return referralWalletRepository.findByUserId(userId).orElseThrow(
                ()-> new RuntimeBusinessException(HttpStatus.NOT_FOUND,$001$REFERRAL$,userId));
    }

    @Override
    public PageImpl<ReferralWallet> getAll(int start, int count) {
        Pageable page = new CustomPaginationPageRequest(start, count);
        return referralWalletRepository.findAll(page);
    }

    @Override
    public ReferralWallet deposit(Long orderId, BigDecimal amount, ReferralCodeEntity parentReferralCode, ReferralCodeEntity referralCode, ReferralTransactionsType type) {
        ReferralWallet wallet = getWalletByUserId(parentReferralCode.getUser().getId());
        wallet.depositBalance(amount);
        referralTransactionRepository.save(buildDepositTransaction(parentReferralCode.getUser(), amount, orderId, referralCode, wallet, type));
        return wallet;
    }

    @Override
    public ReferralWallet deposit(Long orderId, BigDecimal amount, UserEntity user, ReferralTransactionsType type) {
        ReferralWallet wallet = getWalletByUserId(user.getId());
        wallet.depositBalance(amount);
        referralTransactionRepository.save(buildDepositTransaction(user, amount, orderId, null, wallet, type));
        return wallet;
    }

    @Override
    public ReferralWallet withdraw(UserEntity user, Long orderId, BigDecimal amount, ReferralTransactionsType type)  {
        ReferralWallet wallet = getWalletByUserId(user.getId());
        validateWithdraw(amount, wallet.getBalance());
        wallet.withdrawBalance(amount);
        ReferralTransactions referralTransactions = buildDepositTransaction(user, amount, orderId, null, wallet,type);
        referralTransactionRepository.save(referralTransactions);
        return referralWalletRepository.save(wallet);
    }

    @Override
    public Long addReferralTransaction(UserEntity user, BigDecimal amount, Long orderId, ReferralCodeEntity referralCodeEntity, ReferralTransactionsType transactionType, boolean description) {
        return referralTransactionRepository.save(buildDepositTransaction(user, amount, orderId, referralCodeEntity, null,transactionType)).getId();
    }


    private ReferralTransactions buildDepositTransaction(UserEntity user, BigDecimal amount, Long orderId, ReferralCodeEntity referralCodeEntity, ReferralWallet referralWallet, ReferralTransactionsType transactionType) {
        ReferralTransactions transaction = new ReferralTransactions();
        transaction.setAmount(amount);
        transaction.setUser(user);
        transaction.setOrderId(orderId);
        transaction.setType(transactionType);
        transaction.setReferralCodeEntity(referralCodeEntity);
        transaction.setReferralWallet(referralWallet);
        return transaction;
    }



    private void validateWithdraw(BigDecimal amount, BigDecimal balance) {
        if (amount.compareTo(balance) > 0) {
            throw new RuntimeBusinessException(HttpStatus.FORBIDDEN,BANK$TRANS$0001);
        }
    }
}
