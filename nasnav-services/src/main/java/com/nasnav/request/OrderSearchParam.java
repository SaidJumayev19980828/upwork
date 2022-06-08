package com.nasnav.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.enumerations.OrderSortOptions;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderSearchParam {
    private Long user_id;
    private List<Long> org_id;
    private String status;
    private Integer details_level;    
    private List<Long> shop_id;
    private BigDecimal min_total;
    private BigDecimal max_total;
    private OrderSortOptions orders_sorting_option;
    @JsonIgnore
    private Integer status_id;    

    private String updated_after;    
    private String updated_before;

    private String payment_operator;
    private String shipping_service_id;

    private Integer start;
    private Integer count;
}
