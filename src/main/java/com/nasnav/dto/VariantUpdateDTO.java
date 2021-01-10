package com.nasnav.dto;

import static com.nasnav.dto.Required.ALWAYS;
import static com.nasnav.dto.Required.FOR_CREATE;
import static com.nasnav.dto.Required.FOR_UPDATE;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.constatnts.EntityConstants.Operation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class VariantUpdateDTO extends BaseJsonDTO{
	
	private Long productId;
	private Long variantId;
	private Operation operation;
	private String name;	
	private String description;
	private String barcode;
	private String features;
	private String extraAttr;
	private String sku;
	private String productCode;
	private BigDecimal weight;
	
	@JsonProperty("p_name")
	private String pname;
	
	
	@Override
	protected void initRequiredProperties() {
		setPropertyAsRequired("productId", ALWAYS);
		setPropertyAsRequired("operation", ALWAYS);
		setPropertyAsRequired("variantId", FOR_UPDATE);
		setPropertyAsRequired("features", FOR_CREATE);		
	}


	public void setProductId(Long productId) {
		setPropertyAsUpdated("productId");
		this.productId = productId;
	}


	public void setVariantId(Long variantId) {
		setPropertyAsUpdated("variantId");
		this.variantId = variantId;
	}


	public void setOperation(Operation operation) {
		setPropertyAsUpdated("operation");
		this.operation = operation;
	}


	public void setName(String name) {
		setPropertyAsUpdated("name");
		this.name = name;
	}


	public void setDescription(String description) {
		setPropertyAsUpdated("description");
		this.description = description;
	}


	public void setBarcode(String barcode) {
		setPropertyAsUpdated("barcode");
		this.barcode = barcode;
	}


	public void setFeatures(String features) {
		setPropertyAsUpdated("features");
		this.features = features;
	}


	public void setPname(String pname) {
		setPropertyAsUpdated("pname");
		this.pname = pname;
	}
	
	
	public void setSku(String sku) {
		setPropertyAsUpdated("sku");
		this.sku = sku;
	}
	
	
	public void setProductCode(String productCode) {
		setPropertyAsUpdated("productCode");
		this.productCode = productCode;
	}


	public void setWeight(BigDecimal weight) {
		setPropertyAsUpdated("weight");
		this.weight = weight;
	}
	
}
