package com.nasnav.payments.misc;

public enum Gateway {
	MASTERCARD("mcard"),
	UPG("upg"),
	COD("cod");

	private final String value;

	Gateway(String value) {
		this.value = value;
	}

	String getValue() {
		return this.value;
	}
}
