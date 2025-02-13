package com.nasnav.shipping.services.bosta.webclient.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StateHistory {
    private State state;
    private LocalDateTime timestamp;
    private UserInfo takenBy;
}
