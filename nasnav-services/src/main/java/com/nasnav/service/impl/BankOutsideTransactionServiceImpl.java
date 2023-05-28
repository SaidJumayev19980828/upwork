package com.nasnav.service.impl;

import com.nasnav.dao.BankAccountRepository;
import com.nasnav.dao.BankOutsideTransactionRepository;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BankAccountEntity;
import com.nasnav.persistence.BankOutsideTransactionEntity;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.service.BankOutsideTransactionService;
import com.nasnav.service.SecurityService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.nasnav.exceptions.ErrorCodes.BANK$ACC$0002;
import static com.nasnav.exceptions.ErrorCodes.BANK$ACC$0004;

@Service
@AllArgsConstructor
public class BankOutsideTransactionServiceImpl implements BankOutsideTransactionService {
    private final BankOutsideTransactionRepository outsideTransactionRepository;
    private final SecurityService securityService;
    private final BankAccountRepository bankAccountRepository;

    @Override
    public void depositOrWithdrawal(long amount, boolean isDeposit, long transactionIdOfBC) {
        BaseUserEntity loggedInUser = securityService.getCurrentUser();

        if(!bankAccountRepository.existsByUser_IdOrOrganization_Id(loggedInUser.getId(), loggedInUser.getOrganizationId())){
            throw new RuntimeBusinessException(HttpStatus.NOT_FOUND,BANK$ACC$0002);
        }

        if(!validateDepositOrWithdrawalIsDone(transactionIdOfBC)){
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE,BANK$ACC$0004);
        }

        BankAccountEntity bankAccountEntity = bankAccountRepository.getByUser_IdOrOrganization_Id(loggedInUser.getId(), loggedInUser.getOrganizationId());

        BankOutsideTransactionEntity entity = new BankOutsideTransactionEntity();
        entity.setAccount(bankAccountEntity);
        entity.setActivityDate(LocalDateTime.now());
        if (isDeposit) {
            entity.setAmountIn(amount);
        } else {
            entity.setAmountOut(amount);
        }

        outsideTransactionRepository.save(entity);
    }

    @Override
    public Boolean validateDepositOrWithdrawalIsDone(long depositOrWithdrawalId) {
        //TODO connect to BC to make sure it is correct
        //return true if ok
        return false;
    }
}
