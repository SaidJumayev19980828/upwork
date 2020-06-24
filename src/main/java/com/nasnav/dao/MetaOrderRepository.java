package com.nasnav.dao;

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
			+ " WHERE meta.id =:id")
	Optional<MetaOrderEntity> findFullDataById(@Param("id")Long id);
}

