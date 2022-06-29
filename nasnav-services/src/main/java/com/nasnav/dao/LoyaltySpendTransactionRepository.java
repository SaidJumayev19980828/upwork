package com.nasnav.dao;

import com.nasnav.persistence.LoyaltySpentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import javax.transaction.Transactional;
import java.util.Optional;

public interface LoyaltySpendTransactionRepository extends JpaRepository<LoyaltySpentTransactionEntity, Long> {

	@Transactional
	@Modifying
	void deleteByTransaction_Id(Long transactionId);

	Optional<LoyaltySpentTransactionEntity> findByTransaction_Id(Long transactionId);
}
