package com.nasnav.service;

import com.nasnav.dto.CountriesRepObj;
import com.nasnav.dto.CountryDTO;
import com.nasnav.dto.CountryInfoDTO;
import com.nasnav.dto.SubAreasRepObj;
import com.nasnav.dto.request.organization.SubAreasUpdateDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AddressService {
    Map<String, CountriesRepObj> getCountries(Boolean hideEmptyCities, Long organizationId);
    List<SubAreasRepObj> getOrgSubAreas(Long areaId, Long cityId, Long countryId);
    void addCountries(List<CountryDTO> dto);
    void addCountry(CountryInfoDTO dto);
    void removeCountry(Long id, String type);
    void updateSubAreas(SubAreasUpdateDTO subAreas);
    void deleteSubAreas(Set<Long> subAreas);
}
