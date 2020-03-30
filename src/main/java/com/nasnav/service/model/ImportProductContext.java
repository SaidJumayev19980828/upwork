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
	private List<Error> errors;
	private List<Tag> createdTags;
	private List<Brand> createdBrands;
	private List<Product> createdProducts;
	private List<Product> updatedProducts;
	
	
	public ImportProductContext(List<ProductImportDTO> productImportDTOS, ProductImportMetadata productImportMetadata) {
		this.products = productImportDTOS;
		this.importMetaData = productImportMetadata;
		this.productsNum = productImportDTOS.size();
		this.errors = new ArrayList<>();
		this.createdTags = new ArrayList<>();
		this.createdBrands = new ArrayList<>();
		this.createdProducts = new ArrayList<>();
		this.updatedProducts = new ArrayList<>();
	}
	
	
	
	public void logNewTag(Long id, String name) {
		createdTags.add(new Tag(id, name));
	};
	
	
	
	public void logNewBrand(Long id, String name) {
		createdBrands.add(new Brand(id, name));
	}
	
	
	
	public void logNewError(Throwable exception, String data, Integer rowNum) {
		errors.add(new Error(exception, data, rowNum));
	}
	
	
	public boolean isSuccess() {
		return errors.isEmpty();
	}
	
	
	public void logNewCreatedProduct(Long id, String name) {
		createdProducts.add(new Product(id, name));
	}
	
	
	public void logNewUpdatedProduct(Long id, String name) {
		updatedProducts.add(new Product(id, name));
	}
	
}





@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
class Tag{
	private Long id;
	private String name;
}



@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
class Brand{
	private Long id;
	private String name;
}



@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
class Error{
	private Throwable exception;
	private String data;
	private Integer rowNum;
	private String message;
	private String stackTrace;
	
	public Error(Throwable exception, String data, Integer rowNum) {
		this.data = data;
		this.rowNum = rowNum;
		this.exception = exception;
		
		StringBuilder msg = new StringBuilder();
	    msg.append(String.format("Error at Row[%d], with data[%s]", rowNum + 1, data));
	    msg.append(System.getProperty("line.separator"));
	    msg.append("Error Message: " + exception.getMessage());
	    
	    this.message = msg.toString();
	}
}




@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
class Product{
	private Long id;
	private String name;
}