package com.nasnav.yeshtery.controller.v1;

import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;

import com.nasnav.dto.ShopRateDTO;
import com.nasnav.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.nasnav.dto.response.ShopRoomResponse;
import com.nasnav.service.ShopRoomService;

import lombok.RequiredArgsConstructor;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(ShopRoomController.API_PATH)
@RequiredArgsConstructor
public class ShopRoomController {

	private final ShopService shopService;
    static final String API_PATH = YeshteryConstants.API_PATH + "/room/shop";
	private final ShopRoomService metaverseRoomService;

	@GetMapping
	public ShopRoomResponse getShopRoom(@RequestHeader(name = "User-Token", required = false) String userToken,
			@RequestParam(name = "shop_id") Long shopId) {
		return metaverseRoomService.getRoombyShopId(shopId);
	}

	@PostMapping("/template")
	public ShopRoomResponse createUpdateRoomTemplate(@RequestHeader(name = "User-Token", required = false) String userToken,
			@RequestParam(name = "shop_id") Long shopId,
			@RequestBody @Valid RoomTemplateDTO roomTemplate) {
		return metaverseRoomService.createOrUpdateTemplate(shopId, roomTemplate);
	}

	@PostMapping("/session")
	public ShopRoomResponse createRoomSession(@RequestHeader(name = "User-Token", required = false) String userToken,
			@RequestParam(name = "shop_id") Long shopId,
			@RequestBody(required = false) @Valid Optional<RoomSessionDTO> roomSession) {
		return metaverseRoomService.startSession(shopId, roomSession);
	}

	@GetMapping(value = "/list")
	public Set<ShopRoomResponse> getOrgRooms(@RequestParam(name = "org_id", required = false) Long orgId) {
		return metaverseRoomService.getOrgRooms(orgId);
	}

	@GetMapping(value = "/list_for_user")
	public Set<ShopRoomResponse> getAllAccessibleRooms(@RequestHeader(name = "User-Token", required = false) String userToken) {
		return metaverseRoomService.getRooms();
	}

	@DeleteMapping
	public void deleteRoomTemplate(@RequestHeader(name = "User-Token", required = false) String userToken,
			@RequestParam(name = "shop_id") Long shopId) {
		metaverseRoomService.deleteTemplate(shopId);
	}

	@PostMapping(value = "/rateShop", consumes = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> rateShop(@RequestHeader(name = "User-Token", required = false) String userToken,
								   @RequestBody ShopRateDTO dto) {
		shopService.rateShop(dto);
		return new ResponseEntity<>(dto, HttpStatus.CREATED);
	}
}
