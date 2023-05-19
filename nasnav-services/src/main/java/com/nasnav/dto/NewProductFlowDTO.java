package com.nasnav.dto;

import static com.nasnav.dto.Required.ALWAYS;
import static com.nasnav.dto.Required.FOR_UPDATE;

import java.util.ArrayList;

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
public class NewProductFlowDTO  extends BaseJsonDTO{

	public Long productId=null;
	public String name;
	public Long brandId;
	public String description;
	public String priority;
	private Operation operation=null;
	public ArrayList<Long> tags;
	public ArrayList<String> keywords;
	@Override
	protected void initRequiredProperties() {
		setPropertyAsRequired("productId", FOR_UPDATE);
		setPropertyAsRequired("operation", ALWAYS);
		
	}
	

}
