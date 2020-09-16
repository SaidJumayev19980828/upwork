package com.nasnav.dto.response.navbox;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.enumerations.TransactionCurrency;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@JsonPropertyOrder({"user_id", "user_name", "order_id", "subtotal", "shipping", "total",
                    "currency", "operator", "status", "creation_date", "sub_orders"})

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Order {
    List<SubOrder> subOrders;

    private Long userId;
    private String userName;
    private Long orderId;
    private BigDecimal subtotal;
    private BigDecimal shipping;
    private BigDecimal total;
    private TransactionCurrency currency;
    private LocalDateTime creationDate;
    private String operator;
    private BigDecimal discount;
    private String paymentStatus;
    private String status;
    private Boolean isCancelable;
}
