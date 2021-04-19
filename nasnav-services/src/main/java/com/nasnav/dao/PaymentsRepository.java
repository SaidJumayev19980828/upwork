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

    // TODO replace the fixed '4' PaymentStatus
    @Query("select payments from PaymentEntity payments where payments.status=4 and payments.ordersEntity.id = :orderId order by id desc")
    List<PaymentEntity> findRecentByOrdersEntity_Id(@Param("orderId") Long orderId);

    Optional<PaymentEntity> findByMetaOrderId(Long orderId);

    List<PaymentEntity> findBySessionId(String sessionId);

    List<PaymentEntity> findByMetaOrderIdIn(List<Long> metaOrderIds);
}
