package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.constatnts.EntityConstants.Operation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class BundleElementUpdateDTO extends BaseJsonDTO{
	
	private Long bundleId;
	private Operation operation;
	private Long productId;
	private Long variantId;
	
	
	@Override
	protected void initRequiredProperties() {
		setPropertyAsRequired("bundleId", Required.ALWAYS);
		setPropertyAsRequired("operation", Required.ALWAYS);
		setPropertyAsRequired("productId", Required.ALWAYS);		
		setPropertyAsRequired("variantId", Required.NEVER);		
	}


	public void setBundleId(Long bundleId) {
		setPropertyAsUpdated("bundleId");
		this.bundleId = bundleId;
	}


	public void setOperation(Operation operation) {
		setPropertyAsUpdated("operation");
		this.operation = operation;
	}


	public void setProductId(Long productId) {
		setPropertyAsUpdated("productId");
		this.productId = productId;
	}


	public void setVariantId(Long variantId) {
		setPropertyAsUpdated("variantId");
		this.variantId = variantId;
	}
	
	
	
}
