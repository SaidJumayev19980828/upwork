package com.nasnav.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.RocketChatOrganizationDepartmentEntity;

public interface RocketChatOrganizationDepartmentRepository extends JpaRepository<RocketChatOrganizationDepartmentEntity, Long> {
	Optional<RocketChatOrganizationDepartmentEntity> findByOrganizationId(Long orgId);
}
