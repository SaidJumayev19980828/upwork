package com.nasnav.enumerations;

import com.nasnav.exceptions.RuntimeBusinessException;
import lombok.Getter;

import java.util.Arrays;

import static com.nasnav.exceptions.ErrorCodes.ENUM$0001;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

public enum ShippingStatus {


    DRAFT(0),
    REQUSTED(1),
    EN_ROUTE(10),
    PICKED_UP(20),
    DELIVERED(45),
    CANCELED(50),
    FAILED(55),
    RETURNED(60),
    ERROR(-1);

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
                     .orElseGet(() -> ERROR.name());
    }
}
