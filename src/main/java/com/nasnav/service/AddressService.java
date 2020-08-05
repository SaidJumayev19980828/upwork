package com.nasnav.service;

import com.nasnav.dao.AddressRepository;
import com.nasnav.dao.AreaRepository;
import com.nasnav.dao.CityRepository;
import com.nasnav.dao.CountryRepository;
import com.nasnav.dto.*;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.AreasEntity;
import com.nasnav.persistence.CitiesEntity;
import com.nasnav.persistence.CountriesEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.google.common.primitives.Longs.asList;
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


    @Transactional
    public void addCountries(List<CountryDTO> dto) {
        for(CountryDTO coutnry : dto) {
            createCountry(new CountryInfoDTO(coutnry.getId(), coutnry.getName()));
            for (CityDTO city : coutnry.getCities()) {
                createCity(new CountryInfoDTO(city.getId(), city.getName(), coutnry.getId()));
                for (AreaDTO area : city.getAreas()) {
                    createArea(new CountryInfoDTO(area.getId(), area.getName(), city.getId()));
                }
            }
        }
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
        CitiesEntity city;
        if (cityRepo.existsByNameIgnoreCase(dto.getName())) {
            city = cityRepo.findByNameIgnoreCase(dto.getName());
        } else {
            city = new CitiesEntity();
        }
        city.setId(dto.getId());
        city.setName(dto.getName());
        if (isBlankOrNull(dto.getParentId()) && city.getCountriesEntity() == null) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0003, "country");
        }
        if (!isBlankOrNull(dto.getParentId())) {
            if (!countryRepo.existsById(dto.getParentId())) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0006, "country", dto.getParentId());
            }
            city.setCountriesEntity(countryRepo.findById(dto.getParentId()).get());
        }
        cityRepo.save(city);
    }


    private void createArea(CountryInfoDTO dto) {
        AreasEntity area;
        if (areaRepo.existsByNameIgnoreCase(dto.getName())) {
            area = areaRepo.findByNameIgnoreCase(dto.getName());
        } else {
            area = new AreasEntity();
        }
        area.setId(dto.getId());
        area.setName(dto.getName());
        if (isBlankOrNull(dto.getParentId()) && area.getCitiesEntity() == null) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0003, "city");
        }
        if (!isBlankOrNull(dto.getParentId())) {
            if (!cityRepo.existsById(dto.getParentId())) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0006, "city", dto.getParentId());
            }
            area.setCitiesEntity(cityRepo.findById(dto.getParentId()).get());
        }
        areaRepo.save(area);
    }


    public void removeCountry(Long id, String type) {
        if (type.equals("country")) {
            deleteCountry(id);
        } else if (type.equals("city")) {
            deleteCity(id);
        } else if (type.equals("area")) {
            deleteArea(id);
        }
    }


    private void deleteCountry(Long id) {
        List<Long> countryAreas = countryRepo.findAreasByCountryId(id);
        if (addressRepo.existsByAreasEntity_IdIn(countryAreas)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0007, "country");
        }
        countryRepo.deleteById(id);
    }


    private void deleteCity(Long id) {
        List<Long> cityAreas = areaRepo.findAreasByCityId(id);
        if (addressRepo.existsByAreasEntity_IdIn(cityAreas)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0007, "city");
        }
        cityRepo.deleteById(id);
    }


    private void deleteArea(Long id) {
        if (addressRepo.existsByAreasEntity_IdIn(asList(id))) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0007, "area");
        }
        areaRepo.deleteById(id);
    }
}
