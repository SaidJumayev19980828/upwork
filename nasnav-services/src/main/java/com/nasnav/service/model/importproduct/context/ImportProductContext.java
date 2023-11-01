package com.nasnav.service.model.importproduct.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.service.model.DataImportCachedData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ImportProductContext {

	@JsonIgnore
	private List<ProductImportDTO> products;
	@JsonIgnore
	private DataImportCachedData cache;
	private ProductImportMetadata importMetaData;
	private Integer productsNum;
	@ToString.Exclude
	private List<Error> errors;
	private Set<Tag> createdTags;
	private Set<Brand> createdBrands;
	private Set<Product> createdProducts;
	private Set<Product> updatedProducts;

	public ImportProductContext() {
		initialize();
	}

	public ImportProductContext(List<ProductImportDTO> productImportDTOS, ProductImportMetadata productImportMetadata, DataImportCachedData cache) {
		initialize();
		this.products = productImportDTOS;
		this.importMetaData = productImportMetadata;
		this.productsNum = productImportDTOS.size();
		this.cache = cache;
	}


	private void initialize() {
		this.errors = new ArrayList<>();
		this.createdTags = new HashSet<>();
		this.createdBrands = new HashSet<>();
		this.createdProducts = new HashSet<>();
		this.updatedProducts = new HashSet<>();
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

	public void logNewErrorForMissingHeaders(String missingHeaders) {
		ErrorCodes errorCode = ErrorCodes.CSV$001;
		String message = String.format(errorCode.getValue(), missingHeaders);
		Error error = new Error();
		error.setErrorCode(errorCode.toString());
		error.setMessage(message);
		errors.add(error);
	}

	public void logNewErrorForCsvInvalidData(String errorMessage, int rowNum) {
		ErrorCodes errorCode = ErrorCodes.CSV$002;
		String columnName = getColumnNameContainsInvalidData(errorMessage);
		String message = String.format(errorCode.getValue(), columnName, rowNum);
		Error error = new Error(errorCode);
		error.setMessage(message);
		error.setRowNum(rowNum);
		errors.add(error);
	}

	public void logNewErrorForMissingXlsHeaders(String xlsMissingHeaders, int rowNum) {
		ErrorCodes errCode = ErrorCodes.XLS$001;
		Error error = new Error(errCode);
		String errMsg = String.format(errCode.getValue(), xlsMissingHeaders);
		error.setMessage(errMsg);
		error.setRowNum(rowNum);
		errors.add(error);

	}

	public void logNewXlsConversionError(String colName, int rowNum, ErrorCodes errorCode) {
		Error error = new Error(errorCode);
		error.setRowNum(rowNum);
		error.setMessage(String.format(errorCode.getValue(),rowNum, colName));
		errors.add(error);

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

	private String getColumnNameContainsInvalidData(String errMsg) {
		Pattern pattern = Pattern.compile("columnName=([^,]+)");
		Matcher matcher = pattern.matcher(errMsg);
		if(matcher.find())
			return matcher.group(1);

		return "";

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