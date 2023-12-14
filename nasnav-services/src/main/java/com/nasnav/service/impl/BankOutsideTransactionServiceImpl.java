package com.nasnav.service.impl;

import com.nasnav.dao.BankOutsideTransactionRepository;
import com.nasnav.dto.request.BankOutsideTransactionValidDTO;
import com.nasnav.dto.request.BlockChainValidator;
import com.nasnav.dto.request.OutsideTransactionValidatorDTO;
import com.nasnav.dto.response.BlockChainValidatorResponse;
import com.nasnav.dto.response.BlockChainValidatorResponseData;
import com.nasnav.dto.response.DepositBlockChainRequest;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BankAccountEntity;
import com.nasnav.persistence.BankOutsideTransactionEntity;
import com.nasnav.service.BankAccountActivityService;
import com.nasnav.service.BankAccountService;
import com.nasnav.service.BankOutsideTransactionService;
import com.nasnav.service.SecurityService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.nasnav.exceptions.ErrorCodes.BANK$ACC$0005;
import static com.nasnav.exceptions.ErrorCodes.BANK$ACC$0007;
import static com.nasnav.exceptions.ErrorCodes.BANK$ACC$0008;

@Service
@RequiredArgsConstructor
public class BankOutsideTransactionServiceImpl implements BankOutsideTransactionService {
    private final BankOutsideTransactionRepository outsideTransactionRepository;
    private final SecurityService securityService;
    private final BankAccountActivityService bankAccountActivityService;
    private final BankAccountService bankAccountService;
    private final RestTemplate restTemplate;
    private static final String BLOCK_CHAIN_API_KEY = "793F95A1-CA66-478F-8F74-BD70A0B7C9BA";

    @Override
    @Transactional
    public void depositOrWithdrawal(float amount, boolean isDeposit, String transactionIdOfBC) {
        BankAccountEntity bankAccountEntity = bankAccountService.getLoggedAccount();
        if(outsideTransactionRepository.existsByBcKey(transactionIdOfBC)){
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE,BANK$ACC$0007);
        }
        if(!validateDepositOrWithdrawalIsDone(transactionIdOfBC, amount)){
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE,BANK$ACC$0005);
        }

        BankOutsideTransactionEntity entity = new BankOutsideTransactionEntity();
        entity.setAccount(bankAccountEntity);
        entity.setActivityDate(LocalDateTime.now());
        entity.setBcKey(transactionIdOfBC);
        if (isDeposit) {
            entity.setAmountIn(amount);
            entity.setAmountOut(0F);
        } else {
            entity.setAmountIn(0F);
            entity.setAmountOut(amount);
        }
        outsideTransactionRepository.save(entity);

        bankAccountActivityService.addActivity(bankAccountEntity, amount, isDeposit, null, entity);
    }


    @Override
    @Transactional
    public void depositCoins(String txHash) {
        validateTxHash(txHash);
        BlockChainValidatorResponseData response = validateHashAndKnowTheAmount(txHash);
        float amount = Float.parseFloat(response.getTokensReceivedFormatted());
        BankAccountEntity bankAccountEntity = bankAccountService.getLoggedAccount();
        buildOutsideTransaction(txHash,bankAccountEntity,amount);
    }

    @Override
    public Boolean validateDepositOrWithdrawalIsDone(String hashBC, float amount) {
        OutsideTransactionValidatorDTO dto = new OutsideTransactionValidatorDTO(hashBC, String.valueOf(amount));
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<OutsideTransactionValidatorDTO> entity = new HttpEntity<>(dto, headers);
        BankOutsideTransactionValidDTO response = restTemplate.exchange("https://meetusvr-blockchain-api.herokuapp.com/api/tokens/verify", HttpMethod.POST, entity, BankOutsideTransactionValidDTO.class).getBody();
        assert response != null;
        return response.getData();
    }

    @Override
    public void depositCoinsFromBC(DepositBlockChainRequest request) {
        validateTxHash(request.getTxHash());
        float amount = request.getTokenAmount();
        validateApiKey(request.getApiKey());
        BankAccountEntity bankAccountEntity = bankAccountService.getAccountByWalletAddress(request.getWalletAddress());
        buildOutsideTransaction(request.getTxHash(),bankAccountEntity,amount);
    }


    private BlockChainValidatorResponseData validateHashAndKnowTheAmount(String hashBC) {
        BlockChainValidator dto = new BlockChainValidator(hashBC);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<BlockChainValidator> entity = new HttpEntity<>(dto, headers);
        BlockChainValidatorResponse response = restTemplate.exchange("https://meetusvr-blockchain-api-aaed7ef425c0.herokuapp.com/api/tokens/verify", HttpMethod.POST, entity, BlockChainValidatorResponse.class).getBody();
        assert response != null;
        return response.getData();
    }

    private void buildOutsideTransaction(String txHash , BankAccountEntity bankAccount,float amount ){
        BankOutsideTransactionEntity entity = new BankOutsideTransactionEntity();
        entity.setAccount(bankAccount);
        entity.setActivityDate(LocalDateTime.now());
        entity.setAmountIn(amount);
        entity.setAmountOut(0F);
        entity.setBcKey(txHash);
        outsideTransactionRepository.save(entity);
        bankAccountActivityService.addActivity(bankAccount, amount, true, null, entity);
    }

    private void validateTxHash(String txHash){
        if(outsideTransactionRepository.existsByBcKey(txHash)){
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE,BANK$ACC$0007);
        }
    }

    private void validateApiKey(String apiKey){
        if (!apiKey.equals(BLOCK_CHAIN_API_KEY)){
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE,BANK$ACC$0008);
        }
    }
}
