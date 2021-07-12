package com.nasnav.test.external.univocity;

import lombok.Data;

import java.math.BigDecimal;

@Data	
public class CSVRowBean {
	protected String name;
	protected BigDecimal price;
	protected Long quantity;
}
