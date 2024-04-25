package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProductDataDTO {

	private Long id;
	private ProductDetailsDTO productDTO;

	public ProductDataDTO(Long id, ProductDetailsDTO dto) {
		this.id = id;
		this.productDTO = dto;
	}
}
