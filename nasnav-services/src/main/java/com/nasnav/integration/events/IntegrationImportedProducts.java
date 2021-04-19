package com.nasnav.integration.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntegrationImportedProducts {
	private Integer totalPages;
	private List<ShopImportedProducts> allShopsProducts;	
}
