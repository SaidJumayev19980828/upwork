package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.enumerations.ReturnRequestStatus;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ReturnRequestSearchParams {

    private ReturnRequestStatus status;
    private Integer start;
    private Integer count;
    private Long meta_order_id;
    private Long shop_id;
}
