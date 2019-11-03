package com.nasnav.integration.events.data;

import lombok.Data;

@Data
public class CustomerData {
	private Long id;
	private String email;
	private String phone;
	private String address;
}
