package com.nasnav.shipping.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Parameter {
	private String name;
	private ParameterType type;
}
