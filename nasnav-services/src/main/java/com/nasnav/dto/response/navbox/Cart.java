package com.nasnav.dto.response.navbox;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.AppliedPromotionsResponse;
import com.nasnav.dto.response.LoyaltyPointsCartResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@AllArgsConstructor
@NoArgsConstructor
public class Cart {
	private List<CartItem> items;
	private BigDecimal total;
	private BigDecimal discount;
	private BigDecimal subtotal;
	private AppliedPromotionsResponse promos;
	private List<LoyaltyPointsCartResponseDto> pointsPerOrg;

	public Cart(List<CartItem> items) {
		this.items = items;
		total = discount = subtotal = BigDecimal.ZERO;
		promos = new AppliedPromotionsResponse();
		pointsPerOrg = new ArrayList<>();
	}
}
