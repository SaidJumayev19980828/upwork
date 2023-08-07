package com.nasnav.service.impl;

import com.nasnav.dao.BankAccountActivityRepository;
import com.nasnav.dao.BankAccountRepository;
import com.nasnav.dao.BankReservationRepository;
import com.nasnav.dto.response.BankActivityDetailsDTO;
import com.nasnav.dto.response.BankBalanceSummaryDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.service.BankAccountActivityService;
import com.nasnav.service.SecurityService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.nasnav.commons.utils.PagingUtils.getQueryPage;
import static com.nasnav.exceptions.ErrorCodes.BANK$ACC$0003;

@Service
@AllArgsConstructor
public class BankAccountActivityServiceImpl implements BankAccountActivityService {
    private final BankAccountRepository bankAccountRepository;
    private final BankAccountActivityRepository bankAccountActivityRepository;
    private final BankReservationRepository bankReservationRepository;
    private final SecurityService securityService;


    @Override
    public Long getAvailableBalance(long accountId) {
        BankAccountEntity bankAccountEntity = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,BANK$ACC$0003,accountId));

        long totalBalance = this.getTotalBalance(bankAccountEntity.getId());
        long reservedBalance = this.getReservedBalance(bankAccountEntity.getId());

        return totalBalance + bankAccountEntity.getOpeningBalance() - reservedBalance;
    }

    @Override
    public Long getTotalBalance(long accountId) {
        BankAccountEntity bankAccountEntity = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,BANK$ACC$0003,accountId));

        long openingBalanceId = bankAccountEntity.getOpeningBalanceActivity() == null ? 0 : bankAccountEntity.getOpeningBalanceActivity().getId();
        Long sum = bankAccountActivityRepository.getBalance(openingBalanceId, bankAccountEntity.getId());
        return sum == null ? 0 : sum;
    }

    @Override
    public Long getReservedBalance(long accountId) {
        BankAccountEntity bankAccountEntity = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,BANK$ACC$0003,accountId));

        Long sum = bankReservationRepository.getReservedBalance(bankAccountEntity.getId());
        return sum == null ? 0 : sum;
    }

    @Override
    public Boolean checkAvailableBalance(long accountId, long amount) {
        return getAvailableBalance(accountId) > amount;
    }

    @Override
    public BankAccountActivityEntity getLastActivity(long accountId) {
        return bankAccountActivityRepository.findFirstByAccount_IdOrderByIdDesc(accountId);
    }

    @Override
    public PageImpl<BankActivityDetailsDTO> getHistory(Integer start, Integer count) {
        BankAccountEntity bankAccountEntity = this.getLoggedAccount();
        PageRequest page = getQueryPage(start, count);
        PageImpl<BankAccountActivityEntity> source = bankAccountActivityRepository.findAllByAccount_Id(bankAccountEntity.getId(), page);
        List<BankActivityDetailsDTO> dtos = source.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, source.getPageable(), source.getTotalElements());
    }

    @Override
    public BankBalanceSummaryDTO getAccountSummary() {
        long accountId = this.getLoggedAccount().getId();
        BankBalanceSummaryDTO dto = new BankBalanceSummaryDTO();
        dto.setTotalBalance(this.getTotalBalance(accountId));
        dto.setAvailableBalance(this.getAvailableBalance(accountId));
        dto.setReservedBalance(this.getReservedBalance(accountId));
        return dto;
    }

    @Override
    public void addActivity(BankAccountEntity accountEntity, long amount, boolean isDeposit, BankInsideTransactionEntity insideTransactionEntity, BankOutsideTransactionEntity outsideTransactionEntity) {
        BankAccountActivityEntity activity;
        if (isDeposit) {
            activity = BankAccountActivityEntity.builder()
                    .account(accountEntity)
                    .activityDate(LocalDateTime.now())
                    .amountIn(amount)
                    .amountOut(0L)
                    .bankInsideTransaction(insideTransactionEntity)
                    .bankOutsideTransaction(outsideTransactionEntity)
                    .build();

        } else {
            activity = BankAccountActivityEntity.builder()
                    .account(accountEntity)
                    .activityDate(LocalDateTime.now())
                    .amountIn(0L)
                    .amountOut(amount)
                    .bankInsideTransaction(insideTransactionEntity)
                    .bankOutsideTransaction(outsideTransactionEntity)
                    .build();
        }
        bankAccountActivityRepository.save(activity);
    }

    private BankActivityDetailsDTO toDto(BankAccountActivityEntity entity) {
        return BankActivityDetailsDTO.builder()
                .activityDate(entity.getActivityDate())
                .amountIn(entity.getAmountIn())
                .amountOut(entity.getAmountOut())
                .build();
    }

    private BankAccountEntity getLoggedAccount() {
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        BankAccountEntity entity;
        if(loggedInUser instanceof UserEntity){
            entity = bankAccountRepository.getByUser_Id(loggedInUser.getId());
        }
        else {
            entity = bankAccountRepository.getByOrganization_Id(loggedInUser.getOrganizationId());
        }

        if (entity == null)
            throw new RuntimeBusinessException(HttpStatus.NOT_FOUND,BANK$ACC$0003);

        return entity;
    }
}
