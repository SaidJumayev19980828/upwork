package com.nasnav.service;


import org.springframework.data.domain.Page;

import com.nasnav.dto.request.RoomSessionDTO;
import com.nasnav.dto.request.RoomTemplateDTO;
import com.nasnav.dto.response.EventRoomResponse;

public interface EventRoomService {
	EventRoomResponse createOrUpdateTemplate(Long eventId, RoomTemplateDTO dto);

	EventRoomResponse createNewSession(Long eventId, RoomSessionDTO sessionExternalId);

	void deleteTemplate(Long eventId);

	EventRoomResponse getRoombyEventId(Long eventId);

	Page<EventRoomResponse> getOrgRooms(Long orgId, Boolean started, Integer start, Integer count);

	Page<EventRoomResponse> getRooms(Boolean started, Integer start, Integer count);
}
