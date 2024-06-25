package com.nasnav.dto;

import com.nasnav.enumerations.EventStatus;
import com.nasnav.persistence.EventAttachmentsEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventsNewDTO {

    private Long id;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private List<InflunecerEventDto> influencers;
    private List<EventAttachmentsEntity> attachments;
    private String name;
    private String description;
    private EventStatus status;
    private OrganizationNewDTO organization;
    private Long interests;


    public static EventsNewDTO buildNewEventsFromEventProjection (EventProjection eventProjection ,Long interests,OrganizationNewDTO orgDTO) {
        return EventsNewDTO.builder()
                .id(eventProjection.getId())
                .name(eventProjection.getName())
                .interests(interests)
                .description(eventProjection.getDescription())
                .influencers(eventProjection.getInfluencers().stream().map(influencerProjection ->
                        new InflunecerEventDto(influencerProjection.getId(), influencerProjection.getName(),
                                influencerProjection.getImage(), influencerProjection.isEmployee())).toList())
                .startsAt(eventProjection.getStartsAt())
                .organization(orgDTO)
                .attachments(eventProjection.getAttachments())
                .endsAt(eventProjection.getEndsAt())
                .status(eventProjection.getStatus())
                .build();
    }

}


