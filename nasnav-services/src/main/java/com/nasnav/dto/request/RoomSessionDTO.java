package com.nasnav.dto.request;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(SnakeCaseStrategy.class)
public class RoomSessionDTO {
	@NotBlank(message = "\"session_external_id\" must not be blank or null")
	private String sessionExternalId;
}
