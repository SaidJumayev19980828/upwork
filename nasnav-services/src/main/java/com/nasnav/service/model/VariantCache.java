package com.nasnav.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Data
public class VariantCache{    
	private Map<String, VariantBasicData> idToVariantMap;
	private Map<String, VariantBasicData> externalIdToVariantMap;
	private Map<String, VariantBasicData> barcodeToVariantMap; 
	
	public static VariantCache emptyCache() {
		return new VariantCache(new HashMap<>(), new HashMap<>(), new HashMap<>());
	}
}