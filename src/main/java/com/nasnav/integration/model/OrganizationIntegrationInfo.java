package com.nasnav.integration.model;

import java.util.List;

import com.nasnav.integration.IntegrationModule;
import com.nasnav.persistence.IntegrationParamEntity;

import lombok.Data;

@Data
public class OrganizationIntegrationInfo {
	private IntegrationModule integrationModule;
	private List<IntegrationParamEntity> parameters;
}
