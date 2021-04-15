package com.nasnav.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IntegrationErrorDTO {
	private Long id;
	private String eventType;
	private String eventData;
	private LocalDateTime createdAt;
	private String handleException;
	private String fallbackException;
}
