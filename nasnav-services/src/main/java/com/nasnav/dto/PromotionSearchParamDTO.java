package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PromotionSearchParamDTO{
	private String status;
	@JsonProperty("start_date")
	private String startTime;
	@JsonProperty("end_date")
	private String endTime;
	private Long id;
	private Integer start;
	private Integer count;
}
