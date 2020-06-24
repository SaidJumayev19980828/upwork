package com.nasnav.dto.response.navbox;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nasnav.dto.AddressRepObj;
import com.nasnav.dto.BasketItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SubOrder {
    @JsonProperty("shop_id")
    private Long shopId;
    @JsonProperty("shop_name")
    private String shopName;
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

    @JsonIgnore
    private BigDecimal shipping;
    /*
        "shipment": {
        "service_id": "BOSTA",
                "service_name": "bosta",
                "shop_id": 445,
                "shop_name": "Cairo Festival",
                "shipping_fee": 25,
                "eta": {
            "from": "12-06-2020",
                    "to": "14-06-2020"
}*/


}
