package com.nasnav.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

/*@JsonPropertyOrder({
        "user_id",
        "store_id",
        "price",
        "status"
})*/

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@EqualsAndHashCode(callSuper=false)
public class DetailedOrderRepObject extends BaseRepresentationObject{
    @JsonProperty("user_id")
    private long userId;
    @JsonProperty("user_name")
    private String userName;
    @JsonProperty("shop_id")
    private long shopId;
    @JsonProperty("shop_name")
    private String shopName;
    @JsonProperty("shop_address")
    private String shopAddress;
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
    private LocalDateTime createdAt;
    @JsonProperty("delivery_date")
    private LocalDateTime deliveryDate;
    @JsonProperty("status")
    private String status;
    @JsonProperty("total_quantity")
    private Integer totalQuantity;
    @JsonProperty("payment_status")
    private String paymentStatus;

    @JsonProperty("shipping_address")
    private AddressRepObj shippingAddress;

    @JsonProperty("items")
    private List<BasketItem> items;
    
    @JsonProperty("meta_order_id")
    private Long metaOrderId;
    
    @JsonProperty("shipping_status")
    private String shippingStatus;
}
