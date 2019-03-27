package com.nasnav.dto;

import java.util.ArrayList;

import lombok.Data;


@Data
public class OrderJsonDto{

	private Long id;
	private String status;
	private Object[] basket;
	private String address;
}
