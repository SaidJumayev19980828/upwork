package com.nasnav.dto;

import com.nasnav.enumerations.EventStatus;
import com.nasnav.persistence.EventAttachmentsEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventsNewDTO {

    private Long id;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private List<InfluencerProjection> influencers;
    private List<EventAttachmentsEntity> attachments;
    private String name;
    private String description;
    private EventStatus status;
    private OrganizationNewDTO organization;

    // Getters and setters for the above properties
}


