package com.nasnav.service.rocketchat.impl;

import java.util.concurrent.CompletableFuture;

import org.elasticsearch.cluster.metadata.AliasAction.NewAliasValidator;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.nasnav.AppConfig;
import com.nasnav.dto.rocketchat.RocketChatConfigDTO;
import com.nasnav.dto.rocketchat.RocketChatDTOWrapper;
import com.nasnav.dto.rocketchat.RocketChatVisitorDTO;
import com.nasnav.dto.rocketchat.RocketChatResponseWrapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
public class RocketChatClient {
	private final WebClient webClient;

	public RocketChatClient(AppConfig config) {
		webClient = WebClient.builder().baseUrl(config.rocketChatUrl).build();
	}

	public Mono<RocketChatVisitorDTO> liveChatInit(String token, String department) {
		ParameterizedTypeReference<RocketChatResponseWrapper<RocketChatConfigDTO>> typeRef = new ParameterizedTypeReference<>() {
		};
		return webClient.get()
				.uri(
					uriBuilder -> uriBuilder
							.path("livechat/config")
							.queryParam("token", token)
							.queryParam("department", department)
							.build()
				).retrieve()
				.bodyToMono(typeRef)
				.map(RocketChatResponseWrapper::getData)
				.map(RocketChatConfigDTO::getGuest);
	}

	public Mono<RocketChatVisitorDTO> liveChatRegisterVisitor(RocketChatVisitorDTO visitor) {
		ParameterizedTypeReference<RocketChatResponseWrapper<RocketChatVisitorDTO>> typeRef = new ParameterizedTypeReference<>() {
		};
		RocketChatDTOWrapper<RocketChatVisitorDTO> wrappedRequest = new RocketChatDTOWrapper<>();
		wrappedRequest.setData(visitor);
		return webClient.post()
				.uri("livechat/config")
				.body(wrappedRequest, wrappedRequest.getClass())
				.retrieve()
				.bodyToMono(typeRef)
				.map(RocketChatResponseWrapper::getData);
	}
}
