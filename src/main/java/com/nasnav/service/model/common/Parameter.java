package com.nasnav.service.model.common;

import java.util.List;

import lombok.Data;

@Data
public class Parameter {
	private String name;
	private ParameterType type;
	private List<String> options;
	
	public Parameter(String name, ParameterType type) {
		this.name = name;
		this.type = type;
	}
}
