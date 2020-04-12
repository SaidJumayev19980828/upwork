package com.nasnav.service.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class VariantCache{    
	private Map<String, VariantBasicData> idToVariantMap;
	private Map<String, VariantBasicData> externalIdToVariantMap;
	private Map<String, VariantBasicData> barcodeToVariantMap; 
}