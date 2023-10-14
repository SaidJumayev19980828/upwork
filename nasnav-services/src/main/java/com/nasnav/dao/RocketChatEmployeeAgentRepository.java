package com.nasnav.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.RocketChatEmployeeAgentEntity;


public interface RocketChatEmployeeAgentRepository extends JpaRepository<RocketChatEmployeeAgentEntity, Long> {
	Optional<RocketChatEmployeeAgentEntity> findByEmployeeId(Long employeeId);
}
