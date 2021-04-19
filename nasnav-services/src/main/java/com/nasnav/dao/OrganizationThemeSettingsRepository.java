package com.nasnav.dao;

import com.nasnav.persistence.OrganizationThemesSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OrganizationThemeSettingsRepository extends JpaRepository<OrganizationThemesSettingsEntity, Integer> {

    @Query("select orgThemeSetting.organizationEntity.id from OrganizationThemesSettingsEntity orgThemeSetting where orgThemeSetting.theme.id in :themeId")
    Set<Long> findOrganizationIdByThemeIdIn(@Param("themeId") Integer themeId);

    @Query(value = "select ots " +
            " from OrganizationThemesSettingsEntity ots " +
            " left join fetch ots.theme t " +
            " left join fetch t.themeClassEntity themeClass " +
            " where ots.organizationEntity.id = :orgId")
    List<OrganizationThemesSettingsEntity> findByOrganizationEntity_Id(@Param("orgId") Long orgId);

    Optional<OrganizationThemesSettingsEntity> findByOrganizationEntity_IdAndThemeId(Long orgId, Integer themeId);

    boolean existsByOrganizationEntity_IdAndThemeId(Long orgId, Integer themeId);
}
