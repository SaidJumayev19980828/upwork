package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateAiAccountResponse
{
	@JsonProperty(value = "id")
	private Long id;
	@JsonProperty(value = "name")
	private String name;
	@JsonProperty(value = "organization_id")
	@JsonInclude(value = JsonInclude.Include.NON_NULL)
	private Long organizationId;

	public CreateAiAccountResponse(Long id, String name, Long organizationId) {
		this.id = id;
		this.name = name;
		this.organizationId = organizationId;

	}
}
