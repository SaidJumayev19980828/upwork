package com.nasnav.dto.response;

import com.nasnav.enumerations.EventStatus;
import com.nasnav.persistence.EventAttachmentsEntity;
import com.nasnav.persistence.ProductEntity;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventResponseDto {
    private Long id;
    @NotNull
    private LocalDateTime startsAt;
    @NotNull
    private LocalDateTime endsAt;
    @NotNull
    private GeneralRepresentationDto organization;
    private GeneralRepresentationDto influencer;
    private Boolean visible;
    private List<EventAttachmentsEntity> attachments;
    private String name;
    private String description;
    private EventStatus status;
    private List<ProductEntity> products;
}
