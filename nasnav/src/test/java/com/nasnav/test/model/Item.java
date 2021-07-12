package com.nasnav.test.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.json.JSONObject;

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