package com.nasnav.service.rocketchat;

import com.nasnav.dto.rocketchat.RocketChatVisitorDTO;

import reactor.core.publisher.Mono;

public interface CustomerRocketChatService {
	public Mono<RocketChatVisitorDTO> getRocketChatVisitorData();
}
