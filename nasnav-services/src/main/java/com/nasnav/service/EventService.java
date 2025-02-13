package com.nasnav.service;

import com.nasnav.dto.EventInterestsProjection;
import com.nasnav.dto.EventsNewDTO;
import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.request.EventForRequestDTO;
import com.nasnav.dto.response.EventInterestDTO;
import com.nasnav.dto.response.EventResponseDto;
import com.nasnav.enumerations.EventStatus;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EventEntity;
import org.springframework.data.domain.PageImpl;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface EventService {
    EventResponseDto createEvent(EventForRequestDTO dto);
    EventResponseDto getEventById(Long eventId);
    List<EventResponseDto> getEventsByOrgIdForUsers(Long orgID, EventStatus status);
    PageImpl<EventResponseDto> getAllEventsForUser(Integer start, Integer count, Date dateFilter);
    PageImpl<EventResponseDto> getAllEventsHistoryForUser(Integer start, Integer count, Long userId, Boolean previousEvents);
    PageImpl<EventsNewDTO> getEventsForEmployee(Integer start, Integer count, EventStatus status, LocalDateTime fromDate, LocalDateTime toDate,
            String name);
    PageImpl<EventResponseDto> getAdvertisedEvents(Integer start, Integer count, EventStatus status, LocalDateTime fromDate, LocalDateTime toDate,
            String name);
    List<EventResponseDto> getAdvertisedEventsForInfluencer();
    PageImpl<EventInterestDTO> getInterestsByEventId(Long eventId,Integer start, Integer count);
    void updateEvent(EventForRequestDTO dto, Long eventId);
    void deleteEvent(Long eventId, Boolean force);
    void intersetEventForUser(Long eventId) throws MessagingException, IOException;
    PageImpl<EventResponseDto> getEventByName(String name,Integer start, Integer count);
    boolean hasInfluencerOrEmployeeAccessToEvent(BaseUserEntity user, Long eventId);
    boolean hasInfluencerOrEmployeeAccessToEvent(BaseUserEntity user, EventEntity event);
    EventResponseDto toDto(EventEntity entity);
    PaginatedResponse<EventsNewDTO> getAllEvents(Integer start, Integer count, EventStatus eventStatus, LocalDateTime fromDate, LocalDateTime toDate, Long orgId);

    PageImpl<EventsNewDTO> getAllAdvertisedEvents(Integer start, Integer count,Long orgId);

     EventsNewDTO mapEventProjectionToDTO(EventInterestsProjection eventInterestsProjection);

     void sendInterestEmail(LocalDateTime startAt,String eventName ,String orgName ,String userName,String userEmail ,String mailTemplate , String emailSubject , String access) throws MessagingException, IOException;

}
