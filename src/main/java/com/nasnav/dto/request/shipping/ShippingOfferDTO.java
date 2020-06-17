package com.nasnav.dto.request.shipping;

import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ShippingOfferDTO {
	private String serviceId;
	private String serviceName;
	private List<ShippingAdditionalDataDTO> additionalData;
	private List<ShipmentDTO> shipments;
	
	public BigDecimal getTotal() {
		return ofNullable(shipments)
				.orElse(emptyList())
				.stream()
				.map(ShipmentDTO::getShippingFee)
				.reduce(ZERO, BigDecimal::add);
	}
}
