package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;


@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OrganizationIntegrationInfoDTO {
	private Long organizationId;
	private String integrationModule;
	private Integer maxRequestRate;
	private Map<String,String> integrationParameters;
}
