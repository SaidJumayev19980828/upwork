package com.nasnav.dao;

import com.nasnav.persistence.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentsRepository extends JpaRepository<PaymentEntity, Long> {
	
    Optional<PaymentEntity> findById(long id);
}
