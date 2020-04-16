package com.nasnav.service;

import com.nasnav.dao.*;
import com.nasnav.dto.ThemeClassDTO;
import com.nasnav.dto.ThemeDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.ThemeClassEntity;
import com.nasnav.persistence.ThemeEntity;
import com.nasnav.response.ThemeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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
        ThemeClassEntity themeClass;
        if (dto.getId() == null)
            themeClass = new ThemeClassEntity();
        else {
            if(!themeClassRepo.existsById(dto.getId()))
               throw new BusinessException("", "INVALID_PARAM: id", HttpStatus.NOT_ACCEPTABLE);

            themeClass = themeClassRepo.findById(dto.getId()).get();
        }
        themeClass.setName(dto.getName());
        themeClass = themeClassRepo.save(themeClass);
        return new ThemeResponse(themeClass.getId());
    }


    public ThemeResponse updateTheme(ThemeDTO dto) throws BusinessException {
        ThemeEntity theme;
        if (dto.getId() == null)
            theme = new ThemeEntity();
        else {
            if(!themesRepo.existsById(dto.getId()))
                throw new BusinessException("Theme not found!", "INVALID_PARAM: id", HttpStatus.NOT_ACCEPTABLE);

            theme = themesRepo.findById(dto.getId()).get();
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
            if (themeClass.isPresent())
                theme.setThemeClassEntity(themeClass.get());
            else
                throw new BusinessException("Provided theme_class_id doesn't match any existing theme class!",
                        "INVALID_PARAM: theme_class_id", HttpStatus.NOT_ACCEPTABLE);
        }

        return theme;
    }


    public void deleteThemeClass(Integer id) throws BusinessException {
        Optional<ThemeClassEntity> entity = themeClassRepo.findById(id);
        if (entity.isPresent()) {
            if (themesRepo.findByThemeClassEnitiy_Id(id).isEmpty())
                themeClassRepo.delete(entity.get());
            else
                throw new BusinessException("There are themes linked to this class!",
                        "INVALID_OPERATION", HttpStatus.NOT_ACCEPTABLE);
        }
        else
            throw new BusinessException("No theme class found with id "+id,
                    "INVALID_PARAM: id", HttpStatus.NOT_ACCEPTABLE);
    }


    public void deleteTheme(Integer id) throws BusinessException {
        Optional<ThemeEntity> entity = themesRepo.findById(id);
        if (entity.isPresent())
            themesRepo.delete(entity.get()); //TODO check if organization is using this theme
        else
            throw new BusinessException("No theme found with id "+id,
                    "INVALID_PARAM: id", HttpStatus.NOT_ACCEPTABLE);
    }
}
