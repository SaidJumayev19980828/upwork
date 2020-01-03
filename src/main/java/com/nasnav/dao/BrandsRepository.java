package com.nasnav.dao;

import com.nasnav.persistence.BrandsEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BrandsRepository extends CrudRepository<BrandsEntity,Long> {

    List<BrandsEntity> findByOrganizationEntity_Id(Long organizationEntity_Id);

    @Query("SELECT brand.id FROM BrandsEntity brand where brand.categoryId = :categoryId")
    List<Long> getBrandsByCategoryId(@Param("categoryId") Integer categoryId);

    
    @Query("select b.id FROM BrandsEntity b where b.name = :brandName")
	Long findByName(@Param("brandName") String brandName);

	boolean existsByNameIgnoreCaseAndOrganizationEntity_id(String brandName, Long orgId);
}
