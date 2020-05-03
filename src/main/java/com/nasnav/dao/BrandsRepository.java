package com.nasnav.dao;

import com.nasnav.persistence.BrandsEntity;
import com.nasnav.persistence.ProductEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface BrandsRepository extends CrudRepository<BrandsEntity,Long> {

    List<BrandsEntity> findByOrganizationEntity_Id(Long organizationEntity_Id);

    boolean existsByIdAndOrganizationEntity_Id(Long brandId, Long orgId);

    @Query("SELECT brand.id FROM BrandsEntity brand where brand.categoryId = :categoryId")
    List<Long> getBrandsByCategoryId(@Param("categoryId") Integer categoryId);

    
    @Query("select b.id FROM BrandsEntity b where b.name = :brandName")
	Long findByName(@Param("brandName") String brandName);

	boolean existsByNameIgnoreCaseAndOrganizationEntity_id(String brandName, Long orgId);

	@Query("select b.id FROM BrandsEntity b where UPPER(b.name) = UPPER(:brandName)")
	Long findByNameIgnoreCase(@Param("brandName")String brandName);

	List<BrandsEntity> findByNameIn(Set<String> newBrands);

	@Query(value = "select p.id from products p where p.brand_id = :brandId", nativeQuery = true)
	List<Long> getProductsByBrandId(@Param("brandId") Long brandId);

	@Query(value = "select s.id from shops s where s.brand_id = :brandId", nativeQuery = true)
	List<Long> getShopsByBrandId(@Param("brandId") Long brandId);
}
