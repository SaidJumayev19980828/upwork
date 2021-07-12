package com.nasnav.shipping.services.bosta.webclient.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class PackageDetails {
	private Integer itemsCount;
	private String document;
	private String description;
}
