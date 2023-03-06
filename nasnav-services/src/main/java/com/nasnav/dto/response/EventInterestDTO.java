package com.nasnav.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventInterestDTO {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime date;
}
