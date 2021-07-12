package com.nasnav.integration.model;

import com.nasnav.integration.IntegrationModule;
import com.nasnav.persistence.IntegrationParamEntity;
import lombok.Data;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class OrganizationIntegrationInfo {
	@Nonnull
	private IntegrationModule integrationModule;
	private Long requestMinDelayMillis;
	private boolean disabled;
	private List<IntegrationParamEntity> parameters;
	
	
	public OrganizationIntegrationInfo(IntegrationModule integrationModule, Long requestMinDelayMillis, List<IntegrationParamEntity> parameters) {
		this.integrationModule = integrationModule;
		this.requestMinDelayMillis = requestMinDelayMillis;
		this.parameters = Optional.ofNullable(parameters)
									.orElse(new ArrayList<>());
		disabled = false;
	}
}
