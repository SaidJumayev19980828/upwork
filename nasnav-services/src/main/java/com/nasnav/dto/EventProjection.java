package com.nasnav.dto;

import com.nasnav.enumerations.EventStatus;
import com.nasnav.persistence.EventAttachmentsEntity;
import java.time.LocalDateTime;
import java.util.List;

public interface EventProjection {
        Long getId();
        LocalDateTime getStartsAt();
        LocalDateTime getEndsAt();

        List<InfluencerProjection> getInfluencers();
        List<EventAttachmentsEntity> getAttachments();
        String getName();
        String getDescription();
        EventStatus getStatus();
        OrganizationProjection getOrganization();

    }







