package com.nasnav.commons.model.dataimport;

import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.math.BigDecimal.ZERO;

@Data
public class ProductImportDTO {
	protected Long variantId;
	protected String externalId;
	protected String name;
	protected String pname;
	protected String description;
	protected String barcode;
	protected Set<String> tags;
	protected String brand;
	protected Integer quantity;
	protected BigDecimal price;
	protected Map<String,String> features;
	protected Map<String,String> extraAttributes;
	protected String productGroupKey;
	protected BigDecimal discount;
	protected String sku;
	protected String productCode;
	protected String unit;
	protected BigDecimal weight;
	
	public ProductImportDTO() {
		features = new HashMap<>();
		tags = new HashSet<>();
		extraAttributes = new HashMap<>();
		discount = ZERO;
	}
}
