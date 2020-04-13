package com.nasnav.service.model.importproduct.context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.service.model.DataImportCachedData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ImportProductContext {

	@JsonIgnore
	private List<ProductImportDTO> products;
	@JsonIgnore
	private DataImportCachedData cache;
	private ProductImportMetadata importMetaData;
	private Integer productsNum;
	private List<Error> errors;
	private Set<Tag> createdTags;
	private Set<Brand> createdBrands;
	private Set<Product> createdProducts;
	private Set<Product> updatedProducts;
	
	
	public ImportProductContext(List<ProductImportDTO> productImportDTOS, ProductImportMetadata productImportMetadata, DataImportCachedData cache) {
		this.products = productImportDTOS;
		this.importMetaData = productImportMetadata;
		this.productsNum = productImportDTOS.size();
		this.errors = new ArrayList<>();
		this.createdTags = new HashSet<>();
		this.createdBrands = new HashSet<>();
		this.createdProducts = new HashSet<>();
		this.updatedProducts = new HashSet<>();
		this.cache = cache;
	}
	
	
	public ImportProductContext(List<ProductImportDTO> productImportDTOS, ProductImportMetadata productImportMetadata) {
		this(productImportDTOS, productImportMetadata, DataImportCachedData.emptyCache());
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
	
	
	public void logNewError(String message, Integer rowNum) {
		errors.add(new Error(message, rowNum));
	}
	
	
	public boolean isSuccess() {
		return errors.isEmpty();
	}
	
	
	public void setSuccess(boolean value) {
		//just to remove json deserialization errors
	}
	
	
	public void logNewCreatedProduct(Long id, String name) {
		createdProducts.add(new Product(id, name));
	}
	
	
	public void logNewUpdatedProduct(Long id, String name) {
		updatedProducts.add(new Product(id, name));
	}
	
}





@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
class Tag{
	private Long id;
	private String name;
}



@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
class Brand{
	private Long id;
	private String name;
}



@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
class Error{
	@JsonIgnore
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
	
	
	public Error(String message, Integer rowNum) {
		this.message = message;
		this.rowNum = rowNum;
	}
}