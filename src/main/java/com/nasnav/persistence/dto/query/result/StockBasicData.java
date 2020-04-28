package com.nasnav.persistence.dto.query.result;

import lombok.Data;

@Data
public class StockBasicData {
	private Long variantId;
	private Long shopId;
	private Long stockId;
	
	public StockBasicData(Long variantId, Long shopId, Long stockId) {
		this.variantId = variantId;
		this.shopId = shopId;
		this.stockId = stockId;
	}
}
