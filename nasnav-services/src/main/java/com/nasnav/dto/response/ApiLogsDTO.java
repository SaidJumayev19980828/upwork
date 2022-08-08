package com.nasnav.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.BaseRepresentationObject;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ApiLogsDTO extends BaseRepresentationObject {
	private Long id;
	private String url;
	private String callDate;
	private Long userId;
	private String userName;
	private String userEmail;
	private Long organizationId;
	private String requestParameters;
	private String requestContent;
	private Integer responseCode;
}