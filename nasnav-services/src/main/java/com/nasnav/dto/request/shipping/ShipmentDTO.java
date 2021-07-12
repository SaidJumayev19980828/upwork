package com.nasnav.dto.request.shipping;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ShipmentDTO {
	private Long shopId;
	private String shopName;
	private BigDecimal shippingFee;
	private ShippingEtaDTO eta;
	private List<Long> stocks;
	private Long subOrderId;
}
