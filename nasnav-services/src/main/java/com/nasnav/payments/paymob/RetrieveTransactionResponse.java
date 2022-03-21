package com.nasnav.payments.paymob;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class RetrieveTransactionResponse {
    private String transaction;
    private PaymentDetails obj;



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    static class PaymentDetails {
        private Long id;
        private Boolean pending;
        private BigDecimal amountCents;
        private Boolean success;
        private Boolean isAuth;
        private Boolean isCapture;
        private Boolean isStandalonePayment;
        private Boolean isVoided;
        private Boolean isRefund;
        private Boolean is3dSecure;
        private Boolean errorOccured;
        private Long owner;
        private Boolean parentTransaction;
        private PaymentResponseData data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    static class PaymentResponseData {

        private String merchantTxnRef;
        private String cardNum;
        private String avsResultCode;
        private String orderInfo;
        private String merchant;
        private String avsAcqResponseCode;
        private String transactionNo;
        private String batchNo;
        private String message;
        private String txnResponseCode;
        private String secureHash;
        private String cardType;
        private String receiptNo;
        private String createdAt;
        private String currency;
        private String klass;
        private String authorizeId;
        private String amount;
        private String acqResponseCode;
        private String command;
        private Long gatewayIntegrationPk;

    }
}
