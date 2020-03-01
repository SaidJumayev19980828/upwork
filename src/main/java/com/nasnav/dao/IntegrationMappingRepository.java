package com.nasnav.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
	
	List<IntegrationMappingEntity> findByOrganizationIdAndMappingType_typeNameAndRemoteValueIn(Long orgId,
			String value, List<String> remoteValues);
	
	Optional<IntegrationMappingEntity> findByOrganizationIdAndMappingType_typeNameAndRemoteValueIgnoreCase(Long orgId
			, String value, String externalShopId);
	
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

	Page<IntegrationMappingEntity> findByOrganizationId(Long orgId, Pageable pageable);

	Page<IntegrationMappingEntity> findByOrganizationIdAndMappingType_typeName(Long orgId, String mappingtype,
			Pageable pageable);
}
