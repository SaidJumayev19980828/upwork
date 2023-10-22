package com.nasnav.yeshtery.controller.v1;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dto.rocketchat.RocketChatAgentTokenDTO;
import com.nasnav.dto.rocketchat.RocketChatVisitorDTO;
import com.nasnav.service.rocketchat.AgentRocketChatService;
import com.nasnav.service.rocketchat.CustomerRocketChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;

@RestController
@RequestMapping(ChatController.API_PATH)
@RequiredArgsConstructor
public class ChatController {
	static final String API_PATH = YeshteryConstants.API_PATH + "/chat";
	private final CustomerRocketChatService customerRocketChatService;
	private final AgentRocketChatService agentRocketChatService;

	@Operation(responses = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = RocketChatVisitorDTO.class)))
	})
	@PostMapping(value = "visitor")
	public Mono<RocketChatVisitorDTO> getInitialVisitorData(@RequestHeader(TOKEN_HEADER) String userToken,
			@RequestParam("org_id") Long orgId) {
		return customerRocketChatService.getRocketChatVisitorData(orgId);
	}

	@Operation(responses = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = RocketChatAgentTokenDTO.class)))
	})
	@PostMapping("agent/authenticate")
	public Mono<RocketChatAgentTokenDTO> createAgentAuthToken(@RequestHeader(TOKEN_HEADER) String userToken) {
		return agentRocketChatService.createAgentTokenForCurrentEmployeeCreateAgentIfNeeded();
	}
}
