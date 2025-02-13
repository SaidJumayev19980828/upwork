package com.nasnav.dto.request.shipping;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ShippingOfferDTO {
	private String serviceId;
	private String serviceName;
	private List<ShippingAdditionalDataDTO> additionalData;
	private List<ShipmentDTO> shipments;
	private BigDecimal total;
	private String type;
	private String icon;
	private boolean available;
	private String message;
}
