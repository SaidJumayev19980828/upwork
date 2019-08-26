package com.nasnav.request;

import com.nasnav.commons.enums.SortOrder;
import com.nasnav.dto.ProductSortOptions;

import lombok.Data;

@Data
public class BundleSearchParam {
	
	private Long orgId;
	private Long categoryId;
	private Long bundleId;
	private Long start;
	private Long count;
	private ProductSortOptions sort;
	private SortOrder order;
	
	public BundleSearchParam() {
		start = 0L;
		count = 100L;
		sort = ProductSortOptions.ID;
		order = SortOrder.ASC;
	}
}
