package com.nasnav.test.external.univocity;

import java.math.BigDecimal;

import lombok.Data;

@Data	
public class CSVRowBean {
	protected String name;
	protected BigDecimal price;
	protected Long quantity;
}
