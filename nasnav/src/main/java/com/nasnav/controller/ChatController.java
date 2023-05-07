package com.nasnav.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.dto.rocketchat.RocketChatVisitorDTO;
import com.nasnav.service.rocketchat.CustomerRocketChatService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
	CustomerRocketChatService customerRocketChatService;

	@GetMapping(value="visitor_data")
	public Mono<RocketChatVisitorDTO> getInitialVisitorData() {
		return customerRocketChatService.getRocketChatVisitorData();
	}
}
