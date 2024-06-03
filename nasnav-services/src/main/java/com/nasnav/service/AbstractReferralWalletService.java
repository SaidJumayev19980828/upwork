package com.nasnav.service;

import com.nasnav.commons.utils.CustomPaginationPageRequest;
import com.nasnav.dao.ReferralTransactionRepository;
import com.nasnav.dao.ReferralWalletRepository;
import com.nasnav.enumerations.ReferralTransactionsType;
import com.nasnav.enumerations.ReferralType;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.ReferralCodeEntity;
import com.nasnav.persistence.ReferralTransactions;
import com.nasnav.persistence.ReferralWallet;
import com.nasnav.persistence.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

import static com.nasnav.exceptions.ErrorCodes.$001$REFERRAL$;
import static com.nasnav.exceptions.ErrorCodes.BANK$TRANS$0001;

/**
 * Service For Handling the transactions of referral wallet
 * inherited by each type of referrals so that they all share the same wallet implementation but with different types
 *
 * @author Mohamed Shaker
 *
 */
public abstract class AbstractReferralWalletService {

    @Autowired
    private ReferralWalletRepository referralWalletRepository;

    @Autowired
    private ReferralTransactionRepository referralTransactionRepository;


    protected abstract ReferralType getReferralType();

    protected abstract ReferralTransactionsType getInitialTransactionCreateType();

    
    public ReferralWallet create(ReferralCodeEntity referralCode, BigDecimal openingBalance) {
        ReferralWallet referralWallet = new ReferralWallet();
        referralWallet.setUserId(referralCode.getUserId());
        referralWallet.depositBalance(openingBalance);
        referralWallet.setReferralType(getReferralType());
        ReferralTransactions referralTransactions = buildDepositTransaction(referralCode.getUserId(),
                openingBalance, null, referralCode,
                referralWallet, getInitialTransactionCreateType());
        referralTransactionRepository.save(referralTransactions);
        return referralWallet;
    }

    public ReferralWallet getWalletByUserId(Long userId) {
        return referralWalletRepository.findByUserIdAndReferralType(userId, getReferralType()).orElseThrow(
                ()-> new RuntimeBusinessException(HttpStatus.NOT_FOUND,$001$REFERRAL$,userId));
    }

    public ReferralWallet deposit(Long orderId, BigDecimal amount, ReferralCodeEntity parentReferralCode, ReferralCodeEntity referralCode, ReferralTransactionsType type) {
        ReferralWallet wallet = getWalletByUserId(parentReferralCode.getUserId());
        wallet.depositBalance(amount);
        referralTransactionRepository.save(buildDepositTransaction(parentReferralCode.getUserId(), amount, orderId, referralCode, wallet, type));
        return wallet;
    }

    public ReferralWallet deposit(Long orderId, BigDecimal amount, Long userId, ReferralTransactionsType type) {
        ReferralWallet wallet = getWalletByUserId(userId);
        wallet.depositBalance(amount);
        referralTransactionRepository.save(buildDepositTransaction(userId, amount, orderId, null, wallet, type));
        return wallet;
    }

    public ReferralWallet withdraw(UserEntity user, Long orderId, BigDecimal amount, ReferralTransactionsType type)  {
        ReferralWallet wallet = getWalletByUserId(user.getId());
        validateWithdraw(amount, wallet.getBalance());
        wallet.withdrawBalance(amount);
        ReferralTransactions referralTransactions = buildDepositTransaction(user.getId(), amount, orderId, null, wallet,type);
        referralTransactionRepository.save(referralTransactions);
        return referralWalletRepository.save(wallet);
    }

    public Long addReferralTransaction(UserEntity user, BigDecimal amount, Long orderId, ReferralCodeEntity referralCodeEntity, ReferralTransactionsType transactionType) {
        return referralTransactionRepository.save(buildDepositTransaction(user.getId(), amount, orderId, referralCodeEntity, null,transactionType)).getId();
    }

    private ReferralTransactions buildDepositTransaction(Long userId, BigDecimal amount, Long orderId, ReferralCodeEntity referralCodeEntity, ReferralWallet referralWallet, ReferralTransactionsType transactionType) {
        ReferralTransactions transaction = new ReferralTransactions();
        transaction.setAmount(amount);
        transaction.setUserId(userId);
        transaction.setOrderId(orderId);
        transaction.setType(transactionType);
        transaction.setReferralType(getReferralType());
        transaction.setReferralCodeEntity(referralCodeEntity);
        transaction.setReferralWallet(referralWallet);
        return transaction;
    }

    public void validateWithdraw(BigDecimal amount, BigDecimal balance) {
        if (amount.compareTo(balance) > 0) {
            throw new RuntimeBusinessException(HttpStatus.FORBIDDEN,BANK$TRANS$0001);
        }
    }


}
