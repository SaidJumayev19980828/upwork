package com.nasnav.dto.request;


import lombok.Data;

@Data
public class CurrencyPriceItemDTO {
    private String currency;
    private String price;
}
