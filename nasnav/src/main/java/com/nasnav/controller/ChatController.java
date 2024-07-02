package com.nasnav.controller;

import com.nasnav.dto.rocketchat.RocketChatAgentTokenDTO;
import com.nasnav.dto.rocketchat.RocketChatVisitorDTO;
import com.nasnav.security.HasPermission;
import com.nasnav.security.Permissions;
import com.nasnav.service.rocketchat.AgentRocketChatService;
import com.nasnav.service.rocketchat.CustomerRocketChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
	private final CustomerRocketChatService customerRocketChatService;
	private final AgentRocketChatService agentRocketChatService;

	@Operation(responses = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = RocketChatVisitorDTO.class)))
	})
	@HasPermission(Permissions.CHAT_VISITOR)
	@PostMapping("visitor")
	public Mono<RocketChatVisitorDTO> getInitialVisitorData(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken) {
		return customerRocketChatService.getRocketChatVisitorData();
	}

	@HasPermission(Permissions.CHAT_AGENT)
	@Operation(responses = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = RocketChatAgentTokenDTO.class)))
	})
	@PostMapping("agent/authenticate")
	public Mono<RocketChatAgentTokenDTO> createAgentAuthToken(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken) {
		return agentRocketChatService.createAgentTokenForCurrentEmployeeCreateAgentIfNeeded();
	}
}
