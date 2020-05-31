package com.nasnav.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductTagPair {
	private Long productId;
	private Long tagId;
}
