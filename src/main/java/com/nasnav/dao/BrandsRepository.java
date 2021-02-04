package com.nasnav.dao;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.nasnav.service.model.IdAndNamePair;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.nasnav.persistence.BrandsEntity;
import com.nasnav.persistence.dto.query.result.products.BrandBasicData;
import org.springframework.transaction.annotation.Transactional;

public interface BrandsRepository extends CrudRepository<BrandsEntity,Long> {

    List<BrandsEntity> findByOrganizationEntity_IdAndRemovedOrderByPriorityDesc(Long organizationEntity_Id, Integer removed);
    Optional<BrandsEntity> findByIdAndOrganizationEntity_Id(Long id, Long orgId);

    boolean existsByIdAndOrganizationEntity_IdAndRemoved(Long brandId, Long orgId, Integer removed);
	boolean existsByIdAndRemoved(Long brandId, Integer removed);

	boolean existsByNameIgnoreCaseAndOrganizationEntity_idAndRemoved(String brandName, Long orgId, Integer removed);

	List<BrandsEntity> findByNameInAndRemoved(Set<String> newBrands, Integer removed);

	@Transactional
	@Modifying
	@Query(value = "update BrandsEntity b set b.removed = 1 where b.id = :id")
	void setBrandHidden(@Param("id") Long id);

	@Query("SELECT NEW com.nasnav.persistence.dto.query.result.products.BrandBasicData(brand.id, brand.name, org.id) "
			+ " FROM BrandsEntity brand "
			+ " left join brand.organizationEntity org"
			+ " WHERE brand.id in :ids and brand.removed = 0")
	List<BrandBasicData> findByIdIn(@Param("ids")List<Long> ids);

	@Query(value = "select new com.nasnav.service.model.IdAndNamePair(b.id, b.pname) from BrandsEntity b" +
			"  where b.organizationEntity.id = :orgId and b.removed = 0")
	List<IdAndNamePair> getBrandIdAndNamePairs(@Param("orgId") Long orgId);
}
