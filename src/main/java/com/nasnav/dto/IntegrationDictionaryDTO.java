package com.nasnav.dto;

import lombok.Data;

@Data
public class IntegrationDictionaryDTO {
	private String localValue;
	private String remoteValue;
	private String typeName;
	private Long orgId;
}
