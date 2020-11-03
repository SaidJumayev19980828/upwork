package com.nasnav.dao;

import com.nasnav.persistence.ProductCollectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductCollectionRepository extends JpaRepository<ProductCollectionEntity, Long> {

    Optional<ProductCollectionEntity> findByIdAndOrganizationId(Long id, Long orgId);

    List<ProductCollectionEntity> findByOrganizationId(Long orgId);

    @Query("select c from ProductCollectionEntity c where c.id = :id and c.productType = 2")
    Optional<ProductCollectionEntity> findByCollectionId(@Param("id") Long id);
}
