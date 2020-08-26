package com.nasnav.dao;

import com.nasnav.persistence.Shop360ProductsEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ShopsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface Product360ShopsRepository extends JpaRepository<Shop360ProductsEntity, Long> {

    boolean existsByProductEntityAndShopEntity(ProductEntity product, ShopsEntity shop);

    @Transactional
    @Modifying
    void deleteByProductEntityInAndShopEntityIn(List<ProductEntity> product, List<ShopsEntity> shop);

    @Query("select ps.shopEntity.id from Shop360ProductsEntity ps where ps.productEntity.id = :id")
    List<Long> findShopsByProductId(@Param("id") Long id);

    List<Shop360ProductsEntity> findByProductEntity_IdIn(@Param("id") List<Long> ids);
}
