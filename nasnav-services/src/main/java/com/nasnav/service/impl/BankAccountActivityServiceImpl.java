package com.nasnav.service.impl;

import com.nasnav.dao.BankAccountActivityRepository;
import com.nasnav.dao.BankAccountRepository;
import com.nasnav.dao.BankReservationRepository;
import com.nasnav.dto.response.BankActivityDTO;
import com.nasnav.dto.response.BankActivityDetailsDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BankAccountActivityEntity;
import com.nasnav.persistence.BankAccountEntity;
import com.nasnav.persistence.BankInsideTransactionEntity;
import com.nasnav.persistence.BankOutsideTransactionEntity;
import com.nasnav.service.BankAccountActivityService;
import com.nasnav.service.OrganizationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.nasnav.exceptions.ErrorCodes.BANK$ACC$0003;

@Service
@AllArgsConstructor
public class BankAccountActivityServiceImpl implements BankAccountActivityService {
    private final BankAccountRepository bankAccountRepository;
    private final BankAccountActivityRepository bankAccountActivityRepository;
    private final BankReservationRepository bankReservationRepository;
    private final OrganizationService organizationService;

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
    public BankActivityDTO getHistory(long accountId) {
        List<BankAccountActivityEntity> list = bankAccountActivityRepository.findAllByAccount_Id(accountId);
        if(list.size() > 0){
            BankAccountEntity accountEntity = list.get(0).getAccount();
            BankActivityDTO dto = new BankActivityDTO();
            dto.setUser(accountEntity.getUser().getRepresentation());
            dto.setOrg(organizationService.getOrganizationById(accountEntity.getOrganization().getId(), 0));
            dto.setId(accountEntity.getId());
            dto.setWallerAddress(accountEntity.getWalletAddress());
            dto.setHistory(list.stream().map(this::toDto).collect(Collectors.toList()));
            dto.getSummary().setTotalBalance(this.getTotalBalance(accountEntity.getId()));
            dto.getSummary().setAvailableBalance(this.getAvailableBalance(accountEntity.getId()));
            dto.getSummary().setReservedBalance(this.getReservedBalance(accountEntity.getId()));
            return dto;
        }
        return new BankActivityDTO();
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
}
