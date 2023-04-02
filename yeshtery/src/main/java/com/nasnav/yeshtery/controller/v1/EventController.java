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

import static com.nasnav.constatnts.DefaultValueStrings.DEFAULT_PAGING_COUNT;

import java.util.List;

@RestController
@RequestMapping(EventController.API_PATH)
public class EventController {
    static final String API_PATH = YeshteryConstants.API_PATH +"/event";

    @Autowired
    private EventService eventService;

    @PostMapping
    public void createEvent(@RequestHeader(name = "User-Token", required = false) String userToken,
            @RequestBody EventForRequestDTO eventForRequestDto) {
        eventService.createEvent(eventForRequestDto);
    }

    @GetMapping("/listForUser/{orgId}")
    public List<EventResponseDto> getEventsByOrgIdForUser(@RequestHeader(name = "User-Token", required = false) String token,
                                                          @PathVariable Long orgId,@RequestParam(required = false) EventStatus status){
        return eventService.getEventsByOrgIdForUsers(orgId,status);
    }

    @GetMapping("/list")
    public PageImpl<EventResponseDto> getEventsForEmployeePageable(@RequestHeader(name = "User-Token", required = false) String token,
                                                                   @RequestParam(required = false, defaultValue = "0") Integer start,
                                                                   @RequestParam(required = false, defaultValue = DEFAULT_PAGING_COUNT) Integer count,
                                                                   @RequestParam(required = false) EventStatus status){
        return eventService.getEventsForEmployee(start, count, status);
    }

    @GetMapping("/list/advertise")
    public List<EventResponseDto> getAdvertisedEvents(@RequestHeader(name = "User-Token", required = false) String token){
        return eventService.getAdvertisedEvents();
    }

    @GetMapping("/list/advertiseForInfluencer")
    public List<EventResponseDto> getAdvertisedEventsForInfluencer(@RequestHeader(name = "User-Token", required = false) String token){
        return eventService.getAdvertisedEventsForInfluencer();
    }

    @GetMapping("/interests/{eventId}")
    public PageImpl<EventInterestDTO> getInterestsByEventId(@RequestHeader(name = "User-Token", required = false) String token,
                                                            @PathVariable Long eventId,
                                                            @RequestParam(required = false, defaultValue = "0") Integer start,
                                                            @RequestParam(required = false, defaultValue = DEFAULT_PAGING_COUNT) Integer count){
        return eventService.getInterestsByEventId(eventId,start,count);
    }

    @GetMapping("/{eventId}")
    public EventResponseDto getEventById(@RequestHeader(name = "User-Token", required = false) String token,
                                         @PathVariable Long eventId){
        return eventService.getEventById(eventId);
    }

    @PutMapping("/{eventId}")
    public void updateEventById(@RequestHeader(name = "User-Token", required = false) String token,
                                @RequestBody EventForRequestDTO eventForRequestDto,
                                @PathVariable Long eventId){
        eventService.updateEvent(eventForRequestDto, eventId);
    }
    @DeleteMapping("/{eventId}")
    public void deleteEventById(@RequestHeader(name = "User-Token", required = false) String token,
                                @PathVariable Long eventId){
        eventService.deleteEvent(eventId);
    }

    @PostMapping("/interset/{eventId}")
    public void intersetForUser(@RequestHeader(name = "User-Token", required = false) String token,
                                @PathVariable Long eventId){
        eventService.intersetEventForUser(eventId);
    }
}
