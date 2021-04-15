package com.nasnav.commons.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IndexedData<T>{
	private Integer index;
	private T data;
}