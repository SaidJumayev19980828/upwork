package com.nasnav.dao;

import com.nasnav.persistence.BankInsideTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankInsideTransactionRepository extends JpaRepository<BankInsideTransactionEntity, Long> {
}
