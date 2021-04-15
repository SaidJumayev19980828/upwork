package com.nasnav.integration.microsoftdynamics.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GetStoresReponse {
	//their api response have such structure, Don't ask me why !!
	@JsonProperty("results")
	private List<Stores> results;
}
