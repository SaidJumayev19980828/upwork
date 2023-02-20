package com.nasnav.yeshtery.controller.v1;

import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dto.request.EventForRequestDTO;
import com.nasnav.dto.response.EventInterestDTO;
import com.nasnav.dto.response.EventResponseDto;
import com.nasnav.enumerations.EventStatus;
import com.nasnav.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(EventController.API_PATH)
public class EventController {
    static final String API_PATH = YeshteryConstants.API_PATH +"/event";

    @Autowired
    private EventService eventService;

    @PostMapping
    public void createEvent(@RequestBody EventForRequestDTO eventForRequestDto){
        eventService.createEvent(eventForRequestDto);
    }

    @GetMapping("/listForUser/{orgId}")
    public List<EventResponseDto> getEventsByOrgIdForUser(@PathVariable Long orgId,@RequestParam(required = false) EventStatus status){
        return eventService.getEventsByOrgIdForUsers(orgId,status);
    }

    @GetMapping("/list")
    public PageImpl<EventResponseDto> getEventsForEmployeePageable(@RequestParam(required = false, defaultValue = "0") Integer start,
                                                                   @RequestParam(required = false, defaultValue = "10") Integer count,
                                                                   @RequestParam(required = false) EventStatus status){
        return eventService.getEventsForEmployee(start, count, status);
    }

    @GetMapping("/list/advertise")
    public List<EventResponseDto> getAdvertisedEvents(){
        return eventService.getAdvertisedEvents();
    }

    @GetMapping("/list/advertiseForInfluencer")
    public List<EventResponseDto> getAdvertisedEventsForInfluencer(){
        return eventService.getAdvertisedEventsForInfluencer();
    }

    @GetMapping("/interests/{eventId}")
    public PageImpl<EventInterestDTO> getInterestsByEventId(@PathVariable Long eventId,
                                                            @RequestParam(required = false, defaultValue = "0") Integer start,
                                                            @RequestParam(required = false, defaultValue = "10") Integer count){
        return eventService.getInterestsByEventId(eventId,start,count);
    }

    @GetMapping("/{eventId}")
    public EventResponseDto getEventById(@PathVariable Long eventId){
        return eventService.getEventById(eventId);
    }

    @PutMapping("/{eventId}")
    public void updateEventById(@RequestBody EventForRequestDTO eventForRequestDto, @PathVariable Long eventId){
        eventService.updateEvent(eventForRequestDto, eventId);
    }
    @DeleteMapping("/{eventId}")
    public void deleteEventById(@PathVariable Long eventId){
        eventService.deleteEvent(eventId);
    }

    @PostMapping("/interset/{eventId}")
    public void intersetForUser(@PathVariable Long eventId){
        eventService.intersetEventForUser(eventId);
    }
}
