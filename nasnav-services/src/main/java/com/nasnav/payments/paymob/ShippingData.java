package com.nasnav.payments.paymob;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ShippingData {

    private Long id;
    private String firstName = "";
    private String lastName = "";
    private String street = "";
    private String building = "";
    private String floor = "";
    private String apartment = "";
    private String city = "";
    private String state = "";
    private String country = "";
    private String email = "";
    private String phoneNumber = "";
    private String postalCode = "";
    private String extraDescription = "";
    private String shippingMethod = "";
    private Long orderId;
    private Long order;
}
