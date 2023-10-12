package com.nasnav.service.rocketchat.impl;

import org.springframework.stereotype.Service;

import com.nasnav.dao.RocketChatEmployeeAgentRepository;
import com.nasnav.dto.rocketchat.RocketChatAgentTokenDTO;
import com.nasnav.dto.rocketchat.RocketChatUserDTO;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.RocketChatEmployeeAgentEntity;
import com.nasnav.service.SecurityService;
import com.nasnav.service.rocketchat.AgentRocketChatService;
import com.nasnav.service.rocketchat.DepartmentRocketChatService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AgentRocketChatServiceImpl implements AgentRocketChatService {
	private final SecurityService securityService;
	private final DepartmentRocketChatService departmentRocketChatService;
	private final RocketChatClient rocketChatClient;
	private final RocketChatEmployeeAgentRepository agentRepository;

	@Override
	public Mono<RocketChatAgentTokenDTO> createAgentTokenForCurrentEmployeeCreateAgentIfNeeded() {
		EmployeeUserEntity employee = (EmployeeUserEntity) securityService.getCurrentUser();

		return agentRepository.findByEmployeeId(employee.getId())
				.switchIfEmpty(Mono.defer(() -> createRocketChatUser(employee)))
				.doOnNext(this::updateAgentDepartmentIfNeeded)
				.map(RocketChatEmployeeAgentEntity::getAgentId)
				.map(agentId -> RocketChatAgentTokenDTO.builder().userId(agentId).build());
	}

	private Mono<RocketChatEmployeeAgentEntity> createRocketChatUser(EmployeeUserEntity employee) {
		return rocketChatClient.createUser(
				RocketChatUserDTO.builder()
						.name(employee.getName())
						.username(employee.getEmail())
						.email(employee.getEmail())
						.active(true)
						.build())
				.map(agent -> new RocketChatEmployeeAgentEntity(agent.getId(), employee))
				.map(agentRepository::save);
	}

	private Mono<Void> updateAgentDepartmentIfNeeded(RocketChatEmployeeAgentEntity agent) {
		return departmentRocketChatService
				.getDepartmentIdCreateDepartmentIfNeeded(agent.getEmployee().getOrganizationId())
				.doOnNext(departmentId -> getAndValidateAgentDepartment(agent.getAgentId(), departmentId))
				.then();
	}

	private Mono<Void> getAndValidateAgentDepartment(String agentId, String departmentId) {
		return rocketChatClient.getAgentDepartments(agentId)
				.filter(dto -> dto.getDepartmentId().equals(departmentId))
				.then();
	}
}
