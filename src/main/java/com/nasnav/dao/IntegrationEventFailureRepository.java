package com.nasnav.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.IntegrationEventFailureEntity;

public interface IntegrationEventFailureRepository extends JpaRepository<IntegrationEventFailureEntity, Long> {

	Page<IntegrationEventFailureEntity> findByOrganizationIdAndEventType(Long org_id, String event_type,
			Pageable pageable);

	Page<IntegrationEventFailureEntity> findByOrganizationId(Long org_id, Pageable pageable);

}
