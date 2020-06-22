package com.nasnav.shipping.model;

import java.time.Period;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShippingPeriod {
	private Period from;
	private Period to;
}
