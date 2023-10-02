package com.nasnav.dto;

import com.nasnav.persistence.EventAttachmentsEntity;
import java.time.LocalDateTime;
import java.util.List;

public interface EventProjection {
        Long getId();
        LocalDateTime getStartsAt();
        LocalDateTime getEndsAt();

        InfluencerProjection getInfluencer();
        List<EventAttachmentsEntity> getAttachments();
        String getName();
        String getDescription();
        Integer getStatus();


    }






