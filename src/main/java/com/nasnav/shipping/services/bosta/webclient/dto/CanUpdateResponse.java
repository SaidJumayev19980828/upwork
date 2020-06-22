package com.nasnav.shipping.services.bosta.webclient.dto;

import lombok.Data;

@Data
public class CanUpdateResponse {
    private Boolean canUpdate;
    private String reason;
}
