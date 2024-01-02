package com.nasnav.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BlockChainValidatorResponse {
    private BlockChainValidatorResponseData data;
    private String message;
    private String status;
}
