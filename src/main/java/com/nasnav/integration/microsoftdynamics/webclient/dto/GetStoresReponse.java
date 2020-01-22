package com.nasnav.integration.microsoftdynamics.webclient.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class GetStoresReponse {
	//their api response have such structure, Don't ask me why !!
	@JsonProperty("results")
	private List<Stores> results;
}
