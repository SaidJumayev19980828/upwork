package com.nasnav.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.commons.enums.SortOrder;
import com.nasnav.dto.ProductSortOptions;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductSearchParam {

    //had to ignore java naming convention, because the bundle request parameters will be mapped to these properties by name
    public Long org_id;
    public Long shop_id;
    public Long brand_id;
    public Long category_id;
    public List<Long> tags;
    public String name;
    public Integer start;
    public Integer count;
    public ProductSortOptions sort;
    public SortOrder order;
    public BigDecimal minPrice;
    public BigDecimal maxPrice;

    public void setSort(String sort) {
        this.sort = ProductSortOptions.valueOf(sort.toUpperCase());
    }


    public void setOrder(String order) {
        this.order = SortOrder.valueOf(order.toUpperCase());
    }

    @Override
    public String toString() {
        if (this.org_id == null)
            return null;

        String result = "";

        if (this.org_id != null)
            result += "&org_id="+this.org_id;

        if (this.shop_id != null)
            result += "&shop_id="+this.shop_id;

        if (this.brand_id != null)
            result += "&brand_id="+this.brand_id;

        if (this.name != null)
            result += "&name="+this.name;

        if (this.start != null)
            result += "&start="+this.start;

        if (this.count != null)
            result += "&count="+this.count;

        if (this.sort != null)
            result += "&sort="+this.sort;

        if (this.order != null)
            result += "&order="+this.order;

        if (this.tags != null)
            for(Long tagId: tags)
                result += "&tags="+tagId;

        return result.substring(1);

    }
}
