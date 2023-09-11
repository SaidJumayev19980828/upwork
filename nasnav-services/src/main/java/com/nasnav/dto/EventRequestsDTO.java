package com.nasnav.dto;

import com.nasnav.dto.response.EventResponseDto;
import com.nasnav.enumerations.EventRequestStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventRequestsDTO {
    private Long id;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private EventResponseDto event;
    private EventRequestStatus status;
}
