package com.nasnav.dto;

import java.math.BigDecimal;

import org.apache.poi.hpsf.Decimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddonDetailsDTO {
	
	private Long addonStockId;
	private String addonName;
	private Long addonId;
	private BigDecimal price;
	private Integer type;
	
	

}
