package com.nasnav.service.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.dto.ProductImportMetadata;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ImportProductContext {

	@JsonIgnore
	private List<ProductImportDTO> products;
	private ProductImportMetadata importMetaData;
	private Integer productsNum;
	private List<String> errors;
	private List<Tag> createdTags;
	private List<Brand> createdBrands;
	
	public ImportProductContext(List<ProductImportDTO> productImportDTOS, ProductImportMetadata productImportMetadata) {
		this.products = productImportDTOS;
		this.importMetaData = productImportMetadata;
		this.productsNum = productImportDTOS.size();
		this.errors = new ArrayList<>();
		this.createdTags = new ArrayList<>();
		this.createdBrands = new ArrayList<>();
	}
	
	
	
	public void logNewTag(Long id, String name) {
		createdTags.add(new Tag(id, name));
	};
	
	
	
	public void logNewBrand(Long id, String name) {
		createdBrands.add(new Brand(id, name));
	}
	
}





@Data
@AllArgsConstructor
class Tag{
	private Long id;
	private String name;
}



@Data
@AllArgsConstructor
class Brand{
	private Long id;
	private String name;
}
