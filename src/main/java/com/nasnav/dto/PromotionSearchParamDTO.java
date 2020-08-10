package com.nasnav.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PromotionSearchParamDTO{
	private String status;
	private String startTime;
	private String endTime;
	private Long id;
}
