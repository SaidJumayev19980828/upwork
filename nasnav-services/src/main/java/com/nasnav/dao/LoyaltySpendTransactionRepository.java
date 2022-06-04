package com.nasnav.dao;

import com.nasnav.persistence.LoyaltySpentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoyaltySpendTransactionRepository extends JpaRepository<LoyaltySpentTransactionEntity, Long> {
}
