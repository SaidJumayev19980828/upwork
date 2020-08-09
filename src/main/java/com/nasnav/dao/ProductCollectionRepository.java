package com.nasnav.dao;

import com.nasnav.persistence.ProductCollectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductCollectionRepository extends JpaRepository<ProductCollectionEntity, Long> {

    Optional<ProductCollectionEntity> findByIdAndOrganizationId(Long id, Long orgId);

}
