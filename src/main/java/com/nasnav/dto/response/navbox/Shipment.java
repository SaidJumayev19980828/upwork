package com.nasnav.dto.response.navbox;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.shipping.model.ShippingEta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@AllArgsConstructor
@NoArgsConstructor
public class Shipment extends BaseRepresentationObject {
    private String serviceId;
    private String serviceName;
    private BigDecimal shippingFee;
    private ShippingEta shippingEta;
    private String trackingNumber;
    private String externalId;
}
