package com.nasnav.integration.events.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockEventParam {
	private String variantId;
	private String shopId;
}
