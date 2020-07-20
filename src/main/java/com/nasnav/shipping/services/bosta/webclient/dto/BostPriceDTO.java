package com.nasnav.shipping.services.bosta.webclient.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class BostPriceDTO {
	private BigDecimal total;
	private BigDecimal vat;
	@JsonProperty("SEND")
	private BigDecimal send;
}
