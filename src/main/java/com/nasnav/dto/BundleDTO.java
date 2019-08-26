package com.nasnav.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class BundleDTO extends ProductBaseInfo{
	
	private BigDecimal price;
	private List<ProductBaseInfo> products;
}
