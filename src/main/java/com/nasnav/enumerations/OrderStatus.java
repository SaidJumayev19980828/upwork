package com.nasnav.enumerations;

import java.util.Objects;

import lombok.Getter;

/**
 * Hold Response Status constants Prioritize integer value so that orders can be
 * updated in to higher value only
 */
public enum OrderStatus {
	//
	NEW(0)
	, CLIENT_CONFIRMED(1)
	, STORE_CONFIRMED(2)
	, STORE_PREPARED(3)
	, DISPATCHED(4)
	, DELIVERED(5)
	, STORE_CANCELLED(6)
	, CLIENT_CANCELLED(7)
	, FINALIZED(8)
	, RETURNED(9)
	, DISCARDED(10);

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
			if ( Objects.equals(status.getValue() ,statusValue) ) {
				return status;
			}
		}
		return null;
	}
}
