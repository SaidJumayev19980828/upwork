package com.nasnav.commons.model.dataimport;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class ProductImportCsvRowData {
	protected String name;
	protected String pname;
	protected String description;
	protected String barcode;
	protected String brand;
	protected Integer quantity;
	protected BigDecimal price;
	protected Map<String,String> features;
}
