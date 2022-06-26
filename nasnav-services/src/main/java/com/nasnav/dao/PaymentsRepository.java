package com.nasnav.dao;

import com.nasnav.persistence.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentsRepository extends JpaRepository<PaymentEntity, Long> {
	
    Optional<PaymentEntity> findById(long id);
    Optional<PaymentEntity> findByUid(String uid);

    Optional<PaymentEntity> findFirstByMetaOrderId(Long orderId);

    Optional<PaymentEntity> findByMetaOrderId(Long orderId);

    List<PaymentEntity> findBySessionId(String sessionId);

    List<PaymentEntity> findByMetaOrderIdIn(List<Long> metaOrderIds);

    Optional<PaymentEntity> findByObjectContainingAndOperator(String ref, String operator);
}
