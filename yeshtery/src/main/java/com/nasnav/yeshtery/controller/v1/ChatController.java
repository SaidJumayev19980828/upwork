package com.nasnav.yeshtery.controller.v1;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dto.rocketchat.RocketChatVisitorDTO;
import com.nasnav.service.rocketchat.CustomerRocketChatService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;


@RestController
@RequestMapping(ChatController.API_PATH)
@RequiredArgsConstructor
public class ChatController {
    static final String API_PATH = YeshteryConstants.API_PATH + "/chat";
	private final CustomerRocketChatService customerRocketChatService;

	@GetMapping(value="visitor_data")
	public Mono<RocketChatVisitorDTO> getInitialVisitorData(@RequestHeader(TOKEN_HEADER) String userToken) {
		return customerRocketChatService.getRocketChatVisitorData();
	}
}
