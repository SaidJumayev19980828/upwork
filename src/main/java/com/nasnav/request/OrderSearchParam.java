package com.nasnav.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class OrderSearchParam {
    private Long user_id;
    private Long org_id;
    private Long shop_id;
    private String status;
    @JsonIgnore
    private Integer status_id;
    private Integer details_level;
}
