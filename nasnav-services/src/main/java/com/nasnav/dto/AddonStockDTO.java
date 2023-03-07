package com.nasnav.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AddonStockDTO extends BaseRepresentationObject {
	private Long id;
	private Long shopId;
	private Long addonId;
    private Integer quantity;
    private BigDecimal price;
    private String operation;

}
