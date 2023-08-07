package com.nasnav.service.impl;

import com.nasnav.dao.BankOutsideTransactionRepository;
import com.nasnav.dto.request.BankOutsideTransactionValidDTO;
import com.nasnav.dto.request.OutsideTransactionValidatorDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BankAccountEntity;
import com.nasnav.persistence.BankOutsideTransactionEntity;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.service.BankAccountActivityService;
import com.nasnav.service.BankAccountService;
import com.nasnav.service.BankOutsideTransactionService;
import com.nasnav.service.SecurityService;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static com.nasnav.exceptions.ErrorCodes.BANK$ACC$0005;

@Service
@AllArgsConstructor
public class BankOutsideTransactionServiceImpl implements BankOutsideTransactionService {
    private final BankOutsideTransactionRepository outsideTransactionRepository;
    private final SecurityService securityService;
    private final BankAccountActivityService bankAccountActivityService;
    private final BankAccountService bankAccountService;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public void depositOrWithdrawal(long amount, boolean isDeposit, String transactionIdOfBC) {
        BankAccountEntity bankAccountEntity = bankAccountService.getLoggedAccount();

        if(!validateDepositOrWithdrawalIsDone(transactionIdOfBC, amount)){
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
    public Boolean validateDepositOrWithdrawalIsDone(String hashBC, long amount) {
        OutsideTransactionValidatorDTO dto = new OutsideTransactionValidatorDTO(hashBC, amount);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<OutsideTransactionValidatorDTO> entity = new HttpEntity<>(dto, headers);
        BankOutsideTransactionValidDTO response = restTemplate.exchange("https://meetusvr-blockchain-api.herokuapp.com/api/tokens/verify", HttpMethod.POST, entity, BankOutsideTransactionValidDTO.class).getBody();
        return response.getData();
    }
}
