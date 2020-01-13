package com.nasnav.integration.events.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PaymentData {
	private Long id;
	private BigDecimal value;
	private Long userId;
	private String currency;
	private Long orderId;
	private LocalDateTime excutionTime;
}
