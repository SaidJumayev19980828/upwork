package com.nasnav.dao;

import com.nasnav.persistence.BrandsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BrandsRepository extends CrudRepository<BrandsEntity,Long> {

    List<BrandsEntity> findByOrganizationEntity_Id(Long organizationEntity_Id);

    /*@Query("SELECT brands.id FROM BrandsEntity brands where brands.categoryId = :categoryId")
    List<Long> getBrandsByCategoryId(@Param("categoryId") Long categoryId);*/
}
