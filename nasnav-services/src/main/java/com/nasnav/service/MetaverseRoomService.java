package com.nasnav.service;

import java.util.Set;

import com.nasnav.dto.request.RoomSessionDTO;
import com.nasnav.dto.request.RoomTemplateDTO;
import com.nasnav.dto.response.RoomResponse;

public interface MetaverseRoomService {
	RoomResponse createOrUpdateTemplate(Long shopId, RoomTemplateDTO dto);

	RoomResponse createNewSession(Long shopId, RoomSessionDTO sessionExternalId);

	void deleteTemplate(Long shopId);

	RoomResponse getRoombyShopId(Long shopId);

	Set<RoomResponse> getOrgRooms(Long orgId);

	Set<RoomResponse> getRooms();
}
