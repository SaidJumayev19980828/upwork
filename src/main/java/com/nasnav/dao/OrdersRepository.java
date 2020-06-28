package com.nasnav.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.persistence.OrdersEntity;


public interface OrdersRepository extends JpaRepository<OrdersEntity, Long> {
	
    Optional<OrdersEntity> findById(long id);

    @Transactional
    @Modifying
    @Query("update OrdersEntity set payment_status = :paymentStatus, updated_at = :updateTimestamp where id = :orderId")
    void setPaymentStatusForOrder(@Param("orderId") Long orderId, @Param("paymentStatus") Integer paymentStatus, @Param("updateTimestamp") Date updateTimestamp);

    
    @Transactional
    @Modifying
	@Query("delete from OrdersEntity o where o.status = :status and o.userId = :userId")
	void deleteByStatusAndUserId(@Param("status") Integer status, @Param("userId") Long userId);
    
    
    List<OrdersEntity> findByUserId(Long userId);
    List<OrdersEntity> findByUserIdAndStatus(Long userId, Integer status);

    OrdersEntity findByIdAndUserId(Long orderId, Long userId);

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

	List<OrdersEntity> findByMetaOrderId(Long metaOrderId);

    Boolean existsByIdAndUserId(Long orderId, Long userId);

    List<OrdersEntity> findByStatus(Integer status);    

	Optional<OrdersEntity> findFirstByUserIdAndStatusOrderByUpdateDateDesc(Long id, Integer value);

	Long countByStatusAndUserId(Integer value, long l);

	Long countByOrganizationEntity_id(long orgId);

	Long countByShopsEntity_id(Long shopId);

	List<OrdersEntity> findByPaymentEntity_idOrderById(Long id);

	@Query(value = "select o from OrdersEntity o join OrganizationEntity org on o.organizationEntity = org where o.id in :orderIds ")
	List<OrdersEntity> getOrdersIn(@Param("orderIds") List<Long> orderIds);

    @Transactional
    @Modifying
    @Query("delete from OrdersEntity o where o.status = :status and o.id in :orderIds and o.organizationEntity.id = :orgId")
    void deleteByStatusAndIdInAndOrgId(@Param("status") Integer status,
                                       @Param("orderIds") List<Long> orderIds,
                                       @Param("orgId") Long orgId);
    
    @Transactional
    @Modifying
    @Query("delete from OrdersEntity o where o.status = :status and o.organizationEntity.id = :orgId")
    void deleteByStatusAndOrgId(@Param("status") Integer status, @Param("orgId") Long orgId);
    
    
    @Transactional
    @Modifying
    @Query("delete from OrdersEntity o where o.status = :status and o.organizationEntity.id = :orgId and o.id in :idList")
    void deleteAllByStatusAndIdIn(@Param("idList") Collection<Long> orderIdList,
								 @Param("orgId") Long orgId,
							     @Param("status") Integer status);

	long countByStatusAndOrganizationEntity_id(Integer value, Long org);
	
	
	@Query("select ord.id from OrdersEntity ord "
			+ " left join ord.basketsEntity basket "
			+ " where basket.stocksEntity.productVariantsEntity.productEntity.id in :idList "
			+ " and basket.ordersEntity in (select o from OrdersEntity o where o.status = :status and o.organizationEntity.id = :orgId)")
	Set<Long> findOrderIdByStatusAndProductIdIn(@Param("idList") List<Long> productIdList,
								 @Param("orgId") Long orgId,
							     @Param("status") Integer status);

	List<OrdersEntity> findByIdIn(List<Long> orderIds);

	
	@Query("SELECT ord "
			+ " FROM OrdersEntity ord "
			+ " LEFT JOIN FETCH ord.metaOrder meta "
			+ " LEFT JOIN FETCH ord.addressEntity userAddr "
			+ " LEFT JOIN FETCH ord.shopsEntity shop "
			+ " LEFT JOIN FETCH shop.addressesEntity shopAddr"
			+ " LEFT JOIN FETCH meta.subOrders subOrd"
			+ " LEFT JOIN FETCH ord.basketsEntity basket"
			+ " WHERE ord.id = :orderId and shop.id = :shopId" )
	Optional<OrdersEntity> findByIdAndShopsEntity_Id(@Param("orderId")Long orderId, @Param("shopId")Long shopId);
}
