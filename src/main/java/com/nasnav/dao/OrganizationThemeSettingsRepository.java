package com.nasnav.dao;

import com.nasnav.persistence.OrganizationThemesSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationThemeSettingsRepository extends JpaRepository<OrganizationThemesSettingsEntity, Integer> {

    List<OrganizationThemesSettingsEntity> findByThemeId(Integer themeId);

    OrganizationThemesSettingsEntity findByOrganizationEntity_Id(Long orgId);

    Optional<OrganizationThemesSettingsEntity> findByOrganizationEntity_IdAndThemeId(Long orgId, Integer themeId);

    boolean existsByOrganizationEntity_IdAndThemeId(Long orgId, Integer themeId);

}
