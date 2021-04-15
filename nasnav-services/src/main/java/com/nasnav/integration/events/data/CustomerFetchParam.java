package com.nasnav.integration.events.data;

import lombok.Data;

@Data
public class CustomerFetchParam {
	private Long localId;
	private String externalId;
	private String email;
}
