package com.nasnav.service;

import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.OrganizationThemeSettingsRepository;
import com.nasnav.dao.ThemeClassRepository;
import com.nasnav.dao.ThemesRepository;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.nasnav.cache.Caches.ORGANIZATIONS_BY_ID;
import static com.nasnav.cache.Caches.ORGANIZATIONS_BY_NAME;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ThemeServiceImpl implements ThemeService{

    private static final Logger logger = LogManager.getLogger();
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
                .map(ThemeClassEntity::getRepresentation)
                .collect(toList());
    }


    public List<ThemeDTO> listThemes(Integer classId) {
        return ofNullable(classId)
                .map(themesRepo::findByThemeClassEntity_Id)
                .orElse(themesRepo.findAll())
                .stream()
                .map(theme -> (ThemeDTO)theme.getRepresentation())
                .collect(toList());
    }


    
    
    @CacheEvict(allEntries = true, cacheNames = { ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID})
    public ThemeClassResponse updateThemeClass(ThemeClassDTO dto) {
        ThemeClassEntity themeClass = new ThemeClassEntity();
        if (dto.getId() != null) {
            themeClass = themeClassRepo.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, THEME$0001, dto.getId()));
        }
        
        themeClass.setName(dto.getName());
        themeClass = themeClassRepo.save(themeClass);
        return new ThemeClassResponse(themeClass.getId());
    }

    
    

    @CacheEvict(allEntries = true, cacheNames = { ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID})
    public ThemeResponse updateTheme(String uid, ThemeDTO dto) throws BusinessException {
        validateThemeUpdateDTO(uid, dto);

        ThemeEntity theme = themesRepo.findByUid(uid)
                .orElseGet(ThemeEntity::new);

        setThemeProperties(theme, dto);

        return new ThemeResponse(themesRepo.save(theme).getUid());
    }

    private void validateThemeUpdateDTO(String uid, ThemeDTO dto) {
        if (uid == null) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$PRAM$0001, "uid");
        }
        try {
            Integer.parseInt(uid);
        } catch (NumberFormatException e){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$PRAM$0002, "uid");
        }
        if (dto.getThemeClassId() == null) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$PRAM$0001, "theme_class_id");
        }
    }

    private void setThemeProperties(ThemeEntity theme, ThemeDTO dto) {
        if (dto.getUid() != null) {
            if (themesRepo.existsByUid(dto.getUid()))
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, THEME$0004);
            theme.setUid(dto.getUid());
        }

        if (dto.getName() != null)
            theme.setName(dto.getName());

        if (dto.getPreviewImage() != null)
            theme.setPreviewImage(dto.getPreviewImage());

        if (dto.getDefaultSettings() != null)
            theme.setDefaultSettings(dto.getDefaultSettings());

        if (dto.getThemeClassId() != null) {
            ThemeClassEntity themeClass = themeClassRepo.findById(dto.getThemeClassId())
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, THEME$0001, dto.getThemeClassId()));

            theme.setThemeClassEntity(themeClass);
        }
    }


    public void deleteThemeClass(Integer id) {
        ThemeClassEntity themeClass = themeClassRepo.findById(id)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, THEME$0001, id));

        if (themesRepo.countByThemeClassEntity_Id(id) > 0) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, THEME$0002, "themes", id);
        }
        if (orgRepo.countByThemeClassesContains(themeClass) > 0) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, THEME$0002, "organizations", id);
        }

        themeClassRepo.delete(themeClass);
    }


    public void deleteTheme(String themeId) {
        try {
            int id = Integer.parseInt(themeId);
            Set<Long> orgIds = orgRepo.findByThemeId(id);
            if (!orgIds.isEmpty()) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$THEME$0002, orgIds.toString());
            }
            orgThemeSettingsRepo.deleteByTheme_Id(id);
        } catch (NumberFormatException e) {
        }

        ThemeEntity entity = themesRepo.findByUid(themeId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, THEME$0003, themeId));
        themesRepo.delete(entity);
    }


    public List<ThemeClassDTO> getOrgThemeClasses(Long orgId) throws BusinessException {
        Optional<OrganizationEntity> optionalOrg = orgRepo.findById(orgId);
        checkOrgExistence(optionalOrg, orgId);
        OrganizationEntity org = optionalOrg.get();

        return org.getThemeClasses().stream()
                .map(ThemeClassEntity::getRepresentation)
                .collect(toList());
    }


    @Transactional
    @CacheEvict(allEntries = true, cacheNames = { ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID})
    public void assignOrgThemeClass(OrganizationThemeClass orgThemeClassDTO) throws BusinessException {
        Optional<OrganizationEntity> optionalOrg = orgRepo.findById(orgThemeClassDTO.getOrgId());
        checkOrgExistence(optionalOrg, orgThemeClassDTO.getOrgId());
        OrganizationEntity org = optionalOrg.get();
        List<Integer> classIds = new ArrayList<>(orgThemeClassDTO.getClassIds());
        List<ThemeClassEntity> newThemeClasses = themeClassRepo.findByIdIn(classIds);
        checkThemeClassesExist(orgThemeClassDTO.getClassIds(), newThemeClasses);

        Set<ThemeClassEntity> orgClasses = org.getThemeClasses();
        checkThemeExistenceInOrg(org, classIds, orgClasses, newThemeClasses);

        orgClasses.clear();
        orgClasses.addAll(newThemeClasses);
        orgRepo.save(org);
    }


    private void checkThemeExistenceInOrg(OrganizationEntity org, List<Integer> classIds,
                                          Set<ThemeClassEntity> orgClasses, List<ThemeClassEntity> newThemeClasses) {

        boolean themeExistInNewClasses = themesRepo.existsByUidAndThemeClassEntity_IdIn(org.getThemeId()+"", classIds);
        if (!themeExistInNewClasses) {
            List<ThemeClassEntity> deletedThemeClasses = new ArrayList<>(orgClasses);
            deletedThemeClasses.removeAll(newThemeClasses);
            List<Integer> deletedClassIds = deletedThemeClasses.stream().map(ThemeClassEntity::getId).collect(toList());
            boolean themeExistInDeletedClasses = themesRepo.existsByUidAndThemeClassEntity_IdIn(org.getThemeId()+"", deletedClassIds);
            if (themeExistInDeletedClasses) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$THEME$0001, org.getThemeId(), org.getId());
            }
        }
    }


    private void checkThemeClassesExist(List<Integer> requestIds, List<ThemeClassEntity> themeClass) throws BusinessException {
        List<Integer> currentIds = themeClass.stream().map(c -> c.getId()).collect(toList());
        requestIds.removeAll(currentIds);

        if (!requestIds.isEmpty()) {
            throw new BusinessException(format("Provided class ids %s doesn't have corresponding classes", requestIds.toString()),
                    "INVALID_PARAM: classId", NOT_ACCEPTABLE);
        }
    }

    private void checkOrgExistence(Optional<OrganizationEntity> org, Long orgId) throws BusinessException {
        if(!org.isPresent())
            throw new BusinessException(
                    format("No organization found with provided id %d", orgId),
                    "INVALID_PARAM: org_id", NOT_FOUND);
    }

    @Transactional
    @CacheEvict(allEntries = true, cacheNames = { ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID})
    public void changeOrgTheme(OrganizationThemesSettingsDTO dto) throws BusinessException {
        OrganizationEntity org = securityService.getCurrentUserOrganization();

        ThemeEntity theme = themesRepo.findByUid(dto.getThemeId())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, THEME$0003, dto.getThemeId()+""));

        ThemeClassEntity themeClass = theme.getThemeClassEntity();
        Set<ThemeClassEntity> availableThemeClasses = org.getThemeClasses();
        if(!availableThemeClasses.contains(themeClass)) {
            throw new BusinessException(
                    format("Organization %d doesn't have permission to use theme %d!", org.getId(), theme.getId()),
                    "INVALID_PARAM: theme_id", NOT_ACCEPTABLE);
        }


        OrganizationThemesSettingsEntity orgThemeSetting =
        		orgThemeSettingsRepo
        			.findByOrganizationEntity_IdAndThemeId(org.getId(), theme.getId())
        			.orElse(new OrganizationThemesSettingsEntity());

        orgThemeSetting.setOrganizationEntity(org);
        orgThemeSetting.setTheme(theme);

        if (dto.getSettings() != null) {
            orgThemeSetting.setSettings(dto.getSettings());
        } else {
            orgThemeSetting.setSettings(theme.getDefaultSettings());
        }

        org.setThemeId(Integer.parseInt(dto.getThemeId()));
        orgRepo.save(org);

        orgThemeSettingsRepo.save(orgThemeSetting);
    }


    public List<OrgThemeRepObj> getOrgThemes() {
        OrganizationEntity org = securityService.getCurrentUserOrganization();

        Map<String, String> orgThemesSettings = orgThemeSettingsRepo
                .findByOrganizationEntity_Id(org.getId())
                .stream()
                .collect(toMap(s -> s.getTheme().getUid(), OrganizationThemesSettingsEntity::getSettings));

        return org
                .getThemeClasses()
                .stream()
                .map(ThemeClassEntity::getThemes)
                .flatMap(Set::stream)
                .map(OrgThemeRepObj::new)
                .map(t -> setThemeSettings(t, orgThemesSettings) )
                .collect(toList());
    }

    private OrgThemeRepObj setThemeSettings(OrgThemeRepObj theme, Map<String, String> orgThemesSettings) {
        try {
            Map defaultSettings = new JSONObject(theme.getDefaultSettingsString()).toMap();
            theme.setDefaultSettings(defaultSettings);
            if(orgThemesSettings.get(theme.getUid()) != null) {
                Map settings = new JSONObject(orgThemesSettings.get(theme.getUid())).toMap();
                theme.setSettings(settings);
            }
        } catch (Exception e) {
            logger.error(e,e);
        }

        return theme;
    }
}
