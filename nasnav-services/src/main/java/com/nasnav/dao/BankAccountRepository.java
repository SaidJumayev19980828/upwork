package com.nasnav.dao;

import com.nasnav.persistence.BankAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccountEntity, Long> {
    Boolean existsByUser_Id(Long userId);
    Boolean existsByOrganization_Id(Long orgId);
    Optional<BankAccountEntity> findByOrganization_Id(Long orgId);
    BankAccountEntity getByUser_Id(Long userId);
    BankAccountEntity getByOrganization_Id(Long orgId);

    BankAccountEntity findByWalletAddress(String walletAddress);
}
