package com.nasnav.dto.request.organization;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.UserDTOs;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateInfluencerAccountForAiRequest {
	@Schema(example = "John Smith", required = true)
	@NotNull
	private String name;
	@Valid
	private UserDTOs.AiInfluencerUserDataObject data;
}
