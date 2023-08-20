package com.nasnav.service;

import java.util.Set;

import com.nasnav.dto.request.RoomSessionDTO;
import com.nasnav.dto.request.RoomTemplateDTO;
import com.nasnav.dto.response.ShopRoomResponse;

public interface ShopRoomService {
	ShopRoomResponse createOrUpdateTemplate(Long shopId, RoomTemplateDTO dto);

	ShopRoomResponse createNewSession(Long shopId, RoomSessionDTO sessionExternalId);

	void deleteTemplate(Long shopId);

	ShopRoomResponse getRoombyShopId(Long shopId);

	Set<ShopRoomResponse> getOrgRooms(Long orgId);

	Set<ShopRoomResponse> getRooms();
}
