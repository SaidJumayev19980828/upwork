package com.nasnav.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class AddonStocksDTO {
	private Long addonStockId;
	private String addonName;
	private Long addonId;
	private Long shopId;
	private String shopName;
	
	
}
