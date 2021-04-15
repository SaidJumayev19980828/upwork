package com.nasnav.dao;

import com.nasnav.persistence.IntegrationParamTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface IntegrationParamTypeRepostory extends JpaRepository<IntegrationParamTypeEntity, Long> {
	Set<IntegrationParamTypeEntity> findByIsMandatory(Boolean isMandatory);
	Optional<IntegrationParamTypeEntity> findByTypeName(String typeName);
}
