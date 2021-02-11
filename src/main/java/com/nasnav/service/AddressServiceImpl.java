package com.nasnav.service;

import static com.google.common.primitives.Longs.asList;
import static com.nasnav.commons.utils.CollectionUtils.processInBatches;
import static com.nasnav.commons.utils.EntityUtils.*;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.*;

import com.nasnav.commons.utils.FunctionalUtils;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.dto.request.organization.SubAreasUpdateDTO;
import com.nasnav.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.exceptions.RuntimeBusinessException;

@Service
public class AddressServiceImpl implements AddressService{

    @Autowired
    private AddressRepository addressRepo;

    @Autowired
    private CountryRepository countryRepo;

    @Autowired
    private CityRepository cityRepo;

    @Autowired
    private AreaRepository areaRepo;

    @Autowired
    private SubAreaRepository subAreaRepo;

    @Autowired
    private SecurityService securityService;


    public Map<String, CountriesRepObj> getCountries(Boolean hideEmptyCities, Long organizationId) {
        return addressRepo
                .getCountries()
                .stream()
                .collect( toMap(CountriesEntity::getName, country -> getCountriesRepObj(country, hideEmptyCities,organizationId)));
    }


    private CountriesRepObj getCountriesRepObj(CountriesEntity countriesEntity, Boolean hideEmptyCities, Long organizationId) {
        Map<String, CitiesRepObj> cities = getCities(countriesEntity, hideEmptyCities, organizationId);

        CountriesRepObj country = new CountriesRepObj();
        country.setId(countriesEntity.getId());
        country.setName(countriesEntity.getName());
        country.setCities(cities);
        return country;
    }



    private Map<String, CitiesRepObj> getCities(CountriesEntity countriesEntity, Boolean hideEmptyCities, Long organizationId) {
        return countriesEntity
                .getCities()
                .stream()
                .filter(city -> !(city.getAreas().isEmpty() && hideEmptyCities))
                .collect(
                        collectingAndThen(
                            toMap(CitiesEntity::getName, city -> getCitiesRepObj(city, organizationId), FunctionalUtils::getFirst),
                            TreeMap::new
                ));
    }



    private CitiesRepObj getCitiesRepObj(CitiesEntity city, Long organizationId) {
        CitiesRepObj obj = new CitiesRepObj();
        obj.setId(city.getId());
        obj.setName(city.getName());
        obj.setAreas(getAreasMap(city, organizationId));
        return obj;
    }



    private Map<String, AreasRepObj> getAreasMap(CitiesEntity city, Long organizationId) {
        return city
                .getAreas()
                .stream()
                .collect(
                        collectingAndThen(
                                toMap(AreasEntity::getName, area -> getAreasRepObj(area, organizationId), FunctionalUtils::getFirst),
                                TreeMap::new
                        ));
    }



    private AreasRepObj getAreasRepObj(AreasEntity area, Long organizationId){
        AreasRepObj obj = new AreasRepObj();
        obj.setId(area.getId());
        obj.setName(area.getName());
        obj.setSubAreas(getSubAreasMap(area, organizationId));
        return obj;
    }



    private Map<String, SubAreasRepObj> getSubAreasMap(AreasEntity area, Long organizationId) {
        return subAreaRepo
                .findByAreaAndOrganization_Id(area, organizationId)
                .stream()
                .collect(
                        collectingAndThen(
                            toMap(SubAreasEntity::getName, sub -> (SubAreasRepObj)sub.getRepresentation(), FunctionalUtils::getFirst),
                            TreeMap::new
                        ));
    }




    @Transactional
    public void addCountries(List<CountryDTO> dto) {
        dto.forEach(this::createOrUpdateCountryData);
    }



    private void createOrUpdateCountryData(CountryDTO country){
        CountriesEntity countryEntity = getOrCreateCountry(new CountryInfoDTO(country.getId(), country.getName(), country.getIsoCode(), country.getCurrency()));
        ofNullable(country.getCities())
                .orElse(emptyList())
                .forEach(city -> createOrUpdateCityData(city, countryEntity.getId()));
    }



    private void createOrUpdateCityData(CityDTO city, Long countryId){
        CitiesEntity cityEntity = createCity(new CountryInfoDTO(city.getId(), city.getName(), countryId));
        ofNullable(city.getAreas())
                .orElse(emptyList())
                .forEach(area -> createOrUpdateArea(area, cityEntity.getId()));
    }



    private void createOrUpdateArea(AreaDTO area, Long cityId){
        createArea(new CountryInfoDTO(area.getId(), area.getName(), cityId));
    }
    
    

    @Transactional
    public void addCountry(CountryInfoDTO dto) {
        if(isBlankOrNull(dto.getType()))
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, TYP$0001, "country, city, area");
        if (dto.getType().equals("country")) {
            getOrCreateCountry(dto);
        } else if (dto.getType().equals("city")) {
            createCity(dto);
        } else if (dto.getType().equals("area")) {
            createArea(dto);
        }
    }


    
    
    
    private CountriesEntity getOrCreateCountry(CountryInfoDTO dto) {
    	String name = dto.getName();
    	if (isBlankOrNull(name)){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0008);
        }
    	return countryRepo
    			.findByNameIgnoreCase(name.toUpperCase())
				.orElseGet(() -> createNewCountry(dto.getId(), name, dto.getIsoCode(), dto.getCurrency()));
    }
    
    
    
    private CountriesEntity createNewCountry(Long id, String name, Integer isoCode, String currency) {
        if(anyIsNull(id, name, isoCode, currency)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$PRAM$0001, "id, name, iso_code, currency");
        }
    	CountriesEntity country = new CountriesEntity();
        country.setId(id);
        country.setName(name);
        country.setIsoCode(isoCode);
        country.setCurrency(currency);
        return countryRepo.save(country);
    }



    private CitiesEntity createCity(CountryInfoDTO dto) {
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
        return cityRepo.save(city);
    }



    private AreasEntity createArea(CountryInfoDTO dto) {
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
        return areaRepo.save(area);
    }


    @Transactional
    public void removeCountry(Long id, String type) {
        if (type.equals("country")) {
            deleteCountry(id);
        } else if (type.equals("city")) {
            deleteCity(id);
        } else if (type.equals("area")) {
            deleteArea(id);
        }
    }



    @Override
    @Transactional
    public void updateSubAreas(SubAreasUpdateDTO subAreas) {
        validateSubAreas(subAreas);
        updateExistingAndAddNewAreas(subAreas);
    }


    @Override
    @Transactional
    public void deleteSubAreas(Set<Long> subAreas) {
        validateProvidedSubAreasExisting(new HashSet<>(subAreas));

        clearSubAreaFromAddresses(subAreas);
        subAreaRepo.deleteByIdIn(subAreas);
    }


    private void validateProvidedSubAreasExisting(Set<Long> nonExistingSubAreas) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        Set<Long> currentSubAreas = getOrganizationSubAreasIds(orgId);

        nonExistingSubAreas.removeAll(currentSubAreas);
        if (nonExistingSubAreas.size() > 0) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, SUBAREA$003, nonExistingSubAreas.toString(), orgId);
        }
    }


    private void updateExistingAndAddNewAreas(SubAreasUpdateDTO subAreas) {
        ofNullable(subAreas.getSubAreas())
                .orElse(emptyList())
                .stream()
                .map(this::createOrUpdateSubAreaEntity)
                .collect(
                    collectingAndThen(
                        toList()
                        , subAreaRepo::saveAll
                ));
    }



    private void clearSubAreaFromAddresses(Set<Long> subAreasToDelete) {
        processInBatches(subAreasToDelete, 2000, batch -> addressRepo.clearSubAreasFromAddresses(new HashSet<>(batch)));
    }




    private Set<Long> getOrganizationSubAreasIds(Long orgId) {
        return subAreaRepo
                .findByOrganization_Id(orgId)
                .stream()
                .map(SubAreasEntity::getId)
                .collect(toSet());
    }



    private SubAreasEntity createOrUpdateSubAreaEntity(SubAreaDTO subArea){
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        SubAreasEntity entity =
                isNullOrZero(subArea.getId()) ?
                        new SubAreasEntity() :
                        subAreaRepo
                        .findByIdAndOrganization_Id(subArea.getId(), org.getId())
                        .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, SUBAREA$001, subArea.getId(), org.getId()));
        if (!isNullOrZero(subArea.getAreaId())) {
            AreasEntity area =
                    areaRepo
                            .findById(subArea.getAreaId())
                            .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, AREA$001, subArea.getAreaId()));
            entity.setArea(area);
        }
        if (subArea.isUpdated("name")) {
            entity.setName(subArea.getName());
        }
        entity.setOrganization(org);
        entity.setLatitude(subArea.getLatitude());
        entity.setLongitude(subArea.getLongitude());
        return entity;
    }



    private void validateSubAreas(SubAreasUpdateDTO subAreas) {
        ofNullable(subAreas.getSubAreas())
                .orElse(emptyList())
                .forEach(this::validateSubAreaDTO);
    }



    private void validateSubAreaDTO(SubAreaDTO subArea){
        if(isNullOrZero(subArea.getId())
                && (anyIsNull(subArea.getAreaId(), subArea.getName()) || subArea.getName().isEmpty())){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$PRAM$0001, subArea.toString());
        }
    }

    @Override
    public List<SubAreasRepObj> getOrgSubAreas(Long areaId, Long cityId, Long countryId) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        List<SubAreasEntity> subAreasEntities = new ArrayList<>();
        if (areaId != null) {
            subAreasEntities = subAreaRepo.findByOrganization_IdAndArea_Id(orgId, areaId);
        } else if (cityId != null) {
            subAreasEntities = subAreaRepo.findByOrganization_IdAndArea_CitiesEntity_Id(orgId, cityId);
        } else if (countryId != null) {
            subAreasEntities = subAreaRepo.findByOrganization_IdAndArea_CitiesEntity_CountriesEntity_Id(orgId, countryId);
        } else {
            subAreasEntities = subAreaRepo.findByOrganization_Id(orgId);
        }
        return subAreasEntities
                .stream()
                .map(subArea -> (SubAreasRepObj) subArea.getRepresentation())
                .collect(toList());
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
