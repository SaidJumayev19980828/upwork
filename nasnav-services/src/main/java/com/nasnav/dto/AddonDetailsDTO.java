package com.nasnav.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddonDetailsDTO {
	
	private Long addonStockId;
	private String addonName;
	private Long addonId;
	private BigDecimal price;
	private Integer type;
	
	

}
