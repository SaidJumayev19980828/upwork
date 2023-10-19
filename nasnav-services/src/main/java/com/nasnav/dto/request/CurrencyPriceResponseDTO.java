package com.nasnav.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CurrencyPriceResponseDTO {
    private List<CurrencyPriceItemDTO> data;
    private String message;
    private Boolean status = false;
}
