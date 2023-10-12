package com.nasnav.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.dto.rocketchat.RocketChatAgentTokenDTO;
import com.nasnav.dto.rocketchat.RocketChatVisitorDTO;
import com.nasnav.service.rocketchat.AgentRocketChatService;
import com.nasnav.service.rocketchat.CustomerRocketChatService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;


@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
	private final CustomerRocketChatService customerRocketChatService;
	private final AgentRocketChatService agentRocketChatService;

	@GetMapping("visitor_data")
	public Mono<RocketChatVisitorDTO> getInitialVisitorData(@RequestHeader(TOKEN_HEADER) String userToken) {
		return customerRocketChatService.getRocketChatVisitorData();
	}

	@PostMapping("agent/authenticate")
	public Mono<RocketChatAgentTokenDTO> createAgentAuthToken(@RequestHeader(TOKEN_HEADER) String userToken) {
		return agentRocketChatService.createAgentTokenForCurrentEmployeeCreateAgentIfNeeded();
	}
}
