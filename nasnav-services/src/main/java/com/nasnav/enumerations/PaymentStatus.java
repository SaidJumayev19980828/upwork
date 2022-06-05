package com.nasnav.enumerations;

import lombok.Getter;

public enum PaymentStatus {
    UNPAID(0), PAID(1), FAILED(2), ERROR(3), STARTED(4), AUTHORIZED(5), COD_REQUESTED(10), REFUNDED(21);

    @Getter
    private int value;

    PaymentStatus(int value) {
        this.value = value;
    }

    public static PaymentStatus getPaymentStatus(int value) {

        for(PaymentStatus status : PaymentStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return null;
    }
}
