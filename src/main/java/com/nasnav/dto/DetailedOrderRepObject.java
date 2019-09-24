package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
/*@JsonPropertyOrder({
        "user_id",
        "store_id",
        "price",
        "status"
})*/
@Data
@EqualsAndHashCode(callSuper=true)
public class DetailedOrderRepObject extends BaseRepresentationObject{
    @JsonProperty("user_id")
    private long userId;
    @JsonProperty("store_id")
    private long shopId;
    @JsonProperty("order_id")
    private long orderId;
    @JsonProperty("subtotal")
    private BigDecimal subtotal;
    @JsonProperty("shipping")
    private BigDecimal shipping;
    @JsonProperty("total")
    private BigDecimal total;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("creation_date")
    private Date createdAt;
    @JsonProperty("delivery_date")
    private Date deliveryDate;
    @JsonProperty("status")
    private String status;

    @JsonProperty("shipping_address")
    private ShippingAddress shippingAddress;

    @JsonProperty("items")
    private List<BasketItem> items;
}
