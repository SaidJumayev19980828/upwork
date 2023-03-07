package com.nasnav.dao;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.nasnav.persistence.AddonEntity;
import com.nasnav.persistence.dto.query.result.products.ProductAddonBasicData;

public interface AddonsRepository extends CrudRepository<AddonEntity, Long> {

Optional<AddonEntity> findByIdAndOrganizationEntity_Id(Long id, Long orgId);
List<AddonEntity> findByOrganizationEntity_Id(Long orgId);

@Query("SELECT NEW com.nasnav.persistence.dto.query.result.products.ProductAddonBasicData(product.id , addons.id, addons.name) "
		+ " from ProductEntity product "
		+ " left join product.addons addons "
		+ " where product.id in :productIds")
List<ProductAddonBasicData> getAddonsByProductIdIn(@Param("productIds")List<Long> productIds);

@Query("SELECT addon.id from AddonEntity addon where addon.id in :ids and addon.organizationEntity.id = :orgId")
List<Long> getExistingAddonsIds(@Param("ids") Set<Long> addonIds, @Param("orgId") Long orgId);

List<AddonEntity> findByIdInAndOrganizationEntity_Id(List<Long> ids, Long orgId);



}