package com.nasnav.dao;

import com.nasnav.persistence.OrganizationImagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationImagesRepository extends JpaRepository<OrganizationImagesEntity, Long> {

    List<OrganizationImagesEntity> findByOrganizationEntityIdAndShopsEntityNullAndTypeNotIn(Long id, List<Integer> types);
    List<OrganizationImagesEntity> findByShopsEntityIdAndTypeNot(Long id, Integer type);

    Optional<OrganizationImagesEntity> findByIdAndOrganizationEntity_Id(Long id, Long orgId);
    Optional<OrganizationImagesEntity> findByUriAndOrganizationEntity_Id(String uri, Long orgId);

    List<OrganizationImagesEntity> findByOrganizationEntity_Id(Long id);

    List<OrganizationImagesEntity> findByOrganizationEntityIdAndShopsEntityNullAndTypeOrderByIdDesc(Long id, Integer type);
}
