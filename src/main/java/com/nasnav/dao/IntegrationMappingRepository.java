package com.nasnav.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nasnav.persistence.IntegrationMappingEntity;
import com.nasnav.persistence.IntegrationMappingTypeEntity;

public interface IntegrationMappingRepository extends JpaRepository<IntegrationMappingEntity, Long> {

	Optional<IntegrationMappingEntity> findByOrganizationIdAndMappingType_typeNameAndLocalValue(Long orgId, String typeName,
			String localValue);

	Optional<IntegrationMappingEntity> findByOrganizationIdAndMappingType_typeNameAndRemoteValue(Long orgId,
			String value, String remoteValue);
	
	
	@Modifying
	@Query("DELETE FROM IntegrationMappingEntity m "
			+ " where m.organizationId = :orgId "
			+ " and m.localValue = :localValue "
			+ " and m.mappingType = :type")
	void deleteByLocalValue(
			@Param("orgId") Long orgId
			, @Param("type")IntegrationMappingTypeEntity typeId
			, @Param("localValue") String localValue);

	
	
	
	@Modifying
	@Query("DELETE FROM IntegrationMappingEntity m "
			+ " where m.organizationId = :orgId "
			+ " and m.remoteValue = :remoteValue "
			+ " and m.mappingType = :type")
	void deleteByRemoteValue(
			@Param("orgId") Long orgId
			, @Param("type")IntegrationMappingTypeEntity typeId
			, @Param("remoteValue") String remoteValue);
}
