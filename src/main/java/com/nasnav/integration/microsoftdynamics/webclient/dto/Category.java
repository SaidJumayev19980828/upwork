package com.nasnav.integration.microsoftdynamics.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Category {
	@JsonProperty("category_id")
	private Long categoryId;
	
	@JsonProperty("CATEGORYHIERARCHY")
	private Long categoryHierarchy;
	
	@JsonProperty("PARENTCATEGORY")
	private Long parentCategory;
	
	@JsonProperty("category_name")
	private String categoryName;
	
	@JsonProperty("LEVEL_")
	private Integer level;
}
