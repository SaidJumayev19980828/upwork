package com.nasnav.yeshtery.controller.v1;

import java.util.Optional;

import javax.validation.Valid;

import com.nasnav.dto.EventRoomProjection;
import com.nasnav.dto.EventsRoomNewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dto.request.RoomSessionDTO;
import com.nasnav.dto.request.RoomTemplateDTO;
import com.nasnav.dto.response.EventRoomResponse;
import com.nasnav.enumerations.EventRoomStatus;
import com.nasnav.service.EventRoomService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(EventRoomController.API_PATH)
@RequiredArgsConstructor
public class EventRoomController {
	static final String API_PATH = YeshteryConstants.API_PATH + "/room/event";
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
	public EventRoomResponse createRoomSession(@RequestHeader(name = "User-Token", required = false) String userToken,
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

	@GetMapping(value = "/all")
	public PageImpl<EventsRoomNewDTO> getAllRooms(@RequestParam(name = "orgId", required = false) Long orgId,
												   @RequestParam(required = false, defaultValue = "0") Integer start,
												   @RequestParam(required = false, defaultValue = "10") Integer count) {
		return metaverseRoomService.getUserRooms(orgId, start, count);
	}

}
