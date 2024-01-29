package com.nasnav.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventInterestDTO {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String image;
    private String userType;
    private LocalDateTime date;
    private Boolean influencer;
}
