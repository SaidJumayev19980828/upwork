package com.nasnav.service;

import com.nasnav.dao.AddressRepository;
import com.nasnav.dto.AreasRepObj;
import com.nasnav.dto.CitiesRepObj;
import com.nasnav.persistence.CountriesEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.*;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepo;


    public Map getCountries() {

        Map<String, Map> countries = addressRepo.getCountries()
                                .stream()
                                .collect(toMap(CountriesEntity::getName, this::getCitiesMap));
        return countries;
    }


    private Map getCitiesMap(CountriesEntity countriesEntity) {
        Map<String, List<AreasRepObj>> cities = countriesEntity.getCities()
                .stream()
                .map(city -> (CitiesRepObj) city.getRepresentation())
                .collect(toMap(CitiesRepObj::getName, CitiesRepObj::getAreas));

        return cities;
    }
}
