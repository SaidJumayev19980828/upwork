package com.nasnav.dao;

import com.nasnav.dto.response.OrgThemeRepObj;
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

    @Query(value = "select new com.nasnav.dto.response.OrgThemeRepObj( t.uid, t.name, t.previewImage, t.defaultSettings, ots.settings, t.themeClassEntity.id) " +
            " from ThemeEntity t left join fetch OrganizationThemesSettingsEntity ots on ots.themeId = t.id" +
            " where ots.organizationEntity.id = :orgId")
    List<OrgThemeRepObj> findByOrganizationEntity_Id(@Param("orgId") Long orgId);

    Optional<OrganizationThemesSettingsEntity> findByOrganizationEntity_IdAndThemeId(Long orgId, Integer themeId);

    boolean existsByOrganizationEntity_IdAndThemeId(Long orgId, Integer themeId);

    @Query(value = "select new com.nasnav.dto.response.OrgThemeRepObj( t.uid, t.name, t.previewImage, t.defaultSettings," +
            " (select COALESCE(ots.settings, '{}') from  OrganizationThemesSettingsEntity ots where ots.themeId = t.id), t.themeClassEntity.id) " +
            " from ThemeEntity t " +
            " where t.themeClassEntity.id in :themeClasses")
    List<OrgThemeRepObj> findByThemeClasses(@Param("themeClasses") List<Integer> themeClasses);
}
