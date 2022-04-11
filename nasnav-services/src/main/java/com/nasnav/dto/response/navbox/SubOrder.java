package com.nasnav.dto.response.navbox;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.nasnav.dto.AddressRepObj;
import com.nasnav.dto.BasketItem;
import com.nasnav.dto.LoyaltyOrderDetailDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonPropertyOrder({"shop_id", "shop_name", "suborder_id", "subtotal", "total", "status",
        "total_quantity", "shipment", "delivery_address", "items", "points", "total_point_amount"})
@Data
public class SubOrder {
    @JsonProperty("shop_id")
    private Long shopId;
    @JsonProperty("shop_name")
    private String shopName;
    @JsonProperty("shop_logo")
    private String shopLogo;
    @JsonProperty("suborder_id")
    private Long subOrderId;
    private BigDecimal subtotal;
    private BigDecimal total;
    private String status;
    @JsonProperty("creation_date")
    private LocalDateTime creationDate;
    @JsonProperty("total_quantity")
    private Long totalQuantity;
    private Shipment shipment;
    @JsonProperty("delivery_address")
    private AddressRepObj deliveryAddress;
    private List<BasketItem> items;
    private BigDecimal discount;
    @JsonIgnore
    private boolean pickup;

    @EqualsAndHashCode.Exclude
    private List<LoyaltyOrderDetailDTO> points;
    @JsonProperty("total_point_amount")
    private BigDecimal totalPointAmount;
}
