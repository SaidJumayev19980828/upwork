package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DetailedOrderRepObject extends BaseRepresentationObject{
    private long userId;
    private String userName;
    private long shopId;
    private String shopName;
    private String shopAddress;
    private long orderId;
    private BigDecimal subtotal;
    private BigDecimal shipping;
    private String shippingService;
    private String trackNumber;
    private String paymentOperator;
    private BigDecimal total;
    private String currency;
    private String notes;
    
    @JsonProperty("creation_date")
    private LocalDateTime createdAt;
    
    private LocalDateTime deliveryDate;
    private String status;
    private Integer totalQuantity;
    private String paymentStatus;
    private AddressRepObj shippingAddress;
    @EqualsAndHashCode.Exclude
    private List<BasketItem> items;
    private Long metaOrderId;
    private String shippingStatus;
    private BigDecimal discount;
}
