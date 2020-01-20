package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.commons.model.dataimport.ProductImportCsvFieldNames;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CsvHeaderNamesDTO extends ProductImportCsvFieldNames{
	
	@JsonProperty("name_header")
	public void setName(String name) {
		this.name = name;
	}
	
	@JsonProperty("pname_header")
	public void setPname(String pname) {
		this.pname = pname;
	}
	
	
	@JsonProperty("description_header")
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	@JsonProperty("barcode_header")
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	@JsonProperty("brand_header")
	public void setBrand(String brand) {
		this.brand = brand;
	}
	
	
	@JsonProperty("quantity_header")
	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}
	
	
	@JsonProperty("price_header")
	public void setPrice(String price) {
		this.price = price;
	}
}
