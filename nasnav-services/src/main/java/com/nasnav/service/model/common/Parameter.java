package com.nasnav.service.model.common;

import lombok.Data;

import java.util.List;

@Data
public class Parameter {
	private String name;
	private ParameterType type;
	private List<String> options;
	private boolean required;

	public Parameter(String name, ParameterType type) {
		this(name, type, true);
	}


	public Parameter(String name, ParameterType type, boolean required) {
		this.name = name;
		this.type = type;
		this.required = required;
	}
}
