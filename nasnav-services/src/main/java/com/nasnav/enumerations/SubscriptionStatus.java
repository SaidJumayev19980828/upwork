package com.nasnav.enumerations;

import lombok.Getter;

public enum SubscriptionStatus {
	INCOMPLETE("incomplete"),
	INCOMPLETE_EXPIRED("incomplete_expired"),
	TRIALING("trialing"),
	ACTIVE("active"),
	PAST_DUE("past_due"),
	CANCELED("canceled"),
	UNPAID("unpaid"),
	PAUSED("paused");

	@Getter
	private String value;

	SubscriptionStatus(String value){
		this.value = value;
	}
}
