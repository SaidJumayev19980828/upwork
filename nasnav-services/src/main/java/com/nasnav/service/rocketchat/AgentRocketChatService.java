package com.nasnav.service.rocketchat;

import com.nasnav.dto.rocketchat.RocketChatAgentTokenDTO;

import reactor.core.publisher.Mono;

public interface AgentRocketChatService {
	Mono<RocketChatAgentTokenDTO> createAgentTokenForCurrentEmployeeCreateAgentIfNeeded();
}
