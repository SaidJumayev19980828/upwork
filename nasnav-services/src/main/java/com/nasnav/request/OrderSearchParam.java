package com.nasnav.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class OrderSearchParam {
    private Long user_id;
    private List<Long> org_id;
    private String status;
    private Integer details_level;    
    private List<Long> shop_id;
    
    @JsonIgnore
    private Integer status_id;    
    
    private String updated_after;    
    private String updated_before;

    private String payment_method;
    private String shipping_method;

    private Integer start;
    private Integer count;
}
