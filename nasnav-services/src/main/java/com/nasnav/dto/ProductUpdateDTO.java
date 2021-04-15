package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.constatnts.EntityConstants.Operation;
import com.nasnav.persistence.ProductEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductUpdateDTO  extends ProductEntity{
	
	private Operation operation;
    
    
	@Override
    @JsonProperty("p_name")
    public void setPname(String pname) {
    	super.setPname(pname);
    }
    
    
    public void setProductId(Long id) {
    	this.setId(id);
    }
    
    public Long getProductId() {
    	return this.getId();
    }
    
   
}
