package com.nasnav.dao;

import com.nasnav.dto.request.ProductPositionDTO;
import com.nasnav.persistence.*;
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

    @Query("select ps.shopEntity.id from Shop360ProductsEntity ps where ps.productEntity.id = :id and ps.published = 2")
    List<Long> findShopsByProductId(@Param("id") Long id);

    @Query("select new com.nasnav.dto.request.ProductPositionDTO(ps.productEntity.id, ps.floor.number, ps.section.id, ps.scene.id, ps.pitch, ps.yaw, ps.productEntity.productType) " +
            " from Shop360ProductsEntity ps where ps.shopEntity.id = :id and ps.published = :published")
    List<ProductPositionDTO> findProductsPositionsFullData(@Param("id") Long id,
                                                           @Param("published") short published);

    @Query("select ps from Shop360ProductsEntity ps where ps.shopEntity.id = :id and ps.published = 1")
    List<Shop360ProductsEntity> findProductsPositionsByShopId(@Param("id") Long id);

    List<Shop360ProductsEntity> findByProductEntity_IdInAndPublished(List<Long> ids, Short published);

    @Transactional
    @Modifying
    @Query(value = "delete from Shop360ProductsEntity sp where sp.shopEntity.id = :shopId and sp.published = 2")
    void deleteByShopId(@Param("shopId") Long shopId);

    boolean existsByFloor(ShopFloorsEntity floor);

    boolean existsBySection(ShopSectionsEntity section);

    boolean existsByScene(ShopScenesEntity scene);

    @Transactional
    @Modifying
    void deleteByFloor(ShopFloorsEntity floor);

    @Transactional
    @Modifying
    void deleteBySection(ShopSectionsEntity section);

    @Transactional
    @Modifying
    void deleteByScene(ShopScenesEntity scene);
}
