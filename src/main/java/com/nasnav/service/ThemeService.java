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

import static java.util.Optional.ofNullable;

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

    @Autowired
    private SecurityService securityService;


    public List<ThemeClassDTO> listThemeClasses() {
        return themeClassRepo.findAll().stream()
                .map(themeClass -> (ThemeClassDTO)themeClass.getRepresentation())
                .collect(Collectors.toList()); //TODO: >>> static import
    }


    public List<ThemeDTO> listThemes() {
        return themesRepo.findAll().stream()
                .map(theme -> (ThemeDTO)theme.getRepresentation())
                .collect(Collectors.toList()); //TODO: >>> static import
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
            
          //TODO: >>> This is clear enough, but another version can make use of Optional methods like this.
          //use the one that you like
//            themeClassRepo
//            	.findById(dto.getId())
//            	.orElseThrow(() ->
//            		new BusinessException(
//            				"Provided theme_class_id doesn't match any existing theme class!"
//            				, "INVALID_PARAM: class_id"
//            				, NOT_ACCEPTABLE));
        }
        
        themeClass.setName(dto.getName());
        themeClass = themeClassRepo.save(themeClass);
        return new ThemeResponse(themeClass.getId());
    }


    public ThemeResponse updateTheme(ThemeDTO dto) throws BusinessException {
        Optional<ThemeEntity> optionalThemeEntity;
        ThemeEntity theme;
        if (dto.getId() == null) {
            theme = new ThemeEntity();
            if (dto.getThemeClassId() == null)
            	//TODO: >>> use static import for  NOT_ACCEPTABLE, it makes the code more readable.
            	//you don't need to make it by hand, intelliJ should have a short cut that creates the static import when hovering over NOT_ACCEPTABLE
            	//TODO: >>> use {} around "if" body, even if it is a single line, better safe than sorry.
                throw new BusinessException("Must provide theme_class_id!",
                        "MISSING_PARAM: theme_class_id", HttpStatus.NOT_ACCEPTABLE);
        }
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

        //TODO: >>> if you are just checking the existence of themes, then you may use countByThemeClassEntity_Id, as we don't
        //need the query to return themes data.
        if (themesRepo.findByThemeClassEntity_Id(id).isEmpty())
            themeClassRepo.delete(entity.get());
        else
        	//TODO: >>> - static imports for constants
        	//TODO: >>> - id in the message so we can revise logs
        	//TODO: >>> - {} around if-else bodies
            throw new BusinessException("There are themes linked to this class!",
                    "INVALID_OPERATION", HttpStatus.NOT_ACCEPTABLE);
        //TODO check if org is using the theme class
        //TODO: >>> yep
    }


    public void deleteTheme(Integer id) throws BusinessException {
        Optional<ThemeEntity> entity = themesRepo.findById(id);
        List<OrganizationThemesSettingsEntity> orgs;
        //TODO: >>> if you need only the id's , you can return it using JPQL instead of querying all the data 
        //check ProductVariantsRepository.findVariantIdByProductIdIn for example.
        if (entity.isPresent()) {
            orgs = orgThemeSettingsRepo.findByThemeId(id);
            if (!orgs.isEmpty()) {
                List<Long> orgIds = orgs.stream()
                        .map(org -> org.getOrganizationEntity().getId())  
                        .collect(Collectors.toList()); ///TODO: >>> static import of toList() for more readable code	
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
                .collect(Collectors.toList()); //TODO: >>> static imports for toList()
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
        	//TODO: >>> static import for NOT_ACCEPTABLE
        	//TODO: >>> add class and organization id to the message, so we can check the logs
        	//TODO: >>> {} for if-condition body
            throw new BusinessException("Theme class not assigned to organization!",
                    "INVALID_OPERATION", HttpStatus.NOT_ACCEPTABLE);

        orgClasses.remove(themeClass.get());
        orgRepo.save(org);
    }


    private void checkThemeClassExistence(Optional<ThemeClassEntity> themeClass) throws BusinessException {
        if (!themeClass.isPresent())
        	//TODO: >>> use static import for  NOT_ACCEPTABLE, it makes the code more readable.
        	//you don't need to make it by hand, intelliJ should have a short cut that creates the static import when hovering over NOT_ACCEPTABLE
        	
        	//TODO: >>> provide the id in the error message, so we can know which them caused the error in logs.
        	//you may need to use static import of String.format, it makes things a bit clearer than concatenation
            throw new BusinessException("Provided theme_class_id doesn't match any existing theme class!",
                    "INVALID_PARAM: class_id", HttpStatus.NOT_ACCEPTABLE); 
    }


    private void checkOrgExistence(Optional<OrganizationEntity> org) throws BusinessException {
        if(!org.isPresent())
        	//TODO: >>> static imports for NOT_FOUND - use intellij short cuts
        	//TODO: >>> add the organization id using String.format to the message, so we can check the logs
            throw new BusinessException("No organization found with provided id",
                    "INVALID_PARAM: org_id", HttpStatus.NOT_FOUND);
    }


    private void checkThemeExistence(Optional<ThemeEntity> theme) throws BusinessException {
    	//TODO : >>> same notes for checkThemeClassExistence
        if (!theme.isPresent())
            throw new BusinessException("Provided theme_id doesn't match any existing theme!",
                    "INVALID_PARAM: theme_id", HttpStatus.NOT_ACCEPTABLE);
    }


    public void changeOrgTheme(OrganizationThemesSettingsDTO dto) throws BusinessException {
        OrganizationEntity org = securityService.getCurrentUserOrganization();

        Optional<ThemeEntity> theme = themesRepo.findById(dto.getThemeId());
        checkThemeExistence(theme);

        ThemeClassEntity themeClass = theme.get().getThemeClassEntity();
        Set<ThemeClassEntity> availableThemeClasses = org.getThemeClasses();
        if(!availableThemeClasses.contains(themeClass))
        	//TODO: >>> static import
        	//TODO: >>> organization and theme id's in the message
        	//TODO: >>> {} for if-condition body 
            throw new BusinessException("Organization doesn't have permission to use this theme!",
                    "INVALID_PARAM: theme_id", HttpStatus.NOT_ACCEPTABLE);

        //-------------------------------
        Optional<OrganizationThemesSettingsEntity> orgThemeSettings
                = orgThemeSettingsRepo.findByOrganizationEntity_IdAndThemeId(org.getId(), dto.getThemeId());

        OrganizationThemesSettingsEntity orgThemeSetting = new OrganizationThemesSettingsEntity();
        
        
        if (orgThemeSettings.isPresent())
        	//TODO: >>> {} around if-condition body
            orgThemeSetting = orgThemeSettings.get();
        
        //TODO:>>> better to use Optional methods like this
//        OrganizationThemesSettingsEntity orgThemeSetting =
//        		orgThemeSettingsRepo
//        			.findByOrganizationEntity_IdAndThemeId(org.getId(), dto.getThemeId())
//        			.orElse(new OrganizationThemesSettingsEntity());
        //-------------------------------
        orgThemeSetting.setOrganizationEntity(org);
        orgThemeSetting.setThemeId(dto.getThemeId());

        if (dto.getSettings() != null) {
            orgThemeSetting.setSettings(dto.getSettings());
        } else {
            orgThemeSetting.setSettings(theme.get().getDefaultSettings());
        }

        orgThemeSettingsRepo.save(orgThemeSetting);
        
        //TODO: >>> you need to set the theme to the organization itself in organization entity
        //TODO: >>> there should be a test covering this point
        //TODO: >>> this should be Transactional, the settings and the organization entity should change together
    }
}
