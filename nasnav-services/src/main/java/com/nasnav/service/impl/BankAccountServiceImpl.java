package com.nasnav.service.impl;

import com.nasnav.dao.BankAccountRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dto.BankAccountDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BankAccountEntity;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.BankAccountService;
import com.nasnav.service.OrganizationService;
import com.nasnav.service.SecurityService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.nasnav.exceptions.ErrorCodes.*;

@Service
@AllArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {
    private final OrganizationService organizationService;
    private final OrganizationRepository organizationRepository;
    private final SecurityService securityService;
    private final BankAccountRepository bankAccountRepository;

    @Override
    public BankAccountDTO createAccount(BankAccountDTO dto) {
        return toDto(bankAccountRepository.save(toEntity(dto)));
    }

    @Override
    public Long getOpeningBalance(long accountId) {
        BankAccountEntity entity = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,BANK$ACC$0003, accountId));
        return entity.getOpeningBalance();
    }

    @Override
    public void updateOpeningBalance(long accountId, long newBalance) {
        BankAccountEntity entity = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,BANK$ACC$0003, accountId));
        entity.setOpeningBalance(newBalance);
        entity.setOpeningBalanceDate(LocalDateTime.now());
        bankAccountRepository.save(entity);
    }

    @Override
    public void lockOrUnlockAccount(long accountId, boolean isLocked) {
        BankAccountEntity entity = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,BANK$ACC$0003, accountId));
        entity.setLocked(isLocked);
        bankAccountRepository.save(entity);
    }

    private BankAccountEntity toEntity(BankAccountDTO dto) {
        BaseUserEntity loggedUser = securityService.getCurrentUser();
        if(bankAccountRepository.existsByUser_IdOrOrganization_Id(loggedUser.getId(), loggedUser.getOrganizationId()))
                throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE,BANK$ACC$0002);

        BankAccountEntity entity = new BankAccountEntity();
        entity.setCreatedAt(LocalDateTime.now());
        entity.setLocked(false);
        entity.setOpeningBalance(0L);
        entity.setOpeningBalanceDate(LocalDateTime.now());
        entity.setWalletAddress(dto.getWallerAddress());

        if(loggedUser instanceof UserEntity){
            entity.setUser((UserEntity) loggedUser);
        }
        else if (loggedUser instanceof EmployeeUserEntity) {
            entity.setOrganization(organizationRepository.getOne(loggedUser.getOrganizationId()));
        }
        else {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE,BANK$ACC$0001);
        }
        return entity;
    }

    private BankAccountDTO toDto(BankAccountEntity entity) {
        BankAccountDTO dto = new BankAccountDTO();
        dto.setId(entity.getId());
        if (entity.getUser() != null){
            dto.setUser(entity.getUser().getRepresentation());
        }
        if(entity.getOrganization() != null){
            dto.setOrg(organizationService.getOrganizationById(entity.getOrganization().getId(),0));
        }
        dto.setOpeningBalance(entity.getOpeningBalance());
        dto.setOpeningBalanceDate(entity.getOpeningBalanceDate());
        dto.setLocked(entity.getLocked());
        dto.setWallerAddress(entity.getWalletAddress());

        return dto;
    }
}
