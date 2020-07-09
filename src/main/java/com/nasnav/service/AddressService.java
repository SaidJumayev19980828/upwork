package com.nasnav.service;

import com.nasnav.dao.AddressRepository;
import com.nasnav.dao.AreaRepository;
import com.nasnav.dao.CityRepository;
import com.nasnav.dao.CountryRepository;
import com.nasnav.dto.AreasRepObj;
import com.nasnav.dto.CitiesRepObj;
import com.nasnav.dto.CountriesRepObj;
import com.nasnav.dto.CountryInfoDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.AreasEntity;
import com.nasnav.persistence.CitiesEntity;
import com.nasnav.persistence.CountriesEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepo;

    @Autowired
    private CountryRepository countryRepo;

    @Autowired
    private CityRepository cityRepo;

    @Autowired
    private AreaRepository areaRepo;

    public Map<String, CountriesRepObj> getCountries() {

        Map<String, CountriesRepObj> countries = addressRepo.getCountries()
                                .stream()
                                .collect( toMap(CountriesEntity::getName, this::getCountriesRepObj));
        return countries;
    }


    private CountriesRepObj getCountriesRepObj(CountriesEntity countriesEntity) {
        CountriesRepObj country = new CountriesRepObj();
        country.setId(countriesEntity.getId());
        country.setName(countriesEntity.getName());
        country.setCities(countriesEntity.getCities().stream()
                .collect( toMap(CitiesEntity::getName, this::getCitiesRepObj)));
        return country;
    }

    private CitiesRepObj getCitiesRepObj(CitiesEntity city) {
        CitiesRepObj obj = new CitiesRepObj();
        obj.setId(city.getId());
        obj.setName(city.getName());
        obj.setAreas(getAreasMap(city));

        return obj;
    }


    private Map getAreasMap(CitiesEntity city) {
        Map<String, AreasRepObj> areas = city.getAreas()
                .stream()
                .collect( toMap(AreasEntity::getName, c-> (AreasRepObj) c.getRepresentation()));
        return areas;
    }


    public void addCountry(CountryInfoDTO dto) {
        if(isBlankOrNull(dto.getType()))
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, TYP$0001, "country, city, area");
        if (dto.getType().equals("country")) {
            createCountry(dto);
        } else if (dto.getType().equals("city")) {
            createCity(dto);
        } else if (dto.getType().equals("area")) {
            createArea(dto);
        }
    }


    private void createCountry(CountryInfoDTO dto) {
        if (countryRepo.existsByNameIgnoreCase(dto.getName().toUpperCase())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0001,"country", dto.getName());
        }
        CountriesEntity country = new CountriesEntity();
        country.setId(dto.getId());
        country.setName(dto.getName());
        countryRepo.save(country);
    }


    private void createCity(CountryInfoDTO dto) {
        if (cityRepo.existsByNameIgnoreCase(dto.getName())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0001,"city", dto.getName());
        }
        CitiesEntity city = new CitiesEntity();
        city.setName(dto.getName());
        if (!countryRepo.existsById(dto.getParentId())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0003, "country");
        }
        city.setCountriesEntity(countryRepo.findById(dto.getParentId()).get());
        cityRepo.save(city);
    }


    private void createArea(CountryInfoDTO dto) {
        if (areaRepo.existsByNameIgnoreCase(dto.getName())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0001,"area", dto.getName());
        }
        AreasEntity area = new AreasEntity();
        area.setName(dto.getName());
        if (!cityRepo.existsById(dto.getParentId())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0003, "city");
        }
        area.setCitiesEntity(cityRepo.findById(dto.getParentId()).get());
        areaRepo.save(area);
    }
}
