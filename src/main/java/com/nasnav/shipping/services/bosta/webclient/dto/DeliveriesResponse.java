package com.nasnav.shipping.services.bosta.webclient.dto;

import lombok.Data;

import java.util.List;

@Data
public class DeliveriesResponse {

    private List<Delivery> deliveries;
    private Integer count;
}
