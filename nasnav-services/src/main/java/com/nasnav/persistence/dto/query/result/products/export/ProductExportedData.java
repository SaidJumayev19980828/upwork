package com.nasnav.persistence.dto.query.result.products.export;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductExportedData {
	protected Long variantId;
	protected String externalId;
	protected String name;
	protected String description;
	protected String barcode;
	protected String tags;
	protected String brand;
	protected Integer quantity;
	protected BigDecimal price;
	protected String featureSpec;
	protected String productGroupKey;
	protected BigDecimal discount;
	protected Long productId;
	protected String sku;
	protected String productCode;
	protected String unitName;
	private BigDecimal weight;
}

