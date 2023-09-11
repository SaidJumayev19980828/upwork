package com.nasnav.enumerations;

import lombok.Getter;

public enum Gender {
	MALE(true),
	FEMALE(false);

	@Getter
	boolean value;

	private Gender(boolean value) {
		this.value = value;
	}
}
