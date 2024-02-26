package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateOrganizationPannerResponse
{
	@JsonProperty(value = "organization_id")
	@JsonInclude(value = JsonInclude.Include.NON_NULL)
	private Long organizationId;

	@JsonProperty(value = "image_url")
	@JsonInclude(value = JsonInclude.Include.NON_NULL)
	private String imageUrl;

}
