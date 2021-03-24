package com.nasnav.dao;

import com.nasnav.persistence.AreasEntity;
import com.nasnav.persistence.SubAreasEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SubAreaRepository extends JpaRepository<SubAreasEntity, Long> {

    List<SubAreasEntity> findByAreaAndOrganization_Id(AreasEntity area, Long organizationId);
    List<SubAreasEntity> findByOrganization_Id(Long organizationId);
    List<SubAreasEntity> findByOrganization_IdAndArea_Id(Long organizationId, Long areaId);
    List<SubAreasEntity> findByOrganization_IdAndArea_CitiesEntity_Id(Long organizationId, Long cityId);
    List<SubAreasEntity> findByOrganization_IdAndArea_CitiesEntity_CountriesEntity_Id(Long organizationId, Long countryId);

    @Query("SELECT subArea FROM SubAreasEntity subArea " +
            " LEFT JOIN FETCH subArea.area area " +
            " LEFT JOIN subArea.organization org " +
            " WHERE org.id = :orgId " +
            " AND subArea.id = :id")
    Optional<SubAreasEntity> findByIdAndOrganization_Id(@Param("id")Long id, @Param("orgId")Long orgId);

    @Transactional
    @Modifying
    @Query(value = " DELETE SubAreasEntity subArea " +
            " where subArea.id in :subAreas")
    void deleteByIdIn(@Param("subAreas")Set<Long> subAreasToDelete);
}
