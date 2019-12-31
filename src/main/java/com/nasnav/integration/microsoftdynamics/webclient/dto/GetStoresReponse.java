package com.nasnav.integration.microsoftdynamics.webclient.dto;

import java.util.List;

import lombok.Data;

@Data
public class GetStoresReponse {
	private List<Store> results;
}
