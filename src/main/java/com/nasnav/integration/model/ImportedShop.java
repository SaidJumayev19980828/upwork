package com.nasnav.integration.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class ImportedShop {
	private String id;
	private String name;
	private Map<String,String> additionalData;
	
	
	
	
	public ImportedShop() {
		additionalData = new HashMap<>();
	}
	
	
	
	public ImportedShop(String id, String name) {
		this.id = id;
		this.name = name;
	}
}
