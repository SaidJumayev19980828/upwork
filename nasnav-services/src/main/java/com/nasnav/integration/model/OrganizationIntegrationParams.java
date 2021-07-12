package com.nasnav.integration.model;

import com.nasnav.persistence.IntegrationParamEntity;
import lombok.Data;

import java.util.List;

@Data
public class OrganizationIntegrationParams {
	private String integrationModuleClass;
	
	private List<IntegrationParamEntity> params;
}
