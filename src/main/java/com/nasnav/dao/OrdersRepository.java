package com.nasnav.dao;

import com.nasnav.dto.OrderPhoneNumberPair;
import com.nasnav.dto.response.ProductStatisticsInfo;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.dto.query.result.OrderPaymentOperator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;


public interface OrdersRepository extends JpaRepository<OrdersEntity, Long> {

    Optional<OrdersEntity> findById(long id);

    @Transactional
    @Modifying
    @Query("update OrdersEntity set payment_status = :paymentStatus, updated_at = :updateTimestamp where id = :orderId")
    void setPaymentStatusForOrder(@Param("orderId") Long orderId, @Param("paymentStatus") Integer paymentStatus, @Param("updateTimestamp") Date updateTimestamp);


    List<OrdersEntity> findByShopsEntityId(Long shopId);
    List<OrdersEntity> findByOrganizationEntityId(Long orgId);

	List<OrdersEntity> findByMetaOrderId(Long metaOrderId);

    List<OrdersEntity> findByStatus(Integer status);    

	Long countByStatusAndUserId(Integer value, long l);

	Long countByOrganizationEntity_id(long orgId);

	Long countByShopsEntity_id(Long shopId);

	List<OrdersEntity> findByPaymentEntity_idOrderById(Long id);

	@Query(value = "select o from OrdersEntity o join OrganizationEntity org on o.organizationEntity = org where o.id in :orderIds ")
	List<OrdersEntity> getOrdersIn(@Param("orderIds") List<Long> orderIds);


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
			+ " LEFT JOIN FETCH meta.promotions promo "
			+ " LEFT JOIN FETCH meta.user user" 
			+ " LEFT JOIN FETCH ord.addressEntity userAddr "
			+ " LEFT JOIN FETCH ord.shopsEntity shop "
			+ " LEFT JOIN FETCH shop.addressesEntity shopAddr"
			+ " LEFT JOIN FETCH meta.subOrders subOrd"
			+ " LEFT JOIN FETCH ord.basketsEntity basket"
			+ " LEFT JOIN FETCH basket.stocksEntity stock "
			+ " LEFT JOIN FETCH stock.productVariantsEntity variant "
			+ " LEFT JOIN FETCH variant.productEntity product "
			+ " WHERE ord.id = :orderId and shop.id = :shopId" )
	Optional<OrdersEntity> findByIdAndShopsEntity_Id(@Param("orderId")Long orderId, @Param("shopId")Long shopId);
	
	
	
	@Query("SELECT ord "
			+ " FROM OrdersEntity ord "
			+ " LEFT JOIN FETCH ord.metaOrder meta "
			+ " LEFT JOIN FETCH meta.promotions promo "
			+ " LEFT JOIN FETCH meta.user user" 
			+ " LEFT JOIN FETCH ord.addressEntity userAddr "
			+ " LEFT JOIN FETCH ord.shopsEntity shop "
			+ " LEFT JOIN FETCH shop.addressesEntity shopAddr"
			+ " LEFT JOIN FETCH meta.subOrders subOrd"
			+ " LEFT JOIN FETCH subOrd.shipment shipment"
			+ " LEFT JOIN FETCH ord.basketsEntity basket"
			+ " LEFT JOIN FETCH basket.stocksEntity stock "
			+ " LEFT JOIN FETCH stock.productVariantsEntity variant "
			+ " LEFT JOIN FETCH variant.productEntity product "
			+ " WHERE ord.id = :orderId and ord.organizationEntity.id = :orgId" )
	Optional<OrdersEntity> findByIdAndOrganizationEntity_Id(@Param("orderId")Long orderId, @Param("orgId")Long orgId);
	
	
	
	@Query("SELECT ord "
			+ " FROM OrdersEntity ord "
			+ " LEFT JOIN FETCH ord.metaOrder meta "
			+ " LEFT JOIN FETCH meta.user user" 
			+ " LEFT JOIN FETCH ord.addressEntity userAddr "
			+ " LEFT JOIN FETCH ord.shopsEntity shop "
			+ " LEFT JOIN FETCH shop.addressesEntity shopAddr"
			+ " LEFT JOIN FETCH meta.subOrders subOrd"
			+ " LEFT JOIN FETCH subOrd.shipment shipment"
			+ " LEFT JOIN FETCH ord.basketsEntity basket"
			+ " LEFT JOIN FETCH basket.stocksEntity stock "
			+ " LEFT JOIN FETCH stock.productVariantsEntity variant "
			+ " LEFT JOIN FETCH variant.productEntity product "
			+ " WHERE ord.id = :orderId and ord.organizationEntity.id = :orgId "
			+ " and user.id = :userId" )
    Optional<OrdersEntity> findByIdAndUserIdAndOrganizationEntity_Id(@Param("orderId")Long orderId, @Param("userId")Long userId, @Param("orgId")Long orgId);

	
	@Query("SELECT ord "
			+ " FROM OrdersEntity ord "
			+ " LEFT JOIN FETCH ord.metaOrder meta "
			+ " LEFT JOIN FETCH meta.user user " 
			+ " LEFT JOIN FETCH ord.addressEntity userAddr "
			+ " LEFT JOIN FETCH ord.shopsEntity shop "
			+ " LEFT JOIN FETCH shop.addressesEntity shopAddr "
			+ " LEFT JOIN FETCH meta.subOrders subOrd "
			+ " LEFT JOIN FETCH subOrd.shipment shipment "
			+ " LEFT JOIN FETCH ord.basketsEntity basket "
			+ " LEFT JOIN FETCH basket.stocksEntity stock "
			+ " LEFT JOIN FETCH stock.productVariantsEntity variant "
			+ " LEFT JOIN FETCH variant.productEntity product "
			+ " WHERE ord.id = :orderId " )
	Optional<OrdersEntity> findFullDataById(@Param("orderId")Long orderId);


	@Query("select new com.nasnav.dto.OrderPhoneNumberPair(o.id , u.phoneNumber) from OrdersEntity o join UserEntity u  on o.userId = u.id where o.id in :orderIdList")
	List<OrderPhoneNumberPair> findUsersPhoneNumber(@Param("orderIdList") Set<Long> orderIdList);

	@Query("SELECT new com.nasnav.persistence.dto.query.result.OrderPaymentOperator(ord.id, payment.operator) " +
			" FROM OrdersEntity ord " +
			" LEFT JOIN ord.metaOrder meta " +
			" LEFT JOIN PaymentEntity payment " +
			" on payment.metaOrderId = meta.id " +
			" WHERE ord.id in :orderIds")
    List<OrderPaymentOperator> findPaymentOperatorByOrderIdIn(@Param("orderIds") Set<Long> ordersIds);




	@Query("SELECT new com.nasnav.dto.response.ProductStatisticsInfo(" +
			" product.id, variant.id, variant.name,variant.barcode,variant.sku, variant.productCode, COUNT(product.id) as cnt, sum(stock.price), " +
			" DATE_TRUNC('month', subOrder.creationDate) AS date)" +
			" FROM OrdersEntity subOrder " +
			" left join subOrder.basketsEntity basket " +
			" left join basket.stocksEntity stock " +
			" left join stock.productVariantsEntity variant " +
			" left join variant.productEntity product " +
			" where product.organizationId = :orgId and subOrder.creationDate >= :startDate" +
			" GROUP BY product.id, product.name, variant.id, variant.name, DATE_TRUNC('month',subOrder.creationDate)" +
			" order by DATE_TRUNC('month',subOrder.creationDate) desc, COUNT(product.id) desc")
	List<ProductStatisticsInfo> getProductsStatisticsPerMonth(@Param("orgId") Long orgId,
															  @Param("startDate") LocalDateTime startDate);

	@Query("SELECT sum(stock.price) " +
			" FROM OrdersEntity subOrder " +
			" left join subOrder.basketsEntity basket " +
			" left join basket.stocksEntity stock " +
			" left join stock.productVariantsEntity variant " +
			" left join variant.productEntity product " +
			" where product.organizationId = :orgId and subOrder.creationDate between :minMonth and :maxMonth")
	BigDecimal getTotalIncomePerMonth(@Param("orgId") Long orgId,
									  @Param("minMonth") LocalDateTime minMonth,
									  @Param("maxMonth") LocalDateTime maxMonth);

	@Query("SELECT sum(basket.quantity) " +
			" FROM OrdersEntity subOrder " +
			" left join subOrder.basketsEntity basket " +
			" left join basket.stocksEntity stock " +
			" left join stock.productVariantsEntity variant " +
			" left join variant.productEntity product " +
			" where product.organizationId = :orgId and subOrder.creationDate between :minWeek and :maxWeek")
	Integer getSalesPerWeek(@Param("orgId") Long orgId,
							@Param("minWeek") LocalDateTime minWeek,
							@Param("maxWeek") LocalDateTime maxWeek);

	@Query("select count(subOrder) from OrdersEntity subOrder where subOrder.userId = :userId and subOrder.status = 2 and subOrder.id = :orderId")
	Integer getStoreConfirmedOrderCountPerUser(@Param("orderId") Long orderId,
											   @Param("userId") Long userId);
}
