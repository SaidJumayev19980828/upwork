package com.nasnav.shipping.model;

import lombok.Data;

@Data
public class ShipmentReceiver {
	private String firstName;
	private String lastName;
	private String email;
	private String phone;
	private String country;
}
