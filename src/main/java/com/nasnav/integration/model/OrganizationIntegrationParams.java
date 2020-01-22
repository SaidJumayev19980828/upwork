package com.nasnav.integration.model;

import java.util.List;

import com.nasnav.persistence.IntegrationParamEntity;

import lombok.Data;

@Data
public class OrganizationIntegrationParams {
	private String integrationModuleClass;
	
	private List<IntegrationParamEntity> params;
}
