package com.nasnav.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.enumerations.EventRoomStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonNaming(SnakeCaseStrategy.class)
@EqualsAndHashCode(callSuper = false)
public class EventRoomResponse extends RoomResponse {
	private EventRoomStatus status;
	private EventResponseDto event;
}
