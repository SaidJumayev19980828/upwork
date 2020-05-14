package com.nasnav.persistence.dto.query.result.products;

import lombok.Data;

@Data
public class BrandBasicData {
	private Long id;
	private String name;
	private Long orgId;
	
	public BrandBasicData(Long id, String name, Long orgId) {
		this.id = id;
		this.name = name;
		this.orgId = orgId;
	}
}
