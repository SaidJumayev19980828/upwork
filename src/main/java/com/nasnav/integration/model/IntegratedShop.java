package com.nasnav.integration.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class IntegratedShop {
	private String id;
	private String name;
	private Map<String,String> additionalData;
	
	
	
	
	public IntegratedShop() {
		additionalData = new HashMap<>();
	}
}
