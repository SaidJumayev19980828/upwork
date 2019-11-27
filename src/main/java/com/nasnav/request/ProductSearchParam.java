package com.nasnav.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.commons.enums.SortOrder;
import com.nasnav.dto.ProductSortOptions;

import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductSearchParam {

    //had to ignore java naming convention, because the bundle request parameters will be mapped to these properties by name
    public Long org_id;
    public Long shop_id;
    public Long category_id;
    public Long brand_id;
    public List<Long> tags;
    public String name;
    public boolean minprice;
    public Integer start;
    public Integer count;
    public ProductSortOptions sort;
    public SortOrder order;

    public void setSort(String sort) {
        this.sort = ProductSortOptions.valueOf(sort.toUpperCase());
    }


    public void setOrder(String order) {
        this.order = SortOrder.valueOf(order.toUpperCase());
    }
}
