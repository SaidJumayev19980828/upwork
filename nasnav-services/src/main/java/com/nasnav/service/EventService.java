package com.nasnav.service;

import com.nasnav.dto.request.EventForRequestDTO;
import com.nasnav.dto.response.EventInterestDTO;
import com.nasnav.dto.response.EventResponseDto;
import com.nasnav.enumerations.EventStatus;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EventEntity;

import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface EventService {
    EventResponseDto createEvent(EventForRequestDTO dto);
    EventResponseDto getEventById(Long eventId);
    List<EventResponseDto> getEventsByOrgIdForUsers(Long orgID, EventStatus status);
    PageImpl<EventResponseDto> getAllEventsForUser(Integer start, Integer count, Date dateFilter);
    PageImpl<EventResponseDto> getEventsForEmployee(Integer start, Integer count, EventStatus status, LocalDateTime fromDate, LocalDateTime toDate);
    List<EventResponseDto> getAdvertisedEvents();
    List<EventResponseDto> getAdvertisedEventsForInfluencer();
    PageImpl<EventInterestDTO> getInterestsByEventId(Long eventId,Integer start, Integer count);
    void updateEvent(EventForRequestDTO dto, Long eventId);
    void deleteEvent(Long eventId, Boolean force);
    void intersetEventForUser(Long eventId);
    boolean hasInfluencerOrEmployeeAccessToEvent(BaseUserEntity user, Long eventId);
    boolean hasInfluencerOrEmployeeAccessToEvent(BaseUserEntity user, EventEntity event);
    EventResponseDto toDto(EventEntity entity);
    PageImpl<EventResponseDto> getAllEvents(Integer start, Integer count);

}
