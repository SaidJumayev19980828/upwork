package com.nasnav.dto.response;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.PromosConstraints;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PromotionDTO {
	private Long id;
	private String identifier;
	private String name;
	private String description;
	private String banner;
	private String cover;

	private Long organizationId;
	private Integer typeId;
	private Integer classId;

	private ZonedDateTime startDate;
	private ZonedDateTime endDate;
	
	private String status;
	private String code;
	private PromosConstraints constrains;
	private Map<String,Object> discount;
	private Long userId;
	private String userName;
	private LocalDateTime createdOn;
	private Integer priority;
}
