package com.nasnav.dto.request.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartCheckoutAdditionalData {
    private String name;
    private String type;
    private String value;
}
