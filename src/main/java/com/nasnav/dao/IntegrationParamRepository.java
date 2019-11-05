package com.nasnav.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.IntegrationParamEntity;

public interface IntegrationParamRepository extends JpaRepository<IntegrationParamEntity, Long> {
	
	List<IntegrationParamEntity> findByOrganizationId(Long id);
	List<IntegrationParamEntity> findByType_typeName(String typeName);
}
