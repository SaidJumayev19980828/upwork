package com.nasnav.test.model;
import org.json.JSONObject;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Item{
	private Long stockId;
	private Integer quantity;
	
	
	public JSONObject toJsonObject() {
		return new JSONObject()
					.put("stock_id", stockId)
					.put("quantity", quantity);		
	}
}