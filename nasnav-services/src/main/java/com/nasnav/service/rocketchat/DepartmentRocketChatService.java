package com.nasnav.service.rocketchat;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public interface DepartmentRocketChatService {

	Mono<String> getDepartmentIdCreateDepartmentIfNeeded(Long orgId);
}
