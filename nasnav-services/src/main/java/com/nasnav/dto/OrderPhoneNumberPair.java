package com.nasnav.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class OrderPhoneNumberPair {
    private Long orderId;
    private String phoneNumber;
}
