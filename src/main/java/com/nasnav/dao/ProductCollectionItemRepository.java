package com.nasnav.dao;

import com.nasnav.persistence.ProductCollectionItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.Set;

public interface ProductCollectionItemRepository extends JpaRepository<ProductCollectionItemEntity, Long> {
    @Transactional
    @Modifying
    @Query("delete from ProductCollectionItemEntity item where item in :items")
    void deleteItems(@Param("items") Set<ProductCollectionItemEntity> oldItems);
}
