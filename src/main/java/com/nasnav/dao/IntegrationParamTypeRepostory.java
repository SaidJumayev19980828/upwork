package com.nasnav.dao;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.IntegrationParamTypeEntity;

public interface IntegrationParamTypeRepostory extends JpaRepository<IntegrationParamTypeEntity, Long> {
	Set<IntegrationParamTypeEntity> findByIsMandatory(Boolean isMandatory);
}
