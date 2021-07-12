package com.nasnav.dao;

import com.nasnav.persistence.PaymentEntity;
import com.nasnav.persistence.PaymentRefundEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRefundsRepository extends JpaRepository<PaymentRefundEntity, Long> {
	
    Optional<PaymentRefundEntity> findById(long id);
    Optional<PaymentRefundEntity> findByUid(String uid);

    List<PaymentRefundEntity> findAllByPaymentEntity(PaymentEntity payment);


}
