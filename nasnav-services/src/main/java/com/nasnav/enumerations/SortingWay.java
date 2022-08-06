package com.nasnav.enumerations;

import lombok.Getter;

public enum SortingWay {
	ASC("asc"),
	DESC("desc");

	@Getter
	private String value;

	SortingWay(String value){
		this.value = value;
	}
}
