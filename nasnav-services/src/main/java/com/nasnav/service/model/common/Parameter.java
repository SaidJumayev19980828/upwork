package com.nasnav.service.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class Parameter {
	private String name;
	private ParameterType type;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<String> options;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@JsonProperty(value = "options")
	private Map<Long, Set<Long>> multipleOptions;
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
