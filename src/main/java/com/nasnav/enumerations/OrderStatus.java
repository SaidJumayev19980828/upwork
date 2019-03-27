package com.nasnav.enumerations;

/**
 * Hold Response Status constants
 */
public enum OrderStatus {
	NEW, 
	CLIENT_CONFIRMED;
	
	public static OrderStatus findEnum(String statusString) {
		for(OrderStatus status : OrderStatus.values()) {
			if(status.name().equalsIgnoreCase(statusString)) {
				return status;
			}
		}
		return null;
	}
	
}