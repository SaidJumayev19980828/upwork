package com.nasnav.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OutsideTransactionValidatorDTO {
    private String txhash;
    private String tokenAmount;
}
