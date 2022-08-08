package com.nasnav.request;

import lombok.Data;

import java.util.List;

@Data
public class ApiLogsSearchParam extends BaseSearchParams{
	private List<Long> users;
	private List<Long> organizations;
	private String created_after;
	private String created_before;
	private Boolean employees;
	private Integer start;
	private Integer count;
}