package com.nasnav.dao;

import com.nasnav.persistence.IntegrationMappingTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntegrationMappingTypeRepository extends JpaRepository<IntegrationMappingTypeEntity, Long> {

	Optional<IntegrationMappingTypeEntity> findByTypeName(String value);

}
