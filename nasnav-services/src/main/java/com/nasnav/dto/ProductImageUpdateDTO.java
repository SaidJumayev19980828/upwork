package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.constatnts.EntityConstants.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import static com.nasnav.dto.Required.*;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Schema(name = "Image update meta data")
public class ProductImageUpdateDTO extends BaseJsonDTO {
	
	private Long productId;
	private Long variantId;
	private Long imageId;
	private Operation operation;
	private Integer type;
	private Integer priority;
	
	@Override
	
	protected void initRequiredProperties() {
		setPropertyAsRequired("productId", FOR_CREATE);
		setPropertyAsRequired("operation", ALWAYS);
		setPropertyAsRequired("imageId", FOR_UPDATE);
		setPropertyAsRequired("type", FOR_CREATE);
		setPropertyAsRequired("priority", FOR_CREATE);
	}
	
	

	public void setProductId(Long productId) {
		setPropertyAsUpdated("productId");
		this.productId = productId;
	}
	
	
	

	public void setVariantId(Long variantId) {
		setPropertyAsUpdated("variantId");
		this.variantId = variantId;
	}
	
	
	

	public void setImageId(Long imageId) {
		setPropertyAsUpdated("imageId");
		this.imageId = imageId;
	}
	
	

	public void setOperation(Operation operation) {
		setPropertyAsUpdated("operation");
		this.operation = operation;
	}
	
	

	public void setType(Integer type) {
		setPropertyAsUpdated("type");
		this.type = type;
	}
	
	

	public void setPriority(Integer priority) {
		setPropertyAsUpdated("priority");
		this.priority = priority;
	}
	
	
	

}
