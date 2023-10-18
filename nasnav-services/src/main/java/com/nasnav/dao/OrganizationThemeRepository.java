package com.nasnav.dao;

import com.nasnav.persistence.OrganizationThemeEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrganizationThemeRepository extends CrudRepository<OrganizationThemeEntity,Long> {

    Optional<OrganizationThemeEntity> findOneByOrganizationEntity_Id(Long OrganizationEntity_Id);


    @Query("SELECT ot.logo FROM OrganizationThemeEntity ot WHERE ot.organizationEntity.id = :organizationId ")
    List<String> getLogoByOrganizationEntity_Id(@Param("organizationId") Long organizationId);

}
