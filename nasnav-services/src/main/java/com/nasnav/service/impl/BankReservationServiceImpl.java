package com.nasnav.service.impl;

import com.nasnav.dao.BankAccountRepository;
import com.nasnav.dao.BankReservationRepository;
import com.nasnav.dto.response.BankReservationDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BankAccountEntity;
import com.nasnav.persistence.BankReservationEntity;
import com.nasnav.service.BankAccountService;
import com.nasnav.service.BankReservationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.nasnav.exceptions.ErrorCodes.BANK$ACC$0003;
import static com.nasnav.exceptions.ErrorCodes.BANK$ACC$0006;

@Service
@AllArgsConstructor
public class BankReservationServiceImpl implements BankReservationService {
    private final BankReservationRepository bankReservationRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BankAccountService bankAccountService;

    @Override
    public BankReservationDTO createReservation(long accountId, float amount) {
        BankAccountEntity accountEntity = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,BANK$ACC$0003, accountId));
        BankReservationEntity entity = new BankReservationEntity();
        entity.setAccount(accountEntity);
        entity.setActivityDate(LocalDateTime.now());
        entity.setAmount(amount);
        entity.setFulfilled(false);
        return toDto(entity);
    }

    @Override
    public List<BankReservationDTO> getReservations(Boolean isFulfilled) {
        BankAccountEntity accountEntity = bankAccountService.getLoggedAccount();
        if(isFulfilled == null){
            return bankReservationRepository.getAllByAccount_Id(accountEntity.getId())
                    .stream().map(this::toDto).collect(Collectors.toList());
        }
        else {
            return bankReservationRepository.getAllByAccount_IdAndFulfilled(accountEntity.getId(), isFulfilled)
                    .stream().map(this::toDto).collect(Collectors.toList());
        }
    }

    @Override
    public BankReservationDTO getReservationById(long reservationId) {
        return toDto(bankReservationRepository.findById(reservationId)
                .orElseThrow(()-> new RuntimeBusinessException(HttpStatus.NOT_FOUND, BANK$ACC$0006, reservationId)));
    }

    @Override
    public void fulfilReservation(long reservationId) {
        BankReservationEntity entity = bankReservationRepository.findById(reservationId)
                .orElseThrow(()-> new RuntimeBusinessException(HttpStatus.NOT_FOUND, BANK$ACC$0006, reservationId));
        entity.setFulfilled(true);
        entity.setFulfilledDate(LocalDateTime.now());
        bankReservationRepository.save(entity);
    }

    private BankReservationDTO toDto(BankReservationEntity entity) {
        BankReservationDTO dto = BankReservationDTO.builder()
                    .id(entity.getId())
                    .account(bankAccountService.toDto(entity.getAccount()))
                    .activityDate(entity.getActivityDate())
                    .amount(entity.getAmount())
                    .fulfilled(entity.getFulfilled())
                    .fulfilledDate(entity.getFulfilledDate())
                    .build();
        return dto;
    }
}
