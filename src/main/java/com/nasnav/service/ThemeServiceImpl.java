package com.nasnav.service;

import com.nasnav.dao.*;
import com.nasnav.dto.OrganizationThemesSettingsDTO;
import com.nasnav.dto.ThemeClassDTO;
import com.nasnav.dto.ThemeDTO;
import com.nasnav.dto.request.theme.OrganizationThemeClass;
import com.nasnav.dto.response.OrgThemeRepObj;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.OrganizationThemesSettingsEntity;
import com.nasnav.persistence.ThemeClassEntity;
import com.nasnav.persistence.ThemeEntity;
import com.nasnav.response.ThemeClassResponse;
import com.nasnav.response.ThemeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.nasnav.cache.Caches.ORGANIZATIONS_BY_ID;
import static com.nasnav.cache.Caches.ORGANIZATIONS_BY_NAME;
import static com.nasnav.exceptions.ErrorCodes.ORG$THEME$0001;
import static java.lang.String.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ThemeServiceImpl implements ThemeService{

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


    public List<ThemeDTO> listThemes(Integer classId) {
        List<ThemeEntity> themesList;
        if (classId != null) {
            themesList = themesRepo.findByThemeClassEntity_Id(classId);
        } else {
            themesList = themesRepo.findAll();
        }
        return themesList.stream()
                .map(theme -> (ThemeDTO)theme.getRepresentation())
                .collect(toList());
    }


    
    
    @CacheEvict(allEntries = true, cacheNames = { ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID})
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

    
    

    @CacheEvict(allEntries = true, cacheNames = { ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID})
    public ThemeResponse updateTheme(ThemeDTO dto) throws BusinessException {
        Optional<ThemeEntity> optionalThemeEntity;
        ThemeEntity theme = new ThemeEntity();
        if (dto.getUid() == null) {
            throw new BusinessException("Must provide theme_id!",
                    "MISSING_PARAM: theme_id", NOT_ACCEPTABLE);
        }
        if (dto.getThemeClassId() == null) {
            throw new BusinessException("Must provide theme_class_id!",
                    "MISSING_PARAM: theme_class_id", NOT_ACCEPTABLE);
        }

        optionalThemeEntity = themesRepo.findByUid(dto.getUid());

        if (optionalThemeEntity.isPresent()) {
            theme = optionalThemeEntity.get();
        }

        theme = setThemeProperties(theme, dto);
        theme = themesRepo.save(theme);
        return new ThemeResponse(theme.getUid());
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

        theme.setUid(dto.getUid());

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


    public void deleteTheme(String themeId) throws BusinessException {
    	String id = ofNullable(themeId).orElse("-1");
        Optional<ThemeEntity> entity = themesRepo.findByUid(id);

        checkThemeExistence(entity, id);

        Set<Long> orgIds = orgThemeSettingsRepo.findOrganizationIdByThemeIdIn(entity.get().getId());
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


    
    @CacheEvict(allEntries = true, cacheNames = { ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID})
    public void assignOrgThemeClass(OrganizationThemeClass orgThemeClassDTO) throws BusinessException {
        Optional<OrganizationEntity> optionalOrg = orgRepo.findById(orgThemeClassDTO.getOrgId());
        checkOrgExistence(optionalOrg, orgThemeClassDTO.getOrgId());
        OrganizationEntity org = optionalOrg.get();

        List<ThemeClassEntity> newThemeClasses = themeClassRepo.findByIdIn(orgThemeClassDTO.getClassIds());
        checkThemeClassesExist(orgThemeClassDTO.getClassIds(), newThemeClasses);

        Set<ThemeClassEntity> orgClasses = org.getThemeClasses();

        Set<ThemeClassEntity> addedOrgClasses = new HashSet<>(newThemeClasses);
        addedOrgClasses.removeAll(orgClasses);
        List<Integer> addedClassesIds = addedOrgClasses.stream().map(ThemeClassEntity::getId).collect(toList());

        Set<ThemeClassEntity> deletedOrgClasses = new HashSet<>(orgClasses);
        deletedOrgClasses.removeAll(newThemeClasses);
        List<Integer> deletedClassesIds = deletedOrgClasses.stream().map(ThemeClassEntity::getId).collect(toList());

        boolean themeExistInDeletedClass = themesRepo.existsByUidAndThemeClassEntity_IdIn(org.getThemeId()+"", deletedClassesIds);
        boolean themeExistInAddedClass = themesRepo.existsByUidAndThemeClassEntity_IdIn(org.getThemeId()+"", addedClassesIds);

        if (themeExistInDeletedClass && !themeExistInAddedClass) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$THEME$0001, org.getThemeId(), org.getId());
        }
        orgClasses.removeAll(deletedOrgClasses);
        orgClasses.addAll(addedOrgClasses);
        orgRepo.save(org);
    }


    private void checkThemeClassesExist(List<Integer> requestIds, List<ThemeClassEntity> themeClass) throws BusinessException {
        List<Integer> currentIds = themeClass.stream().map(c -> c.getId()).collect(toList());
        requestIds.removeAll(currentIds);

        if (!requestIds.isEmpty()) {
            throw new BusinessException(format("Provided class ids %s doesn't have corresponding classes", requestIds.toString()),
                    "INVALID_PARAM: classId", NOT_ACCEPTABLE);
        }
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


    private void checkThemeExistence(Optional<ThemeEntity> theme, String id) throws BusinessException {
        if (!theme.isPresent())
            throw new BusinessException(
                    format("Provided theme_id %s doesn't match any existing theme!", id),
                    "INVALID_PARAM: theme_id", NOT_ACCEPTABLE);
    }


    @Transactional
    @CacheEvict(allEntries = true, cacheNames = { ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID})
    public void changeOrgTheme(OrganizationThemesSettingsDTO dto) throws BusinessException {
        OrganizationEntity org = securityService.getCurrentUserOrganization();

        Optional<ThemeEntity> theme = themesRepo.findByUid(dto.getThemeId());
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
        			.findByOrganizationEntity_IdAndThemeId(org.getId(), theme.get().getId())
        			.orElse(new OrganizationThemesSettingsEntity());

        orgThemeSetting.setOrganizationEntity(org);
        orgThemeSetting.setThemeId(theme.get().getId());

        if (dto.getSettings() != null) {
            orgThemeSetting.setSettings(dto.getSettings());
        } else {
            orgThemeSetting.setSettings(theme.get().getDefaultSettings());
        }

        org.setThemeId(Integer.parseInt(dto.getThemeId()));
        orgRepo.save(org);

        orgThemeSettingsRepo.save(orgThemeSetting);
    }


    public List<OrgThemeRepObj> getOrgThemes() {
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        List<Integer> organizationThemeClasses = org.getThemeClasses().stream()
                                                                   .map(ThemeClassEntity::getId)
                                                                   .collect(toList());
        return orgThemeSettingsRepo.findByThemeClasses(organizationThemeClasses);
    }

}
