package com.nasnav.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nasnav.dto.MetaOrderBasicInfo;
import com.nasnav.dto.response.OrderStatisticsInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nasnav.persistence.MetaOrderEntity;

public interface MetaOrderRepository extends JpaRepository<MetaOrderEntity, Long> {
	
	
	
	@Query("SELECT meta FROM MetaOrderEntity meta "
			+ " LEFT JOIN FETCH meta.organization org "
			+ " WHERE meta.id =:id")
	Optional<MetaOrderEntity> findMetaOrderWithOrganizationById(@Param("id")Long id);
	
	
	@Query("SELECT meta FROM MetaOrderEntity meta "
			+ " LEFT JOIN FETCH meta.organization org "
			+ " LEFT JOIN FETCH meta.user usr "
			+ " LEFT JOIN FETCH meta.subOrders subOrder "
			+ " LEFT JOIN FETCH subOrder.shopsEntity shop "
			+ " LEFT JOIN FETCH subOrder.basketsEntity item "
			+ " LEFT JOIN FETCH item.stocksEntity stock "
			+ " LEFT JOIN FETCH stock.productVariantsEntity variant "
			+ " LEFT JOIN FETCH variant.productEntity product "
			+ " WHERE meta.id =:id")
	Optional<MetaOrderEntity> findFullDataById(@Param("id")Long id);


	@Query("SELECT meta FROM MetaOrderEntity meta "
			+ " LEFT JOIN FETCH meta.organization org "
			+ " LEFT JOIN FETCH meta.user usr "
			+ " LEFT JOIN FETCH meta.subOrders subOrder "
			+ " LEFT JOIN FETCH subOrder.shopsEntity shop "
			+ " LEFT JOIN FETCH subOrder.basketsEntity item "
			+ " LEFT JOIN FETCH subOrder.shipment shipment "
			+ " LEFT JOIN FETCH item.stocksEntity stock "
			+ " LEFT JOIN FETCH subOrder.addressEntity subOrderAddr "
			+ " LEFT JOIN FETCH shop.addressesEntity shopAddr "
			+ " LEFT JOIN FETCH stock.productVariantsEntity variant "
			+ " LEFT JOIN FETCH variant.productEntity product "
			+ " WHERE meta.id =:id AND org.id = :orgId")
	Optional<MetaOrderEntity> findByIdAndOrganization_Id(@Param("id") Long id,
																  @Param("orgId") Long orgId);

	@Query("SELECT meta FROM MetaOrderEntity meta "
			+ " LEFT JOIN FETCH meta.organization org "
			+ " LEFT JOIN FETCH meta.user usr "
			+ " LEFT JOIN FETCH meta.subOrders subOrder "
			+ " LEFT JOIN FETCH subOrder.shopsEntity shop "
			+ " LEFT JOIN FETCH subOrder.basketsEntity item "
			+ " LEFT JOIN FETCH subOrder.shipment shipment "
			+ " LEFT JOIN FETCH item.stocksEntity stock "
			+ " LEFT JOIN FETCH subOrder.addressEntity subOrderAddr "
			+ " LEFT JOIN FETCH shop.addressesEntity shopAddr "
			+ " LEFT JOIN FETCH stock.productVariantsEntity variant "
			+ " LEFT JOIN FETCH variant.productEntity product "
			+ " LEFT JOIN FETCH meta.promotions"
			+ " WHERE meta.id =:id ")
	Optional<MetaOrderEntity> findByMetaOrderId(@Param("id") Long id);

	@Query("SELECT meta FROM MetaOrderEntity meta "
			+ " LEFT JOIN FETCH meta.organization org "
			+ " LEFT JOIN FETCH meta.user usr "
			+ " LEFT JOIN FETCH meta.subOrders subOrder "
			+ " LEFT JOIN FETCH subOrder.shopsEntity shop "
			+ " LEFT JOIN FETCH subOrder.basketsEntity item "
			+ " LEFT JOIN FETCH subOrder.shipment shipment "
			+ " LEFT JOIN FETCH item.stocksEntity stock "
			+ " LEFT JOIN FETCH subOrder.addressEntity subOrderAddr "
			+ " LEFT JOIN FETCH shop.addressesEntity shopAddr "
			+ " LEFT JOIN FETCH stock.productVariantsEntity variant "
			+ " LEFT JOIN FETCH variant.productEntity product "
			+ " WHERE meta.id =:id AND usr.id = :userId AND org.id = :orgId")
	Optional<MetaOrderEntity> findByIdAndUserIdAndOrganization_Id(@Param("id") Long id,
																  @Param("userId") Long userId,
														 @Param("orgId") Long orgId);

	@Query("SELECT DISTINCT meta FROM MetaOrderEntity meta "
			+ " LEFT JOIN FETCH meta.organization org "
			+ " LEFT JOIN FETCH meta.user usr "
			+ " LEFT JOIN FETCH meta.subOrders subOrder "
			+ " LEFT JOIN FETCH subOrder.shopsEntity shop "
			+ " LEFT JOIN FETCH subOrder.basketsEntity item "
			+ " LEFT JOIN FETCH subOrder.shipment shipment "
			+ " LEFT JOIN FETCH item.stocksEntity stock "
			+ " LEFT JOIN FETCH subOrder.addressEntity subOrderAddr "
			+ " LEFT JOIN FETCH shop.addressesEntity shopAddr "
			+ " LEFT JOIN FETCH stock.productVariantsEntity variant "
			+ " LEFT JOIN FETCH variant.productEntity product "
			+ " WHERE usr.id =:userId AND org.id = :orgId"
			+ " AND meta.status != 10 ")
	List<MetaOrderEntity> findByUser_IdAndOrganization_Id(@Param("userId") Long userId,
														  @Param("orgId") Long orgId);



	@Query("SELECT distinct new com.nasnav.dto.MetaOrderBasicInfo(" +
			"meta.id, meta.createdAt, meta.status, meta.grandTotal"
			+ ", pay.operator, shipment.shippingServiceId, sum(basket.quantity) "
			+ ", pay.status) "
			+ "FROM MetaOrderEntity meta "
			+ "LEFT JOIN meta.subOrders subOrder "
			+ "LEFT JOIN subOrder.basketsEntity basket "
			+ "LEFT JOIN subOrder.shipment shipment "
			+ "LEFT JOIN PaymentEntity pay on meta.id = pay.metaOrderId "
			+ "LEFT JOIN meta.user user "
			+ "LEFT JOIN meta.organization org "
			+ "WHERE user.id = :userId AND org.id = :orgId "
			+ " AND meta.status != 10 "
			+ "group by meta.id, meta.createdAt, meta.status, meta.grandTotal"
			+ " , pay.operator, shipment.shippingServiceId, pay.status")
	List<MetaOrderBasicInfo> getMetaOrderList(@Param("userId") Long userId,
											  @Param("orgId") Long orgId);


	
	@Query("SELECT meta FROM MetaOrderEntity meta "
			+ " LEFT JOIN FETCH meta.organization org "
			+ " LEFT JOIN FETCH meta.user usr "
			+ " LEFT JOIN FETCH meta.subOrders subOrder "
			+ " INNER JOIN PaymentEntity payment on "
			+ " payment.metaOrderId = meta.id "
			+ " and payment.status in :paymentStatus "
			+ " WHERE usr.id = :userId "
			+ " and meta.status = :orderStatus ")
	List<MetaOrderEntity> findByUser_IdAndStatusAndPaymentStatusIn(
			@Param("userId")Long userId
			, @Param("orderStatus")Integer orderStatus
			, @Param("paymentStatus") List<Integer> asList);
	
	
	
	
	
	@Query("SELECT meta FROM MetaOrderEntity meta "
			+ " LEFT JOIN FETCH meta.organization org "
			+ " LEFT JOIN FETCH meta.user usr "
			+ " LEFT JOIN FETCH meta.subOrders subOrder "
			+ " WHERE usr.id = :userId "
			+ " and meta.status = :orderStatus "
			+ " and NOT EXISTS ( "
			+ "		SELECT payment FROM PaymentEntity payment"
			+ "		WHERE payment.metaOrderId = meta.id"
			+ " )")
	List<MetaOrderEntity> findByUser_IdAndStatusAndNoPayment(
			@Param("userId")Long userId
			, @Param("orderStatus")Integer orderStatus);


	@Query("SELECT new com.nasnav.dto.response.OrderStatisticsInfo(DATE_TRUNC('month', meta.createdAt) AS date," +
			" meta.status," +
			" COUNT(meta.id) AS count) " +
			" FROM MetaOrderEntity meta " +
			" where meta.organization.id = :orgId and meta.status in :statuses and meta.createdAt >= :startDate " +
			" GROUP BY meta.status, DATE_TRUNC('month',meta.createdAt)" +
			" order by DATE_TRUNC('month',meta.createdAt)")
	List<OrderStatisticsInfo> getOrderIncomeStatisticsPerMonth(@Param("orgId") Long orgId,
															   @Param("statuses") List<Integer> statuses,
															   @Param("startDate") LocalDateTime startDate);


	@Query("SELECT new com.nasnav.dto.response.OrderStatisticsInfo(DATE_TRUNC('month', meta.createdAt) AS date," +
			" meta.status," +
			" sum(meta.grandTotal) as income) " +
			" FROM MetaOrderEntity meta " +
			" where meta.organization.id = :orgId and meta.status in :statuses and meta.grandTotal is not null " +
			" and meta.createdAt >= :startDate" +
			" GROUP BY meta.status, DATE_TRUNC('month',meta.createdAt)" +
			" order by DATE_TRUNC('month',meta.createdAt)")
	List<OrderStatisticsInfo> getOrderCountStatisticsPerMonth(@Param("orgId") Long orgId,
															  @Param("statuses") List<Integer> statuses,
															  @Param("startDate") LocalDateTime startDate);
}

