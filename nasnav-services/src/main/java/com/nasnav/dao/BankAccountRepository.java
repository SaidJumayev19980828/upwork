package com.nasnav.dao;

import com.nasnav.persistence.BankAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccountEntity, Long> {
    Boolean existsByUser_IdOrOrganization_Id(Long userId, Long orgId);
    BankAccountEntity getByUser_IdOrOrganization_Id(Long userId, Long orgId);
}
