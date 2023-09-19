package com.nasnav.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nasnav.dto.InfluencerDTO;
import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.dto.ProductDetailsDTO;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.enumerations.EventStatus;
import com.nasnav.persistence.EventAttachmentsEntity;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class EventResponseDto {
    private Long id;
    @NotNull
    private LocalDateTime startsAt;
    @NotNull
    private LocalDateTime endsAt;
    @NotNull
    private OrganizationRepresentationObject organization;
    private InfluencerDTO influencer;
    private Boolean visible;
    private List<EventAttachmentsEntity> attachments;
    private String name;
    private String description;
    private EventStatus status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private EventStatus statusRepresentation;
    private Set<ProductDetailsDTO> products;
    private List<EventResponseDto> relatedEvents;
}
