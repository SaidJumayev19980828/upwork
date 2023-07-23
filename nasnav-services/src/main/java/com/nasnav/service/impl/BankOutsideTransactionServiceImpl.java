package com.nasnav.service.impl;

import com.nasnav.dao.BankAccountActivityRepository;
import com.nasnav.dao.BankAccountRepository;
import com.nasnav.dao.BankOutsideTransactionRepository;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.service.BankAccountActivityService;
import com.nasnav.service.BankAccountService;
import com.nasnav.service.BankOutsideTransactionService;
import com.nasnav.service.SecurityService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.nasnav.exceptions.ErrorCodes.*;

@Service
@AllArgsConstructor
public class BankOutsideTransactionServiceImpl implements BankOutsideTransactionService {
    private final BankOutsideTransactionRepository outsideTransactionRepository;
    private final SecurityService securityService;
    private final BankAccountRepository bankAccountRepository;
    private final BankAccountActivityService bankAccountActivityService;
    private final BankAccountService bankAccountService;

    @Override
    @Transactional
    public void depositOrWithdrawal(long amount, boolean isDeposit, long transactionIdOfBC) {
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        BankAccountEntity bankAccountEntity = bankAccountService.getLoggedAccount();

        if(!validateDepositOrWithdrawalIsDone(transactionIdOfBC)){
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE,BANK$ACC$0005);
        }

        BankOutsideTransactionEntity entity = new BankOutsideTransactionEntity();
        entity.setAccount(bankAccountEntity);
        entity.setActivityDate(LocalDateTime.now());
        if (isDeposit) {
            entity.setAmountIn(amount);
            entity.setAmountOut(0L);
        } else {
            entity.setAmountIn(0L);
            entity.setAmountOut(amount);
        }
        outsideTransactionRepository.save(entity);

        bankAccountActivityService.addActivity(bankAccountEntity, amount, isDeposit, null, entity);
    }

    @Override
    public Boolean validateDepositOrWithdrawalIsDone(long depositOrWithdrawalId) {
        //TODO connect to BC to make sure it is correct
        //return true if ok
        return true;
    }
}
