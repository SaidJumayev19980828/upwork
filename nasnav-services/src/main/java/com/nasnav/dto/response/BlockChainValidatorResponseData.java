package com.nasnav.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BlockChainValidatorResponseData {

        private String walletAddress;
        private BigDecimal usdcAmount;
        private BigDecimal pricePerUsd;
        private Integer usdcFeePercent;
        private BigDecimal tokensReceived;
        private String tokensReceivedFormatted;
}
