package com.nasnav.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.RocketChatEmployeeAgentEntity;

import reactor.core.publisher.Mono;


public interface RocketChatEmployeeAgentRepository extends JpaRepository<RocketChatEmployeeAgentEntity, Long> {
	Mono<RocketChatEmployeeAgentEntity> findByEmployeeId(Long employeeId);
}
