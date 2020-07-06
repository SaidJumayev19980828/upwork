package com.nasnav.enumerations;

import com.nasnav.exceptions.RuntimeBusinessException;
import lombok.Getter;

import java.util.Arrays;

import static com.nasnav.exceptions.ErrorCodes.ENUM$0001;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

public enum ShippingStatus {


    DRAFT(0), REQUSTED(1),

    EN_ROUTE(10),
    //PENDING(10), IN_PROGRESS(15), DELIVERY_ON_ROUTE(16), DELIVERING(35), EN_ROUTE_TO_WAREHOUSE(36),
    PICKED_UP(20),
    //PICKING_UP(20), PICKING_UP_FROM_WEARHOUSE(21), PICKED_UP(30),
    DELIVERED(45),
    //ARRIVED_AT_WAREHOUSE(22), ARRIVED_AT_CUSTOMER(40), ARRIVED_AT_BUSINESS(25), RECEIVING(26),  RECEIVED_AT_WAREHOUSE(23), DELIVERED(45),
    CANCELED(50),
    FAILED(55);
    //FAILED(55), PICKUP_FAILED(80);

    @Getter
    private Integer value;

    ShippingStatus(Integer value) {
        this.value = value;
    }

    public static String getShippingStatusName(int value) {
        return Arrays.stream(ShippingStatus.values())
                     .filter(s -> s.value == value)
                     .findFirst()
                     .map(s -> s.name())
                     .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ENUM$0001));
    }
}
