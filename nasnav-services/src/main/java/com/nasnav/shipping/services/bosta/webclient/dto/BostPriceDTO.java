package com.nasnav.shipping.services.bosta.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BostPriceDTO {
	private BigDecimal total;
	private BigDecimal vat;
	@JsonProperty("SEND")
	private BigDecimal send;
	private BigDecimal codAmount;
	private BigDecimal codFees;
}
