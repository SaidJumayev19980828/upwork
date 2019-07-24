package com.nasnav.dao;

import com.nasnav.persistence.OrdersEntity;

import java.util.Date;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface OrdersRepository extends JpaRepository<OrdersEntity, Long> {
	
    Optional<OrdersEntity> findById(long id);

    @Transactional
    @Modifying
    @Query("update OrdersEntity set payment_status = :paymentStatus, updated_at = :updateTimestamp where id = :orderId")
    void setPaymentStatusForOrder(@Param("orderId") Long orderId, @Param("paymentStatus") Integer paymentStatus, @Param("updateTimestamp") Date updateTimestamp);
}
