package com.nasnav.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class DepositBlockChainRequest {
        private float tokenAmount;
        private String txHash;
        private String walletAddress;
        private String apiKey;
}
