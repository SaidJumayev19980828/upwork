package com.nasnav.shipping.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ShippingEta {
	private LocalDateTime from;
	private LocalDateTime to;
}
