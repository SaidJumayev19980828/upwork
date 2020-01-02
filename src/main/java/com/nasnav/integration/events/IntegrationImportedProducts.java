package com.nasnav.integration.events;

import java.util.List;

import lombok.Data;

@Data
public class IntegrationImportedProducts {
	private List<ShopImportedProducts> allShopsProducts;
	private Integer totalPages;
}
