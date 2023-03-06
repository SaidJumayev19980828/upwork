package com.nasnav.service;

import com.nasnav.dto.request.EventForRequestDTO;
import com.nasnav.dto.response.EventInterestDTO;
import com.nasnav.dto.response.EventResponseDto;
import com.nasnav.enumerations.EventStatus;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public interface EventService {
    void createEvent(EventForRequestDTO dto);
    EventResponseDto getEventById(Long eventId);
    List<EventResponseDto> getEventsByOrgIdForUsers(Long orgID, EventStatus status);
    PageImpl<EventResponseDto> getEventsForEmployee(Integer start, Integer count, EventStatus status);
    List<EventResponseDto> getAdvertisedEvents();
    List<EventResponseDto> getAdvertisedEventsForInfluencer();
    PageImpl<EventInterestDTO> getInterestsByEventId(Long eventId,Integer start, Integer count);
    void updateEvent(EventForRequestDTO dto, Long eventId);
    void deleteEvent(Long eventId);
    void intersetEventForUser(Long eventId);
}
