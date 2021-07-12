package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "user_id",
        "store_id",
        "order_id",
        "price",
        "status"
})
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class OrderRepresentationObject extends BaseRepresentationObject{

    @JsonProperty("order_id")
    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("shop_id")
    private Long shopId;

    @JsonProperty("status")
    private String status;

	@JsonProperty("price")
    private BigDecimal price;

    @JsonIgnore
    private HttpStatus code;

    @JsonProperty(value = "items")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<BasketItemDTO> items;

    public OrderRepresentationObject(Long orderId, Long shopId, BigDecimal price, List<BasketItemDTO> items, HttpStatus code) {
        this.id = orderId;
        this.shopId = shopId;
        this.price = price;
        this.code = code;
        this.items = items;
    }
}
