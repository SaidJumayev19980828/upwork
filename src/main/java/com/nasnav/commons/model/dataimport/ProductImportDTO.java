package com.nasnav.commons.model.dataimport;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class ProductImportDTO {
	protected Long variantId;
	protected String externalId;
	protected String name;
	protected String pname;
	protected String description;
	protected String barcode;
	protected String category;
	protected String brand;
	protected Integer quantity;
	protected BigDecimal price;
	protected Map<String,String> features;
	
	
	public ProductImportDTO() {
		features = new HashMap<>();
	}
}
