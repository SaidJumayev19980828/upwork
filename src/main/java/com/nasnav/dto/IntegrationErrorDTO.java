package com.nasnav.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class IntegrationErrorDTO {
	private Long id;
	private String eventType;
	private String eventData;
	private LocalDateTime createdAt;
	private String handleException;
	private String fallbackException;
}
