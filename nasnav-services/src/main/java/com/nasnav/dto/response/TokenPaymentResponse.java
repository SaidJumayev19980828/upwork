package com.nasnav.dto.response;

public record TokenPaymentResponse(
        boolean status,
        TokenData data
) {}

record TokenData(
        String tokensUsed,
        String transactionId
) { }
