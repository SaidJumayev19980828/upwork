package com.nasnav.integration.events;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntegrationImportedProducts {
	private Integer totalPages;
	private List<ShopImportedProducts> allShopsProducts;	
}
