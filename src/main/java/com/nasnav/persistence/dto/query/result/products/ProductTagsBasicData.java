package com.nasnav.persistence.dto.query.result.products;

import lombok.Data;

@Data
public class ProductTagsBasicData {
	private Long productId;
	private Long tagId;
	private String tagName;
	
	
	public ProductTagsBasicData(Long productId, Long tagId, String tagName) {
		this.productId = productId;
		this.tagId = tagId;
		this.tagName = tagName;
	}
	
}
