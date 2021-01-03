package com.nasnav.dao;

import com.nasnav.dto.OrganizationImagesRepresentationObject;
import com.nasnav.persistence.OrganizationImagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrganizationImagesRepository extends JpaRepository<OrganizationImagesEntity, Long> {

    @Query("select new com.nasnav.dto.OrganizationImagesRepresentationObject(i.id, i.type, i.uri, f.mimetype) " +
            " from OrganizationImagesEntity i left join fetch FileEntity f on f.url = i.uri" +
            " where i.organizationEntity.id = :orgId and i.shopsEntity is null and i.type not in :types")
    List<OrganizationImagesRepresentationObject> getByOrgIdAndTypeNotIn(@Param("orgId") Long orgId,
                                                                        @Param("types") List<Integer> types);

    List<OrganizationImagesEntity> findByOrganizationEntityIdAndShopsEntityNullAndTypeNotIn(Long id, List<Integer> types);
    List<OrganizationImagesEntity> findByShopsEntityIdAndTypeNot(Long id, Integer type);


    List<OrganizationImagesEntity> findByOrganizationEntity_Id(Long id);

    List<OrganizationImagesEntity> findByOrganizationEntityIdAndShopsEntityNullAndTypeOrderByIdDesc(Long id, Integer type);
}
