package com.nasnav.integration.microsoftdynamics.webclient.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReturnSalesOrder {
    private String salesId;
    private List<ReturnSalesOrderItem> items;
}
