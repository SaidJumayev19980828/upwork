package com.nasnav.integration.events.data;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentData {
	private Long id;
	private BigDecimal value;
	private Long userId;
	private String currency;
	private Long orderId;
	private String externalOrderId;
	private LocalDateTime executionTime;
	private Long organizationId;
}
