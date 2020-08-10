package com.nasnav.dto.response;

import static com.nasnav.commons.utils.EntityUtils.DEFAULT_TIMESTAMP_PATTERN;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PromotionDTO {
	private Long id;
	private String identifier;
	private Long organizationId;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DEFAULT_TIMESTAMP_PATTERN)
	private LocalDateTime startDate;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DEFAULT_TIMESTAMP_PATTERN)
	private LocalDateTime endDate;
	
	private String status;
	private String code;
	private Map<String,Object> constrains;
	private Map<String,Object> discount;
	private Long userId;
	private String userName;
	private LocalDateTime createdOn;
}
