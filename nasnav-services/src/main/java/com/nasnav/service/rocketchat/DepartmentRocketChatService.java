package com.nasnav.service.rocketchat;

import org.springframework.stereotype.Service;

import com.nasnav.persistence.OrganizationEntity;

import reactor.core.publisher.Mono;

@Service
public interface DepartmentRocketChatService {
	Mono<String> getDepartmentIdCreateDepartmentIfNeeded(OrganizationEntity org);
}
