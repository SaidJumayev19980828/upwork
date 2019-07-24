package com.nasnav.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "order_id",
        "price"
})
@Data
@EqualsAndHashCode(callSuper=true)
public class OrderRepresentationObject extends BaseRepresentationObject{

	@JsonProperty("order_id")
    private long order_id;
	@JsonProperty("price")
    private BigDecimal price;
}
