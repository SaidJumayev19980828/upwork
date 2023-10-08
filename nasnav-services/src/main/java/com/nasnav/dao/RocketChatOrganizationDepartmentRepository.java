package com.nasnav.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.RocketChatOrganizationDepartmentEntity;

import reactor.core.publisher.Mono;

public interface RocketChatOrganizationDepartmentRepository extends JpaRepository<RocketChatOrganizationDepartmentEntity, Long> {
	Mono<RocketChatOrganizationDepartmentEntity> findByOrganizationId(Long orgId);
}
