package com.nasnav.integration.microsoftdynamics.webclient.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Stores {
	@JsonProperty("shops")
	private List<Store> stores;
}
