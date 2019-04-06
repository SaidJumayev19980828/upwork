package com.nasnav.enumerations;

import lombok.Getter;

/**
 * Hold Response Status constants Prioritize integer value so that orders can be
 * updated in to higher value only
 */
public enum OrderStatus {
	//
	NEW(0), CLIENT_CONFIRMED(1);

	@Getter
	private Integer value;

	OrderStatus(Integer value) {
		this.value = value;
	}

	public static OrderStatus findEnum(String statusString) {
		for (OrderStatus status : OrderStatus.values()) {
			if (status.name().equalsIgnoreCase(statusString)) {
				return status;
			}
		}
		return null;
	}

	public static OrderStatus findEnum(Integer statusValue) {
		for (OrderStatus status : OrderStatus.values()) {
			if (status.getValue() == statusValue) {
				return status;
			}
		}
		return null;
	}
}