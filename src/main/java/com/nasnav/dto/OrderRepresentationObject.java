package com.nasnav.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "user_id",
        "store_id",
        "order_id",
        "price",
        "status"
})
@Data
@EqualsAndHashCode(callSuper=true)
public class OrderRepresentationObject extends BaseRepresentationObject{

    @JsonProperty("order_id")
    private long id;

    @JsonProperty("user_id")
    private long userId;

    @JsonProperty("store_id")
    private long shopId;

    @JsonProperty("status")
    private long status;

	@JsonProperty("price")
    private BigDecimal price;
}
