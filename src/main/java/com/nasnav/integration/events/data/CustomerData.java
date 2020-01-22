package com.nasnav.integration.events.data;

import java.time.LocalDate;

import lombok.Data;

@Data
public class CustomerData {
	private Long id;
	private String email;
	private String phone;
	private LocalDate birthDate;
	private String firstName;
	private String lastName;
	private Integer gender;
	private AddressData address;
}
