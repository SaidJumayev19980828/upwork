package com.nasnav.payments.misc;

public enum Gateway {
	MASTERCARD("mcard"),
	UPG("upg"),
	RAVE("rave"),
	COD("cod");

	private final String value;

	Gateway(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public static String getValue(Gateway gateway) {
		return gateway.value;
	}
}
