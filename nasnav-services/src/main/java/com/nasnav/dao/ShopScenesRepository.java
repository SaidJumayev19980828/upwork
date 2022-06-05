package com.nasnav.dao;

import com.nasnav.persistence.ShopScenesEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShopScenesRepository extends CrudRepository<ShopScenesEntity, Long> {

    @Query(value = "select scene from ShopScenesEntity scene" +
                   " left join fetch  scene.shopSectionsEntity section" +
                   " left join fetch  section.shopFloorsEntity floor" +
                   " where scene.id = :id and scene.organizationEntity.id = :orgId and floor.organizationEntity.id = :orgId")
    Optional<ShopScenesEntity> findByIdAndOrganizationEntity_Id(@Param("id") Long id,
                                                                @Param("orgId") Long orgId);
    Optional<ShopScenesEntity> findByIdAndShopSectionsEntity_Id(Long id, Long sectionId);

    @Query(value = "select scene.image from ShopScenesEntity scene" +
            " left join scene.shopSectionsEntity section" +
            " left join section.shopFloorsEntity floor" +
            " where scene.organizationEntity.id = :orgId and floor.organizationEntity.id = :orgId")
    List<String> findByOrganizationEntity_Id(@Param("orgId") Long orgId);

    @Query(value = "select scene.image from ShopScenesEntity scene" +
            " left join scene.shopSectionsEntity section" +
            " left join section.shopFloorsEntity floor" +
            " where scene.organizationEntity.id = :orgId and floor.organizationEntity.id = :orgId and floor.shopThreeSixtyEntity.shopsEntity.id = :shopId")
    List<String> findByOrganizationEntity_IdAndShopId(@Param("orgId") Long orgId, @Param("shopId") Long shopId);
}
