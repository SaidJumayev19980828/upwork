package com.nasnav.dto;

import java.util.Map;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OrganizationIntegrationInfoDTO {
	private Long organizationId;
	private String integrationModule;
	private Integer maxRequestRate;
	private Map<String,String> integrationParameters;
}
