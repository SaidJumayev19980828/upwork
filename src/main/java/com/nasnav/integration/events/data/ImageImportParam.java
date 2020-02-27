package com.nasnav.integration.events.data;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ImageImportParam{
	private Integer type;
	private Integer priority;
	private Boolean ignoreErrors;
	private Integer pageNum;
	private Integer PageCount;
}