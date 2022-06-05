package com.nasnav.payments.paymob;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.persistence.MetaOrderEntity;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.FALSE;


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Builder
public class OrderRequest {
    @JsonProperty("auth_token")
    private String authToken;
    @JsonProperty("delivery_needed")
    private String deliveryNeeded = FALSE.toString();
    @JsonProperty("amount_cents")
    private BigDecimal amountCents;
    private String currency;
}
