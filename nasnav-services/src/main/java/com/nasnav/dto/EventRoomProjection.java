package com.nasnav.dto;

public interface EventRoomProjection {
    EventTemplateProjection getTemplate();
    Long getInterest();

    EventProjection getEvent();
}
