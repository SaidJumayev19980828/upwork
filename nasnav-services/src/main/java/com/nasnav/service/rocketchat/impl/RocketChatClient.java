package com.nasnav.service.rocketchat.impl;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.nasnav.AppConfig;
import com.nasnav.dto.rocketchat.RocketChatConfigDTO;
import com.nasnav.dto.rocketchat.RocketChatDTOWrapper;
import com.nasnav.dto.rocketchat.RocketChatDepartmentDTO;
import com.nasnav.dto.rocketchat.RocketChatVisitorDTO;
import com.nasnav.dto.rocketchat.RocketChatResponseWrapper;

import reactor.core.publisher.Mono;

@Component
public class RocketChatClient {
	private final WebClient webClient;

	public RocketChatClient(AppConfig config) {
		webClient = WebClient.builder()
				.baseUrl(config.rocketChatUrl)
				.defaultHeader("X-Auth-Token", config.getRocketChatAccessToken())
				.defaultHeader("X-User-Id", config.getRocketChatUserId())
				.build();
	}

	public Mono<RocketChatVisitorDTO> liveChatInit(String token, String department) {
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

	public Mono<RocketChatDepartmentDTO> updateDepartment(RocketChatDepartmentDTO department) {
		ParameterizedTypeReference<RocketChatResponseWrapper<RocketChatDepartmentDTO>> typeRef = new ParameterizedTypeReference<>() {
		};
		RocketChatDTOWrapper<RocketChatDepartmentDTO> wrappedRequest = new RocketChatDTOWrapper<>();
		wrappedRequest.setData(department);
		return webClient.put()
				.uri("/livechat/department/{departmentId}", department.getId())
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(wrappedRequest)
				.retrieve()
				.bodyToMono(typeRef)
				.map(RocketChatResponseWrapper::getData);
	}

	public Mono<RocketChatDepartmentDTO> getDepartment(String departmentId) {
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
		return webClient.get()
				.uri("/livechat/department/{departmentId}", departmentId)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(Void.class);
	}
}
