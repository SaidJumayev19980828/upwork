package com.nasnav.shipping.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShippingEta {
	private LocalDateTime from;
	private LocalDateTime to;
	private String fromStr;
	private String toStr;

	public ShippingEta(LocalDateTime from, LocalDateTime to) {
		this.from = from;
		this.to = to;
	}
}
