package com.nasnav.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.enumerations.OrderSortOptions;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderSearchParam extends BaseSearchParams {
    private Long user_id;
    private List<Long> org_id;
    private List<String> status;
    private Integer details_level;    
    private List<Long> shop_id;
    private BigDecimal min_total;
    private BigDecimal max_total;
    private OrderSortOptions orders_sorting_option;
    @JsonIgnore
    private List<Integer> status_ids;

    private String updated_after;    
    private String updated_before;
    private String created_after;
    private String created_before;

    private String payment_operator;
    private String shipping_service_id;

    private Integer start;
    private Integer count;

    private Boolean useCount;
}
