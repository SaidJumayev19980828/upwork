package com.nasnav.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.persistence.ProductFeaturesEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductFeatureDTO extends BaseRepresentationObject{
	private Integer id;
    private String name;    
    private String description;
    
    @JsonProperty(value = "p_name")
    private String pname;
    
    
    public ProductFeatureDTO(ProductFeaturesEntity entity) {
    	this.id = entity.getId();
    	this.name = entity.getName();
    	this.pname = entity.getPname();
    	this.description = entity.getDescription();
    }
}
