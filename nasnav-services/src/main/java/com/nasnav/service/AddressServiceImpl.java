package com.nasnav.service;

import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.commons.utils.FunctionalUtils;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.dto.request.organization.SubAreasUpdateDTO;
import com.nasnav.enumerations.Settings;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import lombok.Data;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.cache.annotation.CacheResult;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.primitives.Longs.asList;
import static com.nasnav.cache.Caches.COUNTRIES;
import static com.nasnav.commons.utils.CollectionUtils.processInBatches;
import static com.nasnav.commons.utils.CollectionUtils.streamJsonArrayElements;
import static com.nasnav.commons.utils.EntityUtils.*;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.enumerations.Settings.ALLOWED_COUNTRIES;
import static com.nasnav.enumerations.Settings.HIDE_AREAS_WITH_NO_SUB_AREA;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

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
    private OrganizationRepository orgRepo;

    @Autowired
    private OrganizationService orgService;

    @Autowired
    private SecurityService securityService;


    @CacheResult(cacheName = COUNTRIES)
    public Map<String, CountriesRepObj> getCountries(Boolean hideEmptyCities, Long organizationId) {
        var orgSupport = getOrganizationAreaSupport(hideEmptyCities, organizationId);
        return addressRepo
                .getCountries(orgSupport.countries)
                .stream()
                .collect( toMap(CountriesEntity::getName, country -> getCountriesRepObj(country, orgSupport)));
    }



    private OrganizationAreaSupport getOrganizationAreaSupport(Boolean hideEmptyCities, Long organizationId) {
        var orgId = ofNullable(organizationId).orElse(0L);
        var orgSubareas = getOrganizationSubAreas(orgId);
        var supportedCountriesByOrg = getSupportedCountriesIdsByOrg(orgId);
        var hideAreasWithNoSubAreas = getHideAreasWithNoSubAreas(orgId);
        return new OrganizationAreaSupport(orgId, orgSubareas, supportedCountriesByOrg, hideEmptyCities, hideAreasWithNoSubAreas);
    }



    private Boolean getHideAreasWithNoSubAreas(Long orgId) {
        return orgService
                .getOrganizationSettingValue(orgId, HIDE_AREAS_WITH_NO_SUB_AREA)
                .map(Boolean::parseBoolean)
                .orElse(false);
    }



    private List<Long> getSupportedCountriesIdsByOrg(Long orgId) {
        return orgRepo.findById(orgId)
                .map(OrganizationEntity::getExtraInfo)
                .map(this::parseSettingValue)
                .filter(EntityUtils::notNullNorEmpty)
                .orElseGet(countryRepo::findAllCountriesIds);
    }



    private List<Long> parseSettingValue(String setting){
        return ofNullable(setting)
                .map(JSONObject::new)
                .filter(o -> o.has(ALLOWED_COUNTRIES.name()))
                .map(o -> o.getJSONArray(ALLOWED_COUNTRIES.name()))
                .map(JSONArray::spliterator)
                .map(iterator -> StreamSupport.stream(iterator, false))
                .orElse(Stream.empty())
                .map(Object::toString)
                .map(Long::parseLong)
                .collect(toList());
    }

    private Map<Long, List<SubAreasRepObj>> getOrganizationSubAreas(Long orgId) {
        return subAreaRepo.findByOrganization_Id(orgId)
                .stream()
                .map(sub -> (SubAreasRepObj)sub.getRepresentation())
                .collect(groupingBy(SubAreasRepObj::getAreaId));
    }


    private CountriesRepObj getCountriesRepObj(CountriesEntity countriesEntity, OrganizationAreaSupport orgSupport) {
        var cities = getCities(countriesEntity, orgSupport);

        var country = new CountriesRepObj();
        country.setId(countriesEntity.getId());
        country.setName(countriesEntity.getName());
        country.setCities(cities);
        return country;
    }



    private Map<String, CitiesRepObj> getCities(CountriesEntity countriesEntity, OrganizationAreaSupport orgSupport) {
        return countriesEntity
                .getCities()
                .stream()
                .filter(city -> !(city.getAreas().isEmpty() && orgSupport.hideEmptyCities))
                .collect(
                        collectingAndThen(
                            toMap(CitiesEntity::getName, city -> getCitiesRepObj(city, orgSupport), FunctionalUtils::getFirst),
                            TreeMap::new
                ));
    }



    private CitiesRepObj getCitiesRepObj(CitiesEntity city, OrganizationAreaSupport orgSupport) {
        var obj = new CitiesRepObj();
        obj.setId(city.getId());
        obj.setName(city.getName());
        obj.setAreas(getAreasMap(city, orgSupport));
        return obj;
    }



    private Map<String, AreasRepObj> getAreasMap(CitiesEntity city, OrganizationAreaSupport orgSupport) {
        return city
                .getAreas()
                .stream()
                .filter(area -> isSupportedByOrganization(area, orgSupport))
                .sorted(comparing(AreasEntity::getId))
                .collect(
                        collectingAndThen(
                                toMap(AreasEntity::getName, area -> getAreasRepObj(area, orgSupport), FunctionalUtils::getFirst),
                                TreeMap::new
                        ));
    }



    private boolean isSupportedByOrganization(AreasEntity area, OrganizationAreaSupport orgSupport) {
        return !orgSupport.hideAreasWithNoSubAreas
                || areaHasSubAreas(area, orgSupport);
    }



    private boolean areaHasSubAreas(AreasEntity area, OrganizationAreaSupport orgSupport) {
        return orgSupport.subAreas.containsKey(area.getId());
    }


    private AreasRepObj getAreasRepObj(AreasEntity area, OrganizationAreaSupport orgSupport){
        var obj = new AreasRepObj();
        obj.setId(area.getId());
        obj.setName(area.getName());
        obj.setSubAreas(getSubAreasMap(area, orgSupport));
        return obj;
    }



    private Map<String, SubAreasRepObj> getSubAreasMap(AreasEntity area, OrganizationAreaSupport orgSupport) {
        return ofNullable(orgSupport.subAreas.get(area.getId()))
                .orElse(emptyList())
                .stream()
                .collect(
                        collectingAndThen(
                            toMap(SubAreasRepObj::getName, sub -> sub, FunctionalUtils::getFirst),
                            TreeMap::new
                        ));
    }




    @Transactional
    @CacheEvict(cacheNames = COUNTRIES, allEntries=true)
    public void addCountries(List<CountryDTO> dto) {
        dto.forEach(this::createOrUpdateCountryData);
    }



    private void createOrUpdateCountryData(CountryDTO country){
        var countryEntity = getOrCreateCountry(new CountryInfoDTO(country.getId(), country.getName(), country.getIsoCode(), country.getCurrency()));
        ofNullable(country.getCities())
                .orElse(emptyList())
                .forEach(city -> createOrUpdateCityData(city, countryEntity.getId()));
    }



    private void createOrUpdateCityData(CityDTO city, Long countryId){
        var cityEntity = createCity(new CountryInfoDTO(city.getId(), city.getName(), countryId));
        ofNullable(city.getAreas())
                .orElse(emptyList())
                .forEach(area -> createOrUpdateArea(area, cityEntity.getId()));
    }



    private void createOrUpdateArea(AreaDTO area, Long cityId){
        createArea(new CountryInfoDTO(area.getId(), area.getName(), cityId));
    }
    
    

    @Transactional
    @CacheEvict(cacheNames = COUNTRIES, allEntries=true)
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
        var name = dto.getName();
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
        var country = new CountriesEntity();
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
    @CacheEvict(cacheNames = COUNTRIES, allEntries=true)
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
    @CacheEvict(value=COUNTRIES, allEntries=true)
    public void updateSubAreas(SubAreasUpdateDTO subAreas) {
        validateSubAreas(subAreas);
        updateExistingAndAddNewAreas(subAreas);
    }


    @Override
    @Transactional
    @CacheEvict(value=COUNTRIES, allEntries=true)
    public void deleteSubAreas(Set<Long> subAreas) {
        validateProvidedSubAreasExisting(new HashSet<>(subAreas));

        clearSubAreaFromAddresses(subAreas);
        subAreaRepo.deleteByIdIn(subAreas);
    }


    private void validateProvidedSubAreasExisting(Set<Long> nonExistingSubAreas) {
        var orgId = securityService.getCurrentUserOrganizationId();
        var currentSubAreas = getOrganizationSubAreasIds(orgId);

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
        var org = securityService.getCurrentUserOrganization();
        var entity =
                isNullOrZero(subArea.getId()) ?
                        new SubAreasEntity() :
                        subAreaRepo
                        .findByIdAndOrganization_Id(subArea.getId(), org.getId())
                        .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, SUBAREA$001, subArea.getId(), org.getId()));
        if (!isNullOrZero(subArea.getAreaId())) {
            var area =
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
        var orgId = securityService.getCurrentUserOrganizationId();
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
        var countryAreas = countryRepo.findAreasByCountryId(id);
        if (addressRepo.existsByAreasEntity_IdIn(countryAreas)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0007, "country");
        }
        countryRepo.deleteById(id);
    }


    private void deleteCity(Long id) {
        var cityAreas = areaRepo.findAreasByCityId(id);
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



@Data
class  OrganizationAreaSupport{
    Long orgId;
    Map<Long, List<SubAreasRepObj>> subAreas;
    List<Long> countries;
    boolean hideEmptyCities;
    boolean hideAreasWithNoSubAreas;

    public OrganizationAreaSupport(Long orgId, Map<Long, List<SubAreasRepObj>> subAreas, List<Long> countries
            , boolean hideEmptyCities, boolean hideAreasWithNoSubAreas) {
        this.orgId = orgId;
        this.subAreas = subAreas;
        this.countries = countries;
        this.hideEmptyCities = hideEmptyCities;
        this.hideAreasWithNoSubAreas = hideAreasWithNoSubAreas;
    }
}
