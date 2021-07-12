package com.nasnav.dao;

import com.nasnav.persistence.IntegrationParamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IntegrationParamRepository extends JpaRepository<IntegrationParamEntity, Long> {
	
	List<IntegrationParamEntity> findByOrganizationId(Long id);
	List<IntegrationParamEntity> findByType_typeName(String typeName);
	Optional<IntegrationParamEntity> findByOrganizationIdAndType_typeName(Long orgId, String typeName);
	void deleteByOrganizationIdAndType_typeName(Long orgId, String paramTypeName);
	void deleteByOrganizationId(Long organizationId);
}
