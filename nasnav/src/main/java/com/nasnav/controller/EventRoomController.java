package com.nasnav.controller;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.dto.request.RoomSessionDTO;
import com.nasnav.dto.request.RoomTemplateDTO;
import com.nasnav.dto.response.EventRoomResponse;
import com.nasnav.enumerations.EventRoomStatus;
import com.nasnav.service.EventRoomService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/room/event")
@RequiredArgsConstructor
public class EventRoomController {
	private final EventRoomService metaverseRoomService;

	@GetMapping
	public EventRoomResponse getRoom(@RequestHeader(name = "User-Token", required = false) String userToken,
			@RequestParam(name = "event_id") Long eventId) {
		return metaverseRoomService.getRoombyEventId(eventId);
	}

	@PostMapping("/template")
	public EventRoomResponse createUpdateRoomTemplate(
			@RequestHeader(name = "User-Token", required = false) String userToken,
			@RequestParam(name = "event_id") Long eventId,
			@RequestBody @Valid RoomTemplateDTO roomTemplate) {
		return metaverseRoomService.createOrUpdateTemplate(eventId, roomTemplate);
	}

	@PostMapping("/session")
	public EventRoomResponse startRoomSession(@RequestHeader(name = "User-Token", required = false) String userToken,
			@RequestParam(name = "event_id") Long eventId,
			@RequestBody(required = false) @Valid Optional<RoomSessionDTO> roomSession) {
		return metaverseRoomService.startSession(eventId, roomSession);
	}

	@PostMapping("/session/suspend")
	public void suspendRoomSession(@RequestHeader(name = "User-Token", required = false) String userToken,
			@RequestParam(name = "event_id") Long eventId) {
		metaverseRoomService.suspendSession(eventId);
	}


	@GetMapping(value = "/list")
	public Page<EventRoomResponse> getOrgRooms(@RequestParam(name = "org_id", required = true) Long orgId,
			@RequestParam(required = false) EventRoomStatus status,
			@RequestParam(required = false, defaultValue = "0") Integer start,
			@RequestParam(required = false, defaultValue = "10") Integer count) {
		return metaverseRoomService.getOrgRooms(orgId, status, start, count);
	}

	@GetMapping(value = "/list_for_user")
	public Page<EventRoomResponse> getAllAccessibleRooms(
			@RequestHeader(name = "User-Token", required = false) String userToken,
			@RequestParam(required = false) EventRoomStatus status,
			@RequestParam(required = false, defaultValue = "0") Integer start,
			@RequestParam(required = false, defaultValue = "10") Integer count) {
		return metaverseRoomService.getRooms(status, start, count);
	}

	@DeleteMapping
	public void deleteRoomTemplate(@RequestHeader(name = "User-Token", required = false) String userToken,
			@RequestParam(name = "event_id") Long eventId) {
		metaverseRoomService.deleteTemplate(eventId);
	}
}
