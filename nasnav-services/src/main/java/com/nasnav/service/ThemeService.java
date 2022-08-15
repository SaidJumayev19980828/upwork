package com.nasnav.service;

import com.nasnav.dto.OrganizationThemesSettingsDTO;
import com.nasnav.dto.ThemeClassDTO;
import com.nasnav.dto.ThemeDTO;
import com.nasnav.dto.request.theme.OrganizationThemeClass;
import com.nasnav.dto.response.OrgThemeRepObj;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.ThemeClassResponse;
import com.nasnav.response.ThemeResponse;

import java.util.List;

public interface ThemeService {
    List<ThemeClassDTO> listThemeClasses();
    List<ThemeDTO> listThemes(Integer classId);
    ThemeClassResponse updateThemeClass(ThemeClassDTO dto) throws BusinessException;
    ThemeResponse updateTheme(String uid, ThemeDTO dto) throws BusinessException;
    void deleteThemeClass(Integer id) throws BusinessException;
    void deleteTheme(String themeId) throws BusinessException;
    List<ThemeClassDTO> getOrgThemeClasses(Long orgId) throws BusinessException;
    void assignOrgThemeClass(OrganizationThemeClass orgThemeClassDTO) throws BusinessException;
    void changeOrgTheme(OrganizationThemesSettingsDTO dto) throws BusinessException;
    List<OrgThemeRepObj> getOrgThemes();
}
