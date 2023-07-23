package com.nasnav.yeshtery.controller.v1;

import java.util.Set;

import javax.validation.Valid;

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
import com.nasnav.dto.response.RoomResponse;
import com.nasnav.service.MetaverseRoomService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(MetaverseRoomController.API_PATH)
@RequiredArgsConstructor
public class MetaverseRoomController {
    static final String API_PATH = YeshteryConstants.API_PATH + "/room";
	private final MetaverseRoomService metaverseRoomService;

	@GetMapping
	public RoomResponse getShopRoom(@RequestHeader(name = "User-Token", required = false) String userToken,
			@RequestParam(name = "shop_id") Long shopId) {
		return metaverseRoomService.getRoombyShopId(shopId);
	}

	@PostMapping("/template")
	public RoomResponse createUpdateRoomTemplate(@RequestHeader(name = "User-Token", required = false) String userToken,
			@RequestParam(name = "shop_id") Long shopId,
			@RequestBody @Valid RoomTemplateDTO roomTemplate) {
		return metaverseRoomService.createOrUpdateTemplate(shopId, roomTemplate);
	}

	@PostMapping("/session")
	public RoomResponse createRoomSession(@RequestHeader(name = "User-Token", required = false) String userToken,
			@RequestParam(name = "shop_id") Long shopId,
			@RequestBody(required = false) @Valid RoomSessionDTO roomSession) {
		return metaverseRoomService.createNewSession(shopId, roomSession);
	}

	@GetMapping(value = "/list")
	public Set<RoomResponse> getOrgRooms(@RequestParam(name = "org_id", required = false) Long orgId) {
		return metaverseRoomService.getOrgRooms(orgId);
	}

	@GetMapping(value = "/list_for_user")
	public Set<RoomResponse> getAllAccessibleRooms(@RequestHeader(name = "User-Token", required = false) String userToken) {
		return metaverseRoomService.getRooms();
	}

	@DeleteMapping
	public void deleteRoomTemplate(@RequestHeader(name = "User-Token", required = false) String userToken,
			@RequestParam(name = "shop_id") Long shopId) {
		metaverseRoomService.deleteTemplate(shopId);
	}
}
