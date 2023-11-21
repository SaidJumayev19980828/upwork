package com.nasnav.dto.request;

import com.nasnav.enumerations.EventStatus;
import com.nasnav.persistence.EventAttachmentsEntity;
import com.univocity.parsers.annotations.Validate;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
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
    private Set<Long> influencersIds;

    private String name;
    private String description;
    private EventStatus status;
    private Set<Long> productsIds;

    private Long coin;

    @NotBlank(message = "\"scene_id\" must not be blank or null")
    private String sceneId;
}
