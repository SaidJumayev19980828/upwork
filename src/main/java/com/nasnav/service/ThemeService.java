package com.nasnav.service;

import com.nasnav.dao.*;
import com.nasnav.dto.OrganizationThemesSettingsDTO;
import com.nasnav.dto.ThemeClassDTO;
import com.nasnav.dto.ThemeDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.OrganizationThemesSettingsEntity;
import com.nasnav.persistence.ThemeClassEntity;
import com.nasnav.persistence.ThemeEntity;
import com.nasnav.response.ThemeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ThemeService {

    @Autowired
    private ThemesRepository themesRepo;
    @Autowired
    private ThemeClassRepository themeClassRepo;
    @Autowired
    private OrganizationThemeSettingsRepository orgThemeSettingsRepo;
    @Autowired
    private OrganizationRepository orgRepo;


    public List<ThemeClassDTO> listThemeClasses() {
        return themeClassRepo.findAll().stream()
                .map(themeClass -> (ThemeClassDTO)themeClass.getRepresentation())
                .collect(Collectors.toList());
    }


    public List<ThemeDTO> listThemes() {
        return themesRepo.findAll().stream()
                .map(theme -> (ThemeDTO)theme.getRepresentation())
                .collect(Collectors.toList());
    }


    public ThemeResponse updateThemeClass(ThemeClassDTO dto) throws BusinessException {
        Optional<ThemeClassEntity> optionalThemeClass;
        ThemeClassEntity themeClass;
        if (dto.getId() == null)
            themeClass = new ThemeClassEntity();
        else {
            optionalThemeClass = themeClassRepo.findById(dto.getId());

            checkThemeClassExistence(optionalThemeClass);

            themeClass = optionalThemeClass.get();
        }
        themeClass.setName(dto.getName());
        themeClass = themeClassRepo.save(themeClass);
        return new ThemeResponse(themeClass.getId());
    }


    public ThemeResponse updateTheme(ThemeDTO dto) throws BusinessException {
        Optional<ThemeEntity> optionalThemeEntity;
        ThemeEntity theme;
        if (dto.getId() == null)
            theme = new ThemeEntity();
        else {
            optionalThemeEntity = themesRepo.findById(dto.getId());

            checkThemeExistence(optionalThemeEntity);

            theme = optionalThemeEntity.get();
        }

        theme = setThemeProperties(theme, dto);
        theme = themesRepo.save(theme);
        return new ThemeResponse(theme.getId());
    }


    private ThemeEntity setThemeProperties(ThemeEntity theme, ThemeDTO dto) throws BusinessException {
        if (dto.getName() != null)
            theme.setName(dto.getName());

        if (dto.getPreviewImage() != null)
            theme.setPreviewImage(dto.getPreviewImage());

        if (dto.getDefaultSettings() != null)
            theme.setDefaultSettings(dto.getDefaultSettings());

        if (dto.getThemeClassId() != null) {
            Optional<ThemeClassEntity> themeClass = themeClassRepo.findById(dto.getThemeClassId());
            checkThemeClassExistence(themeClass);
                theme.setThemeClassEntity(themeClass.get());
        }

        return theme;
    }


    public void deleteThemeClass(Integer id) throws BusinessException {
        Optional<ThemeClassEntity> entity = themeClassRepo.findById(id);
        checkThemeClassExistence(entity);

        if (themesRepo.findByThemeClassEnitiy_Id(id).isEmpty())
            themeClassRepo.delete(entity.get());
        else
            throw new BusinessException("There are themes linked to this class!",
                    "INVALID_OPERATION", HttpStatus.NOT_ACCEPTABLE);

    }


    public void deleteTheme(Integer id) throws BusinessException {
        Optional<ThemeEntity> entity = themesRepo.findById(id);
        List<OrganizationThemesSettingsEntity> orgs;
        if (entity.isPresent()) {
            orgs = orgThemeSettingsRepo.findByThemeId(id);
            if (!orgs.isEmpty()) {
                List<Long> orgIds = orgs.stream()
                        .map(org -> org.getOrganizationEntity().getId())
                        .collect(Collectors.toList());
                throw new BusinessException("Theme is used by organization : " + orgIds.toString(),
                        "INVALID_PARAM: id", HttpStatus.NOT_ACCEPTABLE);
            }
            themesRepo.delete(entity.get());
        }
        else
            throw new BusinessException("No theme found with id "+id,
                    "INVALID_PARAM: id", HttpStatus.NOT_ACCEPTABLE);
    }


    public List<ThemeClassDTO> getOrgThemeClasses(Long orgId) throws BusinessException {
        Optional<OrganizationEntity> optionalOrg = orgRepo.findById(orgId);
        checkOrgExistence(optionalOrg);
        OrganizationEntity org = optionalOrg.get();

        return org.getThemeClasses().stream()
                .map(c -> (ThemeClassDTO)c.getRepresentation())
                .collect(Collectors.toList());
    }


    public void assignOrgThemeClass(Long orgId, Integer themeClassId) throws BusinessException {
        Optional<OrganizationEntity> optionalOrg = orgRepo.findById(orgId);
        checkOrgExistence(optionalOrg);
        OrganizationEntity org = optionalOrg.get();

        Optional<ThemeClassEntity> themeClass = themeClassRepo.findById(themeClassId);
        checkThemeClassExistence(themeClass);

        Set<ThemeClassEntity> orgClasses = org.getThemeClasses();
        if (orgClasses.contains(themeClass.get()))
            throw new BusinessException("Theme class is already assigned to organization!",
                    "INVALID_OPERATION", HttpStatus.NOT_ACCEPTABLE);

        orgClasses.add(themeClass.get());
        orgRepo.save(org);
    }


    public void removeOrgThemeClass(Long orgId, Integer themeClassId) throws BusinessException {
        Optional<OrganizationEntity> optionalOrg = orgRepo.findById(orgId);
        checkOrgExistence(optionalOrg);
        OrganizationEntity org = optionalOrg.get();


        Optional<ThemeClassEntity> themeClass = themeClassRepo.findById(themeClassId);
        checkThemeClassExistence(themeClass);

        Set<ThemeClassEntity> orgClasses = org.getThemeClasses();
        if (!orgClasses.contains(themeClass.get()))
            throw new BusinessException("Theme class not assigned to organization!",
                    "INVALID_OPERATION", HttpStatus.NOT_ACCEPTABLE);

        orgClasses.remove(themeClass.get());
        orgRepo.save(org);
    }


    private void checkThemeClassExistence(Optional<ThemeClassEntity> themeClass) throws BusinessException {
        if (!themeClass.isPresent())
            throw new BusinessException("Provided theme_class_id doesn't match any existing theme class!",
                    "INVALID_PARAM: class_id", HttpStatus.NOT_ACCEPTABLE);
    }


    private void checkOrgExistence(Optional<OrganizationEntity> org) throws BusinessException {
        if(!org.isPresent())
            throw new BusinessException("No organization found with provided id",
                    "INVALID_PARAM: org_id", HttpStatus.NOT_FOUND);
    }


    private void checkThemeExistence(Optional<ThemeEntity> theme) throws BusinessException {
        if (!theme.isPresent())
            throw new BusinessException("Provided theme_id doesn't match any existing theme class!",
                    "INVALID_PARAM: theme_id", HttpStatus.NOT_ACCEPTABLE);
    }


    public void changeOrgTheme(OrganizationThemesSettingsDTO dto) {

    }
}
