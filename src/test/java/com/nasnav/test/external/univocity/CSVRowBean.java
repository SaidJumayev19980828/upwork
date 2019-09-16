package com.nasnav.test.external.univocity;

import java.math.BigDecimal;

import com.univocity.parsers.annotations.Parsed;

import lombok.Data;

@Data	
public class CSVRowBean {
	private String name;
	private BigDecimal price;
	private Long quantity;
}
