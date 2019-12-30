package com.nasnav.request;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class OrderSearchParam {
    private Long user_id;
    private Long org_id;
    private String status;
    private Integer details_level;    
    private Long shop_id;
    
    @JsonIgnore
    private Integer status_id;    
    
    private String updated_after;    
    private String updated_before;


    private Integer start;
    private Integer count;
}
