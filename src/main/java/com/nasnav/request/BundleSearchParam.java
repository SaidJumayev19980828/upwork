package com.nasnav.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.commons.enums.SortOrder;
import com.nasnav.dto.ProductSortOptions;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class BundleSearchParam {
	
	//had to ignore java naming convention, because the bundle request parameters will be mapped to these properties by name
	private Long org_id;
	//private Long category_id;
	private Long bundle_id;
	private Integer start;
	private Integer count;
	private ProductSortOptions sort;
	private SortOrder order;
	
	public BundleSearchParam() {
		start = 0;
		count = 100;
		sort = ProductSortOptions.ID;
		order = SortOrder.ASC;
	}
	
	
	public void setSort(String sort) {
		this.sort = ProductSortOptions.valueOf(sort.toUpperCase());
	}
	
	
	public void setOrder(String order) {
		this.order = SortOrder.valueOf(order.toUpperCase());
	}
}
