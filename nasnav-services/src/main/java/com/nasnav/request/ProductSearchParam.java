package com.nasnav.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.commons.enums.SortOrder;
import com.nasnav.dto.ProductSortOptions;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductSearchParam {

    //had to ignore java naming convention, because the bundle request parameters will be mapped to these properties by name
    public Long org_id;
    public Long shop_id;
    public Long brand_id;
    public Long tags_org_id;
    public Long category_id;
    public Set<Long> category_ids;
    public String category_name;
    public Set<Long> tags;
    public Set<Long> tag_ids;
    public String name;
    public Integer start;
    public Integer count;
    public ProductSortOptions sort;
    public SortOrder order;
    public BigDecimal minPrice;
    public BigDecimal maxPrice;
    public Integer[] product_type;
    public Boolean show_free_products;
    public Boolean hide_empty_stocks;
    public Boolean include_out_of_stock;
    public boolean yeshtery_products;
    public Map<String, List<String>> features;

    public void setSort(ProductSortOptions sort) {
        this.sort = sort;
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

        if (this.product_type != null)
            for(Integer type: product_type)
                result += "&product_type="+type;

        if (this.tag_ids != null)
            for(Long tagId: tag_ids)
                result += "&tag_ids="+tagId;

        return result.substring(1);

    }
}
