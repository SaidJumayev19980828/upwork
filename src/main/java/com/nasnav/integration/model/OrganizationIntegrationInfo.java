package com.nasnav.integration.model;

import java.util.List;

import javax.annotation.Nonnull;

import com.nasnav.integration.IntegrationModule;
import com.nasnav.persistence.IntegrationParamEntity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrganizationIntegrationInfo {
	@Nonnull
	private IntegrationModule integrationModule;
	private List<IntegrationParamEntity> parameters;
}
