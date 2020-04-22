package com.nasnav.dao;

import com.nasnav.persistence.OrganizationThemesSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OrganizationThemeSettingsRepository extends JpaRepository<OrganizationThemesSettingsEntity, Integer> {

    List<OrganizationThemesSettingsEntity> findByThemeId(Integer themeId);

    @Query("select orgThemeSetting.organizationEntity.id from OrganizationThemesSettingsEntity orgThemeSetting where orgThemeSetting.themeId in :themeId")
    Set<Long> findOrganizationIdByThemeIdIn(@Param("themeId") Integer themeId);

    OrganizationThemesSettingsEntity findByOrganizationEntity_Id(Long orgId);

    Optional<OrganizationThemesSettingsEntity> findByOrganizationEntity_IdAndThemeId(Long orgId, Integer themeId);

    boolean existsByOrganizationEntity_IdAndThemeId(Long orgId, Integer themeId);

}
