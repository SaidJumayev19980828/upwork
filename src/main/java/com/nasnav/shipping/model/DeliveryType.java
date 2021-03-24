package com.nasnav.shipping.model;

import com.nasnav.enumerations.PromotionStatus;
import com.nasnav.exceptions.RuntimeBusinessException;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

import static com.nasnav.exceptions.ErrorCodes.PROMO$ENUM$0001;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

public enum DeliveryType {
    NORMAL_DELIVERY("Normal Delivery"), SAME_DAY_DELIVERY("Same Day Delivery");

    @Getter
    private String value;

    DeliveryType(String value) {
        this.value = value;
    }

    public static List<String> getDeliveryTypes() {
        return stream(values())
                .map(DeliveryType::getValue)
                .collect(toList());
    }
}
