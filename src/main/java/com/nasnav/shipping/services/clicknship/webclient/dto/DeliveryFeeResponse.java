package com.nasnav.shipping.services.clicknship.webclient.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class DeliveryFeeResponse {
    private List<DeliveryFee> deliveryFee;
}
