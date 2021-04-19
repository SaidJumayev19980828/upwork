package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.constatnts.EntityConstants.Operation;
import com.nasnav.enumerations.ProductFeatureType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductFeatureUpdateDTO extends BaseJsonDTO{
	
	private Integer featureId;
	private Operation operation;
	private String name;	
	private String description;
	
	@JsonProperty("p_name")
	private String pname;

	private Integer level;

	private ProductFeatureType type;
	
	
	@Override
	protected void initRequiredProperties() {
		setPropertyAsRequired("operation", Required.ALWAYS);
		setPropertyAsRequired("featureId", Required.FOR_UPDATE);
		setPropertyAsRequired("name", Required.FOR_CREATE);		
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		setPropertyAsUpdated("description");
		this.description = description;
	}


	public void setFeatureId(Integer featureId) {
		setPropertyAsUpdated("featureId");
		this.featureId = featureId;
	}



	public void setOperation(Operation operation) {
		setPropertyAsUpdated("operation");
		this.operation = operation;
	}


	public void setName(String name) {
		setPropertyAsUpdated("name");
		this.name = name;
	}


	public void setPname(String pname) {
		setPropertyAsUpdated("pname");
		this.pname = pname;
	}


	public void setLevel(Integer level) {
		setPropertyAsUpdated("level");
		this.level = level;
	}


	public void setType(ProductFeatureType type) {
		setPropertyAsUpdated("type");
		this.type = type;
	}
	

}
