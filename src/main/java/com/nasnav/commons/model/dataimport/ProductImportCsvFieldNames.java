package com.nasnav.commons.model.dataimport;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public abstract class ProductImportCsvFieldNames {
	protected String name;
	protected String pname;
	protected String description;
	protected String barcode;
	protected String category;
	protected String brand;
	protected String quantity;
	protected String price;
		
}
