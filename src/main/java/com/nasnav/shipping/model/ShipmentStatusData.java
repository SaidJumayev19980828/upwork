package com.nasnav.shipping.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ShipmentStatusData {
    private String serviceId;
    private Long orgId;
    private String externalShipmentId;
    private Integer state;
    private String message;
}
