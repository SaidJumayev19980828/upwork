package com.nasnav.payments.paymob;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class WebhookCallbackResponse {
    private RetrieveTransactionResponse obj;
    private String type;
    private String transactionProcessedCallbackResponses;
}
