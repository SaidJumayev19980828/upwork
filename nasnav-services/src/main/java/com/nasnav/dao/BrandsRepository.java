package com.nasnav.dao;

import com.nasnav.dto.Organization_BrandRepresentationObject;
import com.nasnav.persistence.BrandsEntity;
import com.nasnav.service.model.IdAndNamePair;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BrandsRepository extends CrudRepository<BrandsEntity,Long> {

    List<BrandsEntity> findByOrganizationEntity_IdAndRemovedOrderByPriorityDesc(Long organizationEntity_Id, Integer removed);
	PageImpl<BrandsEntity> findByOrganizationEntity_IdInAndRemovedAndPriorityGreaterThanEqualOrderByPriorityDesc(List<Long> orgIds, Integer removed, Integer minPriority,Pageable page);
    Optional<BrandsEntity> findByIdAndOrganizationEntity_Id(Long id, Long orgId);

	@Query("select b from BrandsEntity b " +
			" left join fetch b.organizationEntity o "+
			" where b.id = :id and o.yeshteryState = 1")
	Optional<BrandsEntity> findYeshteryBrandById(@Param("id") Long id);

    boolean existsByIdAndOrganizationEntity_IdAndRemoved(Long brandId, Long orgId, Integer removed);
	boolean existsByIdAndRemoved(Long brandId, Integer removed);

	boolean existsByNameIgnoreCaseAndOrganizationEntity_idAndRemoved(String brandName, Long orgId, Integer removed);

	List<BrandsEntity> findByNameInAndRemoved(Set<String> newBrands, Integer removed);

	@Transactional
	@Modifying
	@Query(value = "update BrandsEntity b set b.removed = 1 where b.id = :id")
	void setBrandHidden(@Param("id") Long id);

	@Query("SELECT brand "
			+ " FROM BrandsEntity brand "
			+ " left join brand.organizationEntity org"
			+ " WHERE brand.id in :ids and brand.removed = 0")
	List<BrandsEntity> findByIdIn(@Param("ids")List<Long> ids);

	@Query(value = "select distinct new com.nasnav.service.model.IdAndNamePair(b.id, b.pname) from ProductEntity p left join p.brand b " +
			"  where b.organizationEntity.id = :orgId and b.removed = 0 and p.removed = 0")
	List<IdAndNamePair> getBrandIdAndNamePairs(@Param("orgId") Long orgId);

	@Query("select new com.nasnav.dto.Organization_BrandRepresentationObject(b.id, b.name, b.pname, b.categoryId, " +
			" b.logo, b.bannerImage, b.coverUrl, b.priority, org.name)"+
			" from BrandsEntity b " +
			" left join b.organizationEntity org " +
			" where org.yeshteryState = 1 and b.removed = 0 order by b.priority desc")
	PageImpl<Organization_BrandRepresentationObject> findByOrganizationEntity_YeshteryState(Pageable page);

	@Query("select new com.nasnav.dto.Organization_BrandRepresentationObject(b.id, b.name, b.pname, b.categoryId, " +
			" b.logo, b.bannerImage, b.coverUrl, b.priority, org.name)"+
			" from BrandsEntity b " +
			" left join b.organizationEntity org " +
			" where b.id in :ids and org.yeshteryState = 1 and b.removed = 0 order by b.priority desc")
	PageImpl<Organization_BrandRepresentationObject> findByIdInAndOrganizationEntity_YeshteryState(@Param("ids")Set<Long>ids,
																								   Pageable page);

	@Query("select new com.nasnav.dto.Organization_BrandRepresentationObject(b.id, b.name, b.pname, b.categoryId, " +
			" b.logo, b.bannerImage, b.coverUrl, b.priority, org.name)"+
			" from BrandsEntity b " +
			" left join b.organizationEntity org " +
			" where b.id in :ids and org.id = :orgId and org.yeshteryState = 1 and b.removed = 0 order by b.priority desc")
	PageImpl<Organization_BrandRepresentationObject> findByIdInAndYeshteryOrganization(@Param("ids")Set<Long>ids,
																					   @Param("orgId") Long orgId,
																					   Pageable page);

	@Query("select new com.nasnav.dto.Organization_BrandRepresentationObject(b.id, b.name, b.pname, b.categoryId, " +
			" b.logo, b.bannerImage, b.coverUrl, b.priority, org.name)"+
			" from BrandsEntity b " +
			" left join b.organizationEntity org " +
			" where org.id = :orgId and org.yeshteryState = 1 and b.removed = 0 order by b.priority desc")
	PageImpl<Organization_BrandRepresentationObject> findByYeshteryOrganization(@Param("orgId") Long orgId,
																				Pageable page);

	List<BrandsEntity> findByIdInAndRemoved(Set<Long> brands, int removed);
}
