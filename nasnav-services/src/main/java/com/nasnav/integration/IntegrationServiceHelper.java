package com.nasnav.integration;

import com.nasnav.dao.IntegrationMappingRepository;
import com.nasnav.integration.enums.MappingType;
import com.nasnav.persistence.IntegrationMappingEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Service
public class IntegrationServiceHelper {

	@Autowired
	private IntegrationMappingRepository mappingRepo;
	
		
	
	public Map<String,String> getLocalMappedValues(Long orgId, MappingType type, List<String> externalValues) {
		return mappingRepo
				.findByOrganizationIdAndMappingType_typeNameAndRemoteValueIn(orgId, type.getValue(), externalValues)
				.stream()
				.filter(mapping -> mapping.getRemoteValue() != null)
				.collect(toMap(IntegrationMappingEntity::getRemoteValue, IntegrationMappingEntity::getLocalValue));
	}
}
