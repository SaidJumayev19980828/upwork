package com.nasnav.dao;

import com.nasnav.persistence.ProductEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProductRepository extends CrudRepository<ProductEntity,Long> {

    List<ProductEntity> findByOrganizationIdOrderByIdAsc(Long organizationId);
    List<ProductEntity> findByOrganizationIdOrderByIdDesc(Long organizationId);

    List<ProductEntity> findByOrganizationIdOrderByNameAsc(Long organizationId);
    List<ProductEntity> findByOrganizationIdOrderByNameDesc(Long organizationId);

//    List<ProductEntity> findByOrganizationIdOrderByPNameAsc(Long organizationId);
//    List<ProductEntity> findByOrganizationIdOrderByPNameDesc(Long organizationId);

    List<ProductEntity> findByOrganizationIdAndCategoryIdOrderByIdAsc(Long organizationId,Long categoryId);
    List<ProductEntity> findByOrganizationIdAndCategoryIdOrderByIdDesc(Long organizationId,Long categoryId);

    List<ProductEntity> findByOrganizationIdAndCategoryIdOrderByNameAsc(Long organizationId,Long categoryId);
    List<ProductEntity> findByOrganizationIdAndCategoryIdOrderByNameDesc(Long organizationId,Long categoryId);

    List<ProductEntity> findByOrganizationIdAndCategoryIdOrderByPnameAsc(Long organizationId,Long categoryId);
    List<ProductEntity> findByOrganizationIdAndCategoryIdOrderByPnameDesc(Long organizationId,Long categoryId);

}
