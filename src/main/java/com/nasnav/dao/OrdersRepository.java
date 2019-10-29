package com.nasnav.dao;

import com.nasnav.persistence.OrdersEntity;

import java.util.Date;
import java.util.List;
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

    List<OrdersEntity> findByUserId(Long userId);
    List<OrdersEntity> findByUserIdAndStatus(Long userId, Integer status);

    List<OrdersEntity> findByShopsEntityId(Long shopId);
    List<OrdersEntity> getOrdersEntityByShopsEntityIdAndUserId(Long shopId, Long userId);
    List<OrdersEntity> getOrdersEntityByShopsEntityIdAndStatus(Long shopId, Integer status);
    List<OrdersEntity> getOrdersEntityByShopsEntityIdAndOrganizationEntityId(Long shopId, Long orgId);
    List<OrdersEntity> getOrdersEntityByShopsEntityIdAndUserIdAndOrganizationEntityId(Long storeId, Long userId, Long orgId);
    List<OrdersEntity> getOrdersEntityByShopsEntityIdAndStatusAndUserId(Long shopId, Integer status, Long userId);
    List<OrdersEntity> getOrdersEntityByShopsEntityIdAndStatusAndOrganizationEntityId(Long shopId, Integer status, Long orgId);
    List<OrdersEntity> getOrdersEntityByShopsEntityIdAndStatusAndUserIdAndOrganizationEntityId(Long shopId, Integer status, Long userId, Long orgId);

    List<OrdersEntity> findByOrganizationEntityId(Long orgId);
    List<OrdersEntity> findByOrganizationEntityIdAndUserId(Long orgId, Long userId);
    List<OrdersEntity> findByOrganizationEntityIdAndStatus(Long orgId, Integer status);
    List<OrdersEntity> findByOrganizationEntityIdAndStatusAndUserId(Long orgId, Integer status, Long userId);

    Boolean existsByIdAndUserId(Long orderId, Long userId);

    List<OrdersEntity> findByStatus(Integer status);    

	Optional<OrdersEntity> findFirstByUserIdAndStatusOrderByUpdateDateDesc(Long id, Integer value);

	Long countByStatusAndUserId(Integer value, long l);

	void deleteByStatusAndUserId(Integer value, Long id);
}
