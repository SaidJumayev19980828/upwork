package com.nasnav.service.impl;

import com.nasnav.dao.BankAccountRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dto.response.BankAccountDTO;
import com.nasnav.dto.response.BankAccountDetailsDTO;
import com.nasnav.dto.response.BankBalanceSummaryDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BankAccountEntity;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.BankAccountActivityService;
import com.nasnav.service.BankAccountService;
import com.nasnav.service.OrganizationService;
import com.nasnav.service.SecurityService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.nasnav.exceptions.ErrorCodes.*;

@Service
@AllArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {
    private final OrganizationService organizationService;
    private final OrganizationRepository organizationRepository;
    private final SecurityService securityService;
    private final BankAccountRepository bankAccountRepository;
    private final BankAccountActivityService bankAccountActivityService;

    @Override
    public BankAccountDetailsDTO createAccount(BankAccountDTO dto) {
        return toDto(bankAccountRepository.save(toEntity(dto)));
    }

    @Override
    public BankAccountDTO getAccount() {
        return toDto(getLoggedAccount());
    }

    @Override
    public Long getOpeningBalance(long accountId) {
        BankAccountEntity entity = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,BANK$ACC$0003, accountId));
        return entity.getOpeningBalance();
    }

    @Override
    public void setOpeningBalance(long accountId) {
        BankAccountEntity entity = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,BANK$ACC$0003, accountId));
        //lock account
        this.lockOrUnlockAccount(entity.getId(), true);
        //accumulate from the begin of opening balance date to the last activity
        Long balance = bankAccountActivityService.getAvailableBalance(accountId);
        //set opening balance
        entity.setOpeningBalance(balance);
        entity.setOpeningBalanceDate(LocalDateTime.now());
        entity.setOpeningBalanceActivity(bankAccountActivityService.getLastActivity(accountId));
        bankAccountRepository.save(entity);
        //unlock account
        this.lockOrUnlockAccount(entity.getId(), false);
    }

    @Override
    public void setAllAccountsOpeningBalance() {
        List<BankAccountEntity> accountEntities = bankAccountRepository.findAll();
        accountEntities.stream().forEach(account -> {
            this.setOpeningBalance(account.getId());
        });
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

        if(checkAccountExistence(null))
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

    @Override
    public BankAccountDetailsDTO toDto(BankAccountEntity entity) {
        BankAccountDetailsDTO dto = new BankAccountDetailsDTO();
        BankBalanceSummaryDTO summary = new BankBalanceSummaryDTO();
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
        summary.setTotalBalance(bankAccountActivityService.getTotalBalance(entity.getId()));
        summary.setAvailableBalance(bankAccountActivityService.getAvailableBalance(entity.getId()));
        summary.setReservedBalance(bankAccountActivityService.getReservedBalance(entity.getId()));
        dto.setSummary(summary);

        return dto;
    }

    @Override
    public BankAccountEntity getLoggedAccount() {
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

    @Override
    public Boolean checkAccountExistence(Long accountId) {
        Boolean isExist = false;
        if(accountId != null){
            isExist = bankAccountRepository.findById(accountId).isPresent();
        }
        else {
            BaseUserEntity loggedUser = securityService.getCurrentUser();
            if (loggedUser instanceof UserEntity){
                isExist = bankAccountRepository.existsByUser_Id(loggedUser.getId());
            }
            else {
                isExist = bankAccountRepository.existsByOrganization_Id(loggedUser.getOrganizationId());
            }
        }
        return isExist;
    }
}
