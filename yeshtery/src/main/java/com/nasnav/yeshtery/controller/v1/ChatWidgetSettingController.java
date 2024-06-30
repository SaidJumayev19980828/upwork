package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.response.ChatWidgetSettingResponse;
import com.nasnav.dto.response.CreateChatWidgetRequest;
import com.nasnav.service.impl.ChatWidgetServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;


@RestController
@RequestMapping("/v1/chat-widget-setting")
@RequiredArgsConstructor
public class ChatWidgetSettingController {
	private final ChatWidgetServiceImpl chatWidgetService;

	@PostMapping(value="/create")
	public ResponseEntity<ChatWidgetSettingResponse> create(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
															@RequestBody CreateChatWidgetRequest request) {
		return ResponseEntity.ok(chatWidgetService.create(request));
	}

	@PostMapping(value="/publish")
	public ResponseEntity<ChatWidgetSettingResponse> publish(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
															@RequestParam("org_id") Long orgId) {
		return ResponseEntity.ok(chatWidgetService.publish(orgId));
	}

	@GetMapping(value="/get-unpublished")
	public ResponseEntity<ChatWidgetSettingResponse> getUnPublished(@RequestParam("org_id") Long orgId) {
		return ResponseEntity.ok(chatWidgetService.getUnPublished(orgId));
	}

	@GetMapping(value="/get-published")
	public ResponseEntity<ChatWidgetSettingResponse> getPublished(@RequestParam("org_id") Long orgId) {
		return ResponseEntity.ok(chatWidgetService.getPublished(orgId));
	}
}
