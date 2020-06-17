package com.nasnav.shipping.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShippingEta {
	private LocalDate from;
	private LocalDate to;
}
