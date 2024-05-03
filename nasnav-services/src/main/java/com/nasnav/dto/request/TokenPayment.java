package com.nasnav.dto.request;

public record TokenPayment (
        String userTokenBalance,
       String brandOrgAddress,
       String usdcToSend
) { }
