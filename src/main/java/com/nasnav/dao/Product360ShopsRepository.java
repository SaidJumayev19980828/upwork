package com.nasnav.dao;

import com.nasnav.persistence.Product360ShopsEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ShopsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface Product360ShopsRepository extends JpaRepository<Product360ShopsEntity, Long> {

    boolean existsByProductEntityAndShopEntity(ProductEntity product, ShopsEntity shop);

    @Transactional
    @Modifying
    void deleteByProductEntityInAndShopEntityIn(List<ProductEntity> product, List<ShopsEntity> shop);

    @Query("select ps.shopEntity.id from Product360ShopsEntity ps where ps.productEntity.id = :id")
    List<Long> findShopsByProductId(@Param("id") Long id);

    List<Product360ShopsEntity> findByProductEntity_IdIn(@Param("id") List<Long> ids);
}
