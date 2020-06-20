package com.nasnav.dto.request.shipping;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ShipmentDTO {
	private Long shopId;
	private String shopName;
	private BigDecimal shippingFee;
	private ShippingEtaDTO eta;
	private List<Long> stocks;
}
