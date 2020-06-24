package com.nasnav.dto.response.navbox;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nasnav.dto.DetailedOrderRepObject;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Order {
    @JsonProperty("sub_orders")
    List<SubOrder> subOrders;

    @JsonProperty("user_id")
    private Long userId;
    @JsonProperty("user_name")
    private String userName;
    @JsonProperty("order_id")
    private Long orderId;
    private BigDecimal subtotal;
    private BigDecimal shipping;
    private BigDecimal total;
    private String currency;
    @JsonProperty("creation_date")
    private LocalDateTime creationDate;

}
