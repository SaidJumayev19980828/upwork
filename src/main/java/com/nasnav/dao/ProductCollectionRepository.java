package com.nasnav.dao;

import com.nasnav.persistence.ProductCollectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ProductCollectionRepository extends JpaRepository<ProductCollectionEntity, Long> {

    @Query("select c from ProductCollectionEntity c " +
            " left join fetch c.items items " +
            " left join fetch items.item item " +
            " where c.organizationId = :orgId " +
            " and c.productType = 2 " +
            " and c.id = :id")
    Optional<ProductCollectionEntity> findByIdAndOrganizationId(@Param("id")Long id, @Param("orgId")Long orgId);

    @Query("select c from ProductCollectionEntity c " +
            " left join fetch c.items items " +
            " left join fetch items.item item " +
            " where c.organizationId = :orgId and c.productType = 2")
    List<ProductCollectionEntity> findByOrganizationId(@Param("orgId")Long orgId);


    @Query("select c from ProductCollectionEntity c " +
            " Left join fetch c.items collectionItem " +
            " Left join fetch collectionItem.item variant " +
            " where c.id = :id " +
            " and c.productType = 2")
    Optional<ProductCollectionEntity> findByCollectionId(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query("update ProductCollectionEntity c set c.removed = 1 where c.id = :id and c.organizationId = :orgId")
    void removeCollection(@Param("id") Long id, @Param("orgId") Long orgId);
}
