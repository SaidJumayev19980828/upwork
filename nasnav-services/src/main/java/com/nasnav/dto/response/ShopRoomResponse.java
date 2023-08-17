package com.nasnav.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.ShopRepresentationObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonNaming(SnakeCaseStrategy.class)
@EqualsAndHashCode(callSuper = false)
public class ShopRoomResponse extends RoomResponse {
	private ShopRepresentationObject shop;
}
