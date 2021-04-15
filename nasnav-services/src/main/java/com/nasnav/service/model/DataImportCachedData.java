package com.nasnav.service.model;

import com.nasnav.persistence.BrandsEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class DataImportCachedData {
	private Map<String, String> featureNameToIdMapping;
	private Map<String, BrandsEntity> brandsCache;
	private VariantCache variantsCache;
	
	public static DataImportCachedData emptyCache() {
		return new DataImportCachedData(new HashMap<>(), new HashMap<>(), VariantCache.emptyCache());
	}
}