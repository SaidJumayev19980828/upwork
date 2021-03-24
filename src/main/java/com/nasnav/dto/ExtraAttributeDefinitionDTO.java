package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import com.nasnav.enumerations.ExtraAttributeType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

import static com.nasnav.enumerations.ExtraAttributeType.INVISIBLE;
import static java.util.Optional.ofNullable;

@Data
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ExtraAttributeDefinitionDTO {
	private Integer id;
	private String name;
	private ExtraAttributeType type;
	private String iconUrl;
	private Boolean invisible;
}
