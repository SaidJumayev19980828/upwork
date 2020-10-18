package com.nasnav.shipping.model;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
public class ReturnShipmentTracker extends ShipmentTracker{
    private String emailMessage;

    public ReturnShipmentTracker(ShipmentTracker tracker, String emailMessage){
        this.setAirwayBillFile(tracker.getAirwayBillFile());
        this.setShipmentExternalId(tracker.getShipmentExternalId());
        this.setTracker(tracker.getTracker());
        this.setShippingDetails(tracker.getShippingDetails());
        this.setEmailMessage(emailMessage);
    }
}
