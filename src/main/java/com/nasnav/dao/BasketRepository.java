package com.nasnav.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.nasnav.persistence.BasketsEntity;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface BasketRepository extends JpaRepository<BasketsEntity, Long> {

    @Query("select basket from BasketsEntity basket where basket.ordersEntity.id = :orderId")
    List<BasketsEntity> findByOrdersEntity_Id(@Param("orderId") Long orderId);

    @Transactional
    void deleteByOrdersEntity_Id(Long orderId);
}
