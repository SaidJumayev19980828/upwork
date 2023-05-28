package com.nasnav.dao;

import com.nasnav.persistence.BankOutsideTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankOutsideTransactionRepository extends JpaRepository<BankOutsideTransactionEntity, Long> {
}
