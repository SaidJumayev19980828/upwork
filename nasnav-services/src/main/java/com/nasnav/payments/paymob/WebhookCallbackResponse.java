package com.nasnav.payments.paymob;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WebhookCallbackResponse {
    private RetrieveTransactionResponse obj;
    private String type;
    @JsonProperty("transaction_processed_callback_responses")
    private String transactionProcessedCallbackResponses;
}
