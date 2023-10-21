package com.nasnav.service.rocketchat.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.RocketChatEmployeeAgentRepository;
import com.nasnav.dto.rocketchat.RocketChatAgentDepartmentsDTO;
import com.nasnav.dto.rocketchat.RocketChatAgentTokenDTO;
import com.nasnav.dto.rocketchat.RocketChatDepartmentAgentDTO;
import com.nasnav.dto.rocketchat.RocketChatUpdateDepartmentAgentDTO;
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
	private final EmployeeUserRepository employeeUserRepository;

	@Transactional
	@Override
	public Mono<RocketChatAgentTokenDTO> createAgentTokenForCurrentEmployeeCreateAgentIfNeeded() {
		return getOrCreateAgentForEmployee((EmployeeUserEntity)securityService.getCurrentUser())
				.flatMap(this::createAgentToken);
	}

	private Mono<RocketChatEmployeeAgentEntity> getOrCreateAgentForEmployee(EmployeeUserEntity employee) {
		return agentRepository.findByEmployeeId(employee.getId())
				.map(agent -> Mono.just(agent).doOnNext(this::makeSureAgentBelongsToNeededDepartment))
				.orElseGet(() -> createAgent(employee));
	}

	private Mono<RocketChatAgentTokenDTO> createAgentToken(RocketChatEmployeeAgentEntity agent) {
		return rocketChatClient.createAgentToken(new RocketChatAgentTokenDTO(agent.getAgentId()));
		// for testing
		// return Mono.just(RocketChatAgentTokenDTO.builder().userId(agent.getAgentId())
		// 		.authToken(UUID.randomUUID().toString()).build());
	}

	private Mono<RocketChatEmployeeAgentEntity> createAgent(EmployeeUserEntity employee) {
		RocketChatEmployeeAgentEntity agent = createAgentEntity(employee);
		Long employeeId = agent.getEmployeeId();
		return createRocketChatUser(agent)
				.map(userDTO -> {
					agent.setAgentId(userDTO.getId());
					agent.setUsername(userDTO.getUsername());
					return agentRepository.save(agent);
				})
				.flatMap(this::registerUserAsAgent)
				.then(addAgentToNeededDepartment(agent))
				.doOnError(ex -> agentRepository.deleteById(employeeId))
				.then(Mono.just(agent));
	}

	private RocketChatEmployeeAgentEntity createAgentEntity(EmployeeUserEntity employee) {
		RocketChatEmployeeAgentEntity agent = new RocketChatEmployeeAgentEntity();
		agent.setEmployee(employeeUserRepository.getOne(employee.getId())); // not sure why I need to refresh the employee!
		return agentRepository.save(agent);
	}

	private Mono<RocketChatUserDTO> createRocketChatUser(RocketChatEmployeeAgentEntity agent) {
		String username = UUID.randomUUID().toString();
		// email needs to be unique accross servers
		String email = "not_email" + username.replace("-", "") + "@nasnav.com";
		RocketChatUserDTO rocketChatUser = RocketChatUserDTO.builder()
				.name(agent.getEmployee().getName())
				.email(email)
				.username(username)
				.password(UUID.randomUUID().toString())
				.active(true)
				.build();
		return rocketChatClient.createUser(rocketChatUser);
	}

	Mono<Void> registerUserAsAgentIfNeeded(RocketChatEmployeeAgentEntity agent) {
		return rocketChatClient.getAgent(agent.getAgentId())
			.onErrorResume(WebClientResponseException.class, ex -> createRocketChatUser(agent).then(registerUserAsAgent(agent)))
			.switchIfEmpty(Mono.defer(() -> registerUserAsAgent(agent)))
			.then();
	}

	private Mono<RocketChatUserDTO> registerUserAsAgent(RocketChatEmployeeAgentEntity agent) {
		RocketChatUserDTO agentForRegister = RocketChatUserDTO.builder()
				.username(agent.getUsername())
				.build();
		return rocketChatClient.registerAgent(agentForRegister);
	}

	private Mono<Void> makeSureAgentBelongsToNeededDepartment(RocketChatEmployeeAgentEntity agent) {
		Mono<String> departmentIdMono = getNeededDepartmentId(agent);
		return Mono.zip(rocketChatClient.getAgentDepartments(agent.getAgentId()), departmentIdMono)
				.flatMap(tuple -> addAgentToDepartmentsIfNeeded(agent, tuple.getT1(), tuple.getT2()));
	}

	private Mono<Void> addAgentToDepartmentsIfNeeded(RocketChatEmployeeAgentEntity agent,
			RocketChatAgentDepartmentsDTO agentDepartments,
			String departmentId) {
		if (agentDepartments.stream().map(RocketChatDepartmentAgentDTO::getDepartmentId).anyMatch(departmentId::equals)) {
			return Mono.empty();
		} else {
			return registerUserAsAgentIfNeeded(agent).then(addAgentToDepartment(departmentId, agent));
		}
	}

	private Mono<Void> addAgentToNeededDepartment(RocketChatEmployeeAgentEntity agent) {
		return getNeededDepartmentId(agent).flatMap(departmentId -> addAgentToDepartment(departmentId, agent));
	}

	private Mono<Void> addAgentToDepartment(String departmentId, RocketChatEmployeeAgentEntity agent) {
		RocketChatUpdateDepartmentAgentDTO departmentAgents = RocketChatUpdateDepartmentAgentDTO.builder()
				.upsert(RocketChatDepartmentAgentDTO.builder()
						.departmentId(departmentId)
						.agentId(agent.getAgentId())
						.username(agent.getUsername())
						.build())
				.build();
		return rocketChatClient.updateDepartmentAgents(departmentId, departmentAgents);
	}

	private Mono<String> getNeededDepartmentId(RocketChatEmployeeAgentEntity agent) {
		return departmentRocketChatService.getDepartmentIdCreateDepartmentIfNeeded(agent.getEmployee().getOrganizationId());
	}
}
