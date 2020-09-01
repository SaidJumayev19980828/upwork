package com.nasnav.dao;

import com.nasnav.dto.request.ProductPositionDTO;
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

    @Query("select new com.nasnav.dto.request.ProductPositionDTO(ps.productEntity.id, ps.floor.id, ps.section.id, ps.scene.id, ps.pitch, ps.yaw, ps.productEntity.productType) " +
            " from Shop360ProductsEntity ps where ps.shopEntity.id = :id and ps.published = :published")
    List<ProductPositionDTO> findProductsPositionsFullData(@Param("id") Long id,
                                                           @Param("published") Boolean published);

    @Query("select ps from Shop360ProductsEntity ps where ps.shopEntity.id = :id and ps.published = false")
    List<Shop360ProductsEntity> findProductsPositionsByShopId(@Param("id") Long id);

    List<Shop360ProductsEntity> findByProductEntity_IdIn(@Param("id") List<Long> ids);
}
