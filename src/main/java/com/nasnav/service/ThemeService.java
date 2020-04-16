package com.nasnav.service;

import com.nasnav.dao.*;
import com.nasnav.dto.ThemeClassDTO;
import com.nasnav.dto.ThemeDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.ThemeClassEntity;
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

}
