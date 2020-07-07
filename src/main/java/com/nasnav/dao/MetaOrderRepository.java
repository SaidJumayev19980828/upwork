package com.nasnav.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nasnav.persistence.MetaOrderEntity;

public interface MetaOrderRepository extends JpaRepository<MetaOrderEntity, Long> {
	
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

	boolean existsByIdAndOrganization_Id(Long id, Long orgId);
	boolean existsByIdAndUserIdAndOrganization_Id(Long id, Long userId, Long orgId);

	Optional<MetaOrderEntity> findByIdAndOrganization_Id(Long id, Long orgId);
	Optional<MetaOrderEntity> findByIdAndUserIdAndOrganization_Id(Long id, Long userId, Long orgId);
	
	
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
}

