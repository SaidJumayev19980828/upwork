package com.nasnav.service.rocketchat.impl;

import static com.nasnav.exceptions.ErrorCodes.CHAT$NOT_CONFIGURED;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.nasnav.AppConfig;
import com.nasnav.dto.rocketchat.RocketChatAgentDepartmentsDTO;
import com.nasnav.dto.rocketchat.RocketChatAgentTokenDTO;
import com.nasnav.dto.rocketchat.RocketChatConfigDTO;
import com.nasnav.dto.rocketchat.RocketChatDTOWrapper;
import com.nasnav.dto.rocketchat.RocketChatDepartmentDTO;
import com.nasnav.dto.rocketchat.RocketChatVisitorDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.dto.rocketchat.RocketChatResponseWrapper;
import com.nasnav.dto.rocketchat.RocketChatUpdateDepartmentAgentDTO;
import com.nasnav.dto.rocketchat.RocketChatUserDTO;

import reactor.core.publisher.Mono;

@Component
public class RocketChatClient {
	private final WebClient webClient;

	public RocketChatClient(AppConfig config) {
		webClient = StringUtils.isNotBlank(config.rocketChatUrl)? WebClient.builder()
				.baseUrl(config.rocketChatUrl)
				.defaultHeader("X-Auth-Token", config.getRocketChatAccessToken())
				.defaultHeader("X-User-Id", config.getRocketChatUserId())
				.build() : null;
	}

	public Mono<RocketChatVisitorDTO> liveChatInit(String token, String department) {
		validateWebClientConfigured();
		ParameterizedTypeReference<RocketChatResponseWrapper<RocketChatConfigDTO>> typeRef = new ParameterizedTypeReference<>() {
		};
		return webClient.get()
				.uri(
					uriBuilder -> uriBuilder
							.path("/livechat/config")
							.queryParam("token", token)
							.queryParam("department", department)
							.build()
				).accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(typeRef)
				.map(RocketChatResponseWrapper::getData)
				.mapNotNull(RocketChatConfigDTO::getGuest);
	}

	public Mono<RocketChatVisitorDTO> liveChatRegisterVisitor(RocketChatVisitorDTO visitor) {
		validateWebClientConfigured();
		ParameterizedTypeReference<RocketChatResponseWrapper<RocketChatVisitorDTO>> typeRef = new ParameterizedTypeReference<>() {
		};
		RocketChatDTOWrapper<RocketChatVisitorDTO> wrappedRequest = new RocketChatDTOWrapper<>();
		wrappedRequest.setData(visitor);
		return webClient.post()
				.uri("/livechat/visitor")
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(wrappedRequest)
				.retrieve()
				.bodyToMono(typeRef)
				.map(RocketChatResponseWrapper::getData);
	}

	public Mono<RocketChatDepartmentDTO> createDepartment(RocketChatDepartmentDTO department) {
		validateWebClientConfigured();
		ParameterizedTypeReference<RocketChatResponseWrapper<RocketChatDepartmentDTO>> typeRef = new ParameterizedTypeReference<>() {
		};
		RocketChatDTOWrapper<RocketChatDepartmentDTO> wrappedRequest = new RocketChatDTOWrapper<>();
		wrappedRequest.setData(department);
		return webClient.post()
				.uri("/livechat/department")
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(wrappedRequest)
				.retrieve()
				.bodyToMono(typeRef)
				.map(RocketChatResponseWrapper::getData);
	}

	public Mono<RocketChatDepartmentDTO> getDepartment(String departmentId) {
		validateWebClientConfigured();
		ParameterizedTypeReference<RocketChatResponseWrapper<RocketChatDepartmentDTO>> typeRef = new ParameterizedTypeReference<>() {
		};
		return webClient.get()
				.uri("/livechat/department/{departmentId}", departmentId)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(typeRef)
				.map(RocketChatResponseWrapper::getData);
	}

	public Mono<Void> deleteDepartment(String departmentId) {
		validateWebClientConfigured();
		return webClient.delete()
				.uri("/livechat/department/{departmentId}", departmentId)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(Void.class);
	}

	public Mono<RocketChatUserDTO> createUser(RocketChatUserDTO user) {
		validateWebClientConfigured();
		ParameterizedTypeReference<RocketChatResponseWrapper<RocketChatUserDTO>> typeRef = new ParameterizedTypeReference<>() {
		};

		return webClient.post()
				.uri("/users.create")
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(user)
				.retrieve()
				.bodyToMono(typeRef)
				.map(RocketChatResponseWrapper::getData);
	}

	Mono<RocketChatUserDTO> getAgent(String agentId) {
		validateWebClientConfigured();
		ParameterizedTypeReference<RocketChatResponseWrapper<RocketChatUserDTO>> typeRef = new ParameterizedTypeReference<>() {
		};

		return webClient.get()
				.uri("/livechat/users/agent/{agentId}", agentId)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(typeRef)
				.map(RocketChatResponseWrapper::getData);
	}

	public Mono<RocketChatUserDTO> registerAgent(RocketChatUserDTO user) {
		validateWebClientConfigured();
		ParameterizedTypeReference<RocketChatResponseWrapper<RocketChatUserDTO>> typeRef = new ParameterizedTypeReference<>() {
		};

		return webClient.post()
				.uri("/livechat/users/agent")
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(user)
				.retrieve()
				.bodyToMono(typeRef)
				.map(RocketChatResponseWrapper::getData);
	}

	public Mono<Void> updateDepartmentAgents(String departmentId, RocketChatUpdateDepartmentAgentDTO departmentAgents) {
		return webClient.post()
				.uri("/livechat/department/{departmentId}/agents", departmentId)
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(departmentAgents)
				.retrieve()
				.bodyToMono(void.class);
	}

	public Mono<RocketChatAgentTokenDTO> createAgentToken(RocketChatAgentTokenDTO agent) {
		validateWebClientConfigured();
		ParameterizedTypeReference<RocketChatResponseWrapper<RocketChatAgentTokenDTO>> typeRef = new ParameterizedTypeReference<>() {
		};

		return webClient.post()
				.uri("/users.createToken")
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(agent)
				.retrieve()
				.bodyToMono(typeRef)
				.map(RocketChatResponseWrapper::getData);
	}

	public Mono<RocketChatAgentDepartmentsDTO> getAgentDepartments(String agentId) {
		validateWebClientConfigured();
		ParameterizedTypeReference<RocketChatResponseWrapper<RocketChatAgentDepartmentsDTO>> typeRef = new ParameterizedTypeReference<>() {
		};

		return webClient.get()
				.uri("/livechat/agents/{agentId}/departments", agentId)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(typeRef)
				.map(RocketChatResponseWrapper::getData);
	}

	public Mono<Void> deleteUser(RocketChatUserDTO user) {
		validateWebClientConfigured();
		return webClient.post()
				.uri("/users.delete")
				.bodyValue(user)
				.retrieve()
				.bodyToMono(Void.class);
	}

	private void validateWebClientConfigured() {
		if (webClient == null) {
			throw new RuntimeBusinessException(HttpStatus.OK, CHAT$NOT_CONFIGURED);
		}
	}
}
