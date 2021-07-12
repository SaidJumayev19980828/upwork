package com.nasnav.dto.response.navbox;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class WishlistItem extends CartItem{

	@Override
	@JsonIgnore
	public Integer getQuantity(){
		return null;
	};
}
