package com.nasnav.commons.model.dataimport;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductImportCsvRowData {
	protected String name;
	protected String pname;
	protected String description;
	protected String barcode;
	protected String category;
	protected String brand;
	protected Long quantity;
	protected BigDecimal price;
}
