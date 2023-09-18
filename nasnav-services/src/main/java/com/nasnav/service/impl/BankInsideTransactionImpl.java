package com.nasnav.service.impl;

import com.nasnav.AppConfig;
import com.nasnav.dao.BankAccountRepository;
import com.nasnav.dao.BankInsideTransactionRepository;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BankAccountEntity;
import com.nasnav.persistence.BankInsideTransactionEntity;
import com.nasnav.service.BankAccountActivityService;
import com.nasnav.service.BankAccountService;
import com.nasnav.service.BankInsideTransactionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.nasnav.exceptions.ErrorCodes.BANK$ACC$0002;
import static com.nasnav.exceptions.ErrorCodes.BANK$ACC$0005;

@Service
@AllArgsConstructor
public class BankInsideTransactionImpl implements BankInsideTransactionService {

    private final BankAccountRepository bankAccountRepository;
    private final BankInsideTransactionRepository bankInsideTransactionRepository;
    private final BankAccountActivityService bankAccountActivityService;
    private final BankAccountService bankAccountService;
    private final AppConfig appConfig;

    @Override
    public void transfer(long receiverAccountId, float amount) {
        BankAccountEntity sender = bankAccountService.getLoggedAccount();
        BankAccountEntity receiver = bankAccountRepository.findById(receiverAccountId).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,BANK$ACC$0002));
        this.transferImpl(sender, receiver, amount);
    }

    @Override
    public void pay(float amount) {
        BankAccountEntity sender = bankAccountService.getLoggedAccount();
        BankAccountEntity receiver = bankAccountRepository.findById(appConfig.nasnavOrgId).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,BANK$ACC$0002));
        this.transferImpl(sender, receiver, amount);
    }

    @Override
    @Transactional
    public void transferImpl(BankAccountEntity sender, BankAccountEntity receiver, float amount) {
        if(!bankAccountActivityService.checkAvailableBalance(sender.getId(), amount)) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE,BANK$ACC$0005);
        }

        BankInsideTransactionEntity entity = new BankInsideTransactionEntity();
        entity.setSender(sender);
        entity.setReceiver(receiver);
        entity.setActivityDate(LocalDateTime.now());
        entity.setAmount(amount);

        bankInsideTransactionRepository.save(entity);
        //insert two records into account activity
        bankAccountActivityService.addActivity(sender, amount, false, entity, null);
        bankAccountActivityService.addActivity(receiver, amount, true, entity, null);
    }

}
