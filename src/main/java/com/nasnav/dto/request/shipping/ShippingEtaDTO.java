package com.nasnav.dto.request.shipping;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ShippingEtaDTO {
	private LocalDate from;
	private LocalDate to;
}
