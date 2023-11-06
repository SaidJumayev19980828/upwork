package com.nasnav.service;


import java.util.Optional;

import com.nasnav.dto.EventRoomProjection;
import com.nasnav.dto.EventsRoomNewDTO;
import org.springframework.data.domain.Page;

import com.nasnav.dto.request.RoomSessionDTO;
import com.nasnav.dto.request.RoomTemplateDTO;
import com.nasnav.dto.response.EventRoomResponse;
import com.nasnav.enumerations.EventRoomStatus;
import org.springframework.data.domain.PageImpl;

public interface EventRoomService {
	EventRoomResponse createOrUpdateTemplate(Long eventId, RoomTemplateDTO dto);

	EventRoomResponse startSession(Long eventId, Optional<RoomSessionDTO> sessionExternalId);

	void suspendSession(Long eventId);

	void deleteTemplate(Long eventId);

	EventRoomResponse getRoombyEventId(Long eventId);

	Page<EventRoomResponse> getOrgRooms(Long orgId, EventRoomStatus status, Integer start, Integer count);

	Page<EventRoomResponse> getRooms(EventRoomStatus status, Integer start, Integer count);
	PageImpl<EventsRoomNewDTO> getUserRooms(Long orgId, Integer start, Integer count);

}
