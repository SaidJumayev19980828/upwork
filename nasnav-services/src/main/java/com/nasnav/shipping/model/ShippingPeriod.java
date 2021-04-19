package com.nasnav.shipping.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Period;

@Data
@AllArgsConstructor
public class ShippingPeriod {
	private Period from;
	private Period to;
}
