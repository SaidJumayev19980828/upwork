package com.nasnav.dto.request;

import com.nasnav.enumerations.EventStatus;
import com.nasnav.persistence.EventAttachmentsEntity;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class EventForRequestDTO {
    private Long id;
    @NotNull
    private LocalDateTime startsAt;
    @NotNull
    private LocalDateTime endsAt;
    @NotNull
    private Long organizationId;
    private Long influencerId;
    @NotNull
    private Boolean visible;
    private List<EventAttachmentsEntity> attachments;
    private String name;
    private String description;
    private EventStatus status;
    private Set<Long> productsIds;

    private Long coin;
}
