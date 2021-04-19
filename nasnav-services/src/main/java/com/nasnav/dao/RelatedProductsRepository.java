package com.nasnav.dao;

import com.nasnav.persistence.RelatedProductsEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RelatedProductsRepository extends CrudRepository<RelatedProductsEntity, Long> {

    @Transactional
    @Modifying
    @Query(value = "delete from RelatedProductsEntity r " +
            " where r.product.id = :productId and r.relatedProduct.id in :relatedProductsIds ")
    void deleteByProductAndRelatedProductsIn(@Param("productId") Long productId,
                                             @Param("relatedProductsIds") List<Long> relatedProductsIds);

    List<RelatedProductsEntity> findByProduct_Id(Long productId);
}
