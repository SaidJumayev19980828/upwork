package com.nasnav.dto.request.shipping;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@AllArgsConstructor
@NoArgsConstructor
public class ShippingAdditionalDataDTO {
	private String name;
	private String type;
	private List<Object> options;
	private Map<Long, Set<Long>> multipleOptions;

	public ShippingAdditionalDataDTO(String name, String type, List<Object> options) {
		this.name = name;
		this.type = type;
		this.options = options;
	}

	public ShippingAdditionalDataDTO(String name, String type, Map<Long, Set<Long>> multipleOptions) {
		this.name = name;
		this.type = type;
		this.multipleOptions = multipleOptions;
	}
}
