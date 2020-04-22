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
import com.nasnav.response.ThemeClassResponse;
import com.nasnav.response.ThemeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.lang.String.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @Autowired
    private SecurityService securityService;


    public List<ThemeClassDTO> listThemeClasses() {
        return themeClassRepo.findAll().stream()
                .map(themeClass -> (ThemeClassDTO)themeClass.getRepresentation())
                .collect(toList());
    }


    public List<ThemeDTO> listThemes() {
        return themesRepo.findAll().stream()
                .map(theme -> (ThemeDTO)theme.getRepresentation())
                .collect(toList());
    }


    public ThemeClassResponse updateThemeClass(ThemeClassDTO dto) throws BusinessException {
        Optional<ThemeClassEntity> optionalThemeClass;
        
        ThemeClassEntity themeClass;
        if (dto.getId() == null)
            themeClass = new ThemeClassEntity();
        else {
            optionalThemeClass = themeClassRepo.findById(dto.getId());

            checkThemeClassExistence(optionalThemeClass, dto.getId());

            themeClass = optionalThemeClass.get();
        }
        
        themeClass.setName(dto.getName());
        themeClass = themeClassRepo.save(themeClass);
        return new ThemeClassResponse(themeClass.getId());
    }


    public ThemeResponse updateTheme(ThemeDTO dto) throws BusinessException {
        Optional<ThemeEntity> optionalThemeEntity;
        ThemeEntity theme;
        if (dto.getId() == null) {
            theme = new ThemeEntity();
            if (dto.getThemeClassId() == null) {
                throw new BusinessException("Must provide theme_class_id!",
                        "MISSING_PARAM: theme_class_id", NOT_ACCEPTABLE);
            }
        }
        else {
            optionalThemeEntity = themesRepo.findById(dto.getId());

            checkThemeExistence(optionalThemeEntity, dto.getId());

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
            checkThemeClassExistence(themeClass, dto.getThemeClassId());
            theme.setThemeClassEntity(themeClass.get());
        }

        return theme;
    }


    public void deleteThemeClass(Integer id) throws BusinessException {
        Optional<ThemeClassEntity> entity = themeClassRepo.findById(id);
        checkThemeClassExistence(entity, id);

        if (themesRepo.countByThemeClassEntity_Id(id) > 0) {
            throw new BusinessException("There are themes linked to class: " + id,
                    "INVALID_OPERATION", NOT_ACCEPTABLE);
        }
        if (orgRepo.countByThemeClassesContains(entity.get()) > 0) {
            throw new BusinessException("There are organizations linked to class: " + id,
                    "INVALID_OPERATION", NOT_ACCEPTABLE);
        }

        themeClassRepo.delete(entity.get());
    }


    public void deleteTheme(Integer id) throws BusinessException {
        Optional<ThemeEntity> entity = themesRepo.findById(id);

        checkThemeExistence(entity, id);

        Set<Long> orgIds = orgThemeSettingsRepo.findOrganizationIdByThemeIdIn(id);
        if (!orgIds.isEmpty()) {
            throw new BusinessException("Theme is used by organization : " + orgIds.toString(),
                    "INVALID_PARAM: id", NOT_ACCEPTABLE);
        }
        themesRepo.delete(entity.get());

    }


    public List<ThemeClassDTO> getOrgThemeClasses(Long orgId) throws BusinessException {
        Optional<OrganizationEntity> optionalOrg = orgRepo.findById(orgId);
        checkOrgExistence(optionalOrg, orgId);
        OrganizationEntity org = optionalOrg.get();

        return org.getThemeClasses().stream()
                .map(c -> (ThemeClassDTO)c.getRepresentation())
                .collect(toList());
    }


    public void assignOrgThemeClass(Long orgId, Integer themeClassId) throws BusinessException {
        Optional<OrganizationEntity> optionalOrg = orgRepo.findById(orgId);
        checkOrgExistence(optionalOrg, orgId);
        OrganizationEntity org = optionalOrg.get();

        Optional<ThemeClassEntity> themeClass = themeClassRepo.findById(themeClassId);
        checkThemeClassExistence(themeClass, themeClassId);

        Set<ThemeClassEntity> orgClasses = org.getThemeClasses();
        if (orgClasses.contains(themeClass.get()))
            throw new BusinessException("Theme class is already assigned to organization!",
                    "INVALID_OPERATION", NOT_ACCEPTABLE);

        orgClasses.add(themeClass.get());
        orgRepo.save(org);
    }


    public void removeOrgThemeClass(Long orgId, Integer themeClassId) throws BusinessException {
        Optional<OrganizationEntity> optionalOrg = orgRepo.findById(orgId);
        checkOrgExistence(optionalOrg, orgId);
        OrganizationEntity org = optionalOrg.get();


        Optional<ThemeClassEntity> themeClass = themeClassRepo.findById(themeClassId);
        checkThemeClassExistence(themeClass, themeClassId);

        Set<ThemeClassEntity> orgClasses = org.getThemeClasses();
        if (!orgClasses.contains(themeClass.get())) {
            throw new BusinessException(
                    format("Theme class %d not assigned to organization %d !", themeClassId, orgId),
                    "INVALID_OPERATION", NOT_ACCEPTABLE);
        }
        orgClasses.remove(themeClass.get());
        orgRepo.save(org);
    }


    private void checkThemeClassExistence(Optional<ThemeClassEntity> themeClass, Integer id) throws BusinessException {
        if (!themeClass.isPresent())
            throw new BusinessException(
                    format("Provided theme_class_id (%d) doesn't match any existing theme class!", id),
                    "INVALID_PARAM: class_id", NOT_ACCEPTABLE);
    }


    private void checkOrgExistence(Optional<OrganizationEntity> org, Long orgId) throws BusinessException {
        if(!org.isPresent())
            throw new BusinessException(
                    format("No organization found with provided id %d", orgId),
                    "INVALID_PARAM: org_id", NOT_FOUND);
    }


    private void checkThemeExistence(Optional<ThemeEntity> theme, Integer id) throws BusinessException {
        if (!theme.isPresent())
            throw new BusinessException(
                    format("Provided theme_id %d doesn't match any existing theme!", id),
                    "INVALID_PARAM: theme_id", NOT_ACCEPTABLE);
    }


    @Transactional
    public void changeOrgTheme(OrganizationThemesSettingsDTO dto) throws BusinessException {
        OrganizationEntity org = securityService.getCurrentUserOrganization();

        Optional<ThemeEntity> theme = themesRepo.findById(dto.getThemeId());
        checkThemeExistence(theme, dto.getThemeId());

        ThemeClassEntity themeClass = theme.get().getThemeClassEntity();
        Set<ThemeClassEntity> availableThemeClasses = org.getThemeClasses();
        if(!availableThemeClasses.contains(themeClass)) {
            throw new BusinessException(
                    format("Organization %d doesn't have permission to use theme %d!", org.getId(), theme.get().getId()),
                    "INVALID_PARAM: theme_id", NOT_ACCEPTABLE);
        }


        OrganizationThemesSettingsEntity orgThemeSetting =
        		orgThemeSettingsRepo
        			.findByOrganizationEntity_IdAndThemeId(org.getId(), dto.getThemeId())
        			.orElse(new OrganizationThemesSettingsEntity());

        orgThemeSetting.setOrganizationEntity(org);
        orgThemeSetting.setThemeId(dto.getThemeId());

        if (dto.getSettings() != null) {
            orgThemeSetting.setSettings(dto.getSettings());
        } else {
            orgThemeSetting.setSettings(theme.get().getDefaultSettings());
        }

        org.setThemeId(dto.getThemeId());
        orgRepo.save(org);

        orgThemeSettingsRepo.save(orgThemeSetting);
    }
}
