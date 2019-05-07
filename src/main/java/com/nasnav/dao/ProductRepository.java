package com.nasnav.dao;

import com.nasnav.persistence.ProductEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProductRepository extends CrudRepository<ProductEntity,Long> {

    List<ProductEntity> findByOrganizationId(Long organizationId);
    List<ProductEntity> findByOrganizationIdOrderByIdAsc(Long organizationId);
    List<ProductEntity> findByOrganizationIdOrderByIdDesc(Long organizationId);

    List<ProductEntity> findByOrganizationIdOrderByNameAsc(Long organizationId);
    List<ProductEntity> findByOrganizationIdOrderByNameDesc(Long organizationId);

    List<ProductEntity> findByOrganizationIdOrderByPnameAsc(Long organizationId);
    List<ProductEntity> findByOrganizationIdOrderByPnameDesc(Long organizationId);

    List<ProductEntity> findByOrganizationIdAndCategoryId(Long organizationId, Long categoryId);
    List<ProductEntity> findByOrganizationIdAndCategoryIdOrderByIdAsc(Long organizationId,Long categoryId);
    List<ProductEntity> findByOrganizationIdAndCategoryIdOrderByIdDesc(Long organizationId,Long categoryId);
    List<ProductEntity> findByOrganizationIdAndCategoryIdOrderByNameAsc(Long organizationId,Long categoryId);
    List<ProductEntity> findByOrganizationIdAndCategoryIdOrderByNameDesc(Long organizationId,Long categoryId);
    List<ProductEntity> findByOrganizationIdAndCategoryIdOrderByPnameAsc(Long organizationId,Long categoryId);
    List<ProductEntity> findByOrganizationIdAndCategoryIdOrderByPnameDesc(Long organizationId,Long categoryId);


    List<ProductEntity> findByIdIn(List<Long> ids);
    List<ProductEntity> findByIdInOrderByIdAsc(List<Long> ids);
    List<ProductEntity> findByIdInOrderByIdDesc(List<Long> ids);
    List<ProductEntity> findByIdInOrderByNameAsc(List<Long> ids);
    List<ProductEntity> findByIdInOrderByNameDesc(List<Long> ids);
    List<ProductEntity> findByIdInOrderByPnameAsc(List<Long> ids);
    List<ProductEntity> findByIdInOrderByPnameDesc(List<Long> ids);

//    List<ProductEntity> findByIdInOrderByPriceAsc(List<Long> ids);
//    List<ProductEntity> findByIdInOrderByPriceDesc(List<Long> ids);

    List<ProductEntity> findByIdInAndCategoryId(List<Long> ids, Long categoryId);
    List<ProductEntity> findByIdInAndCategoryIdOrderByIdAsc(List<Long> ids, Long categoryId);
    List<ProductEntity> findByIdInAndCategoryIdOrderByIdDesc(List<Long> ids, Long categoryId);
    List<ProductEntity> findByIdInAndCategoryIdOrderByNameAsc(List<Long> ids, Long categoryId);
    List<ProductEntity> findByIdInAndCategoryIdOrderByNameDesc(List<Long> ids, Long categoryId);
    List<ProductEntity> findByIdInAndCategoryIdOrderByPnameAsc(List<Long> ids, Long categoryId);
    List<ProductEntity> findByIdInAndCategoryIdOrderByPnameDesc(List<Long> ids, Long categoryId);

//    List<ProductEntity> findByIdInAndCategoryIdOrderByPriceeAsc(List<Long> ids,Long categoryId);
//    List<ProductEntity> findByIdInAndCategoryIdOrderByPriceDesc(List<Long> ids,Long categoryId);

}



