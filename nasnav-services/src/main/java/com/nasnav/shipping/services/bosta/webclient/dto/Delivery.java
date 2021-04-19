package com.nasnav.shipping.services.bosta.webclient.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Delivery {

    private Address pickupAddress;
    private Address dropOffAddress;
    private Address returnAddress;
    private Receiver receiver;
    private Tracker tracker;
    private State state;
    private String trackingNumber;
    private String notes;
    private BigDecimal cod; //cash on delivery
    private String businessReference;

    private Long type;
    private Boolean isSameDay;
    private String subAccountId;
    private String webhookUrl;
    
    private PackageSpec specs;

}
