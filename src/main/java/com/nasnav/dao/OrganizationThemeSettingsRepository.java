package com.nasnav.dao;

import com.nasnav.persistence.OrganizationThemesSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganizationThemeSettingsRepository extends JpaRepository<OrganizationThemesSettingsEntity, Integer> {

    List<OrganizationThemesSettingsEntity> findByThemeId(Integer themeId);

}
