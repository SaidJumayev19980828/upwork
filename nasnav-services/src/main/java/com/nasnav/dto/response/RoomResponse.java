package com.nasnav.dto.response;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.dto.request.RoomTemplateDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonNaming(SnakeCaseStrategy.class)
@EqualsAndHashCode(callSuper = false)
public class RoomResponse extends RoomTemplateDTO {
	private String sessionExternalId;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private LocalDateTime sessionCreatedAt;
}
