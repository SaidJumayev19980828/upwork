package com.nasnav.service.model;

import java.util.Map;

import com.nasnav.persistence.ProductVariantsEntity;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class VariantCache{    
	private Map<String, ProductVariantsEntity> idToVariantMap;
	private Map<String, ProductVariantsEntity> externalIdToVariantMap;
	private Map<String, ProductVariantsEntity> barcodeToVariantMap; 
}