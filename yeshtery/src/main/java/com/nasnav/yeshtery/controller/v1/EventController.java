package com.nasnav.yeshtery.controller.v1;

import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dto.EventsNewDTO;
import com.nasnav.dto.request.EventForRequestDTO;
import com.nasnav.dto.response.EventInterestDTO;
import com.nasnav.dto.response.EventResponseDto;
import com.nasnav.enumerations.EventStatus;
import com.nasnav.persistence.EventEntity;
import com.nasnav.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static com.nasnav.constatnts.DefaultValueStrings.DEFAULT_PAGING_COUNT;

@RestController
@RequestMapping(EventController.API_PATH)
public class EventController {
    static final String API_PATH = YeshteryConstants.API_PATH +"/event";

    @Autowired
    private EventService eventService;

    @PostMapping
    public EventResponseDto createEvent(@RequestHeader(name = "User-Token", required = false) String userToken,
            @RequestBody EventForRequestDTO eventForRequestDto) {
        return eventService.createEvent(eventForRequestDto);
    }

    @GetMapping("/listForUser/{orgId}")
    public List<EventResponseDto> getEventsByOrgIdForUser(@RequestHeader(name = "User-Token", required = false) String token,
                                                          @PathVariable Long orgId,@RequestParam(required = false) EventStatus status){
        return eventService.getEventsByOrgIdForUsers(orgId,status);
    }
    

    @GetMapping("/listForUser")
    public PageImpl<EventResponseDto> getEventsForUser(@RequestHeader(name = "User-Token", required = false) String token,
                                                       @RequestParam(required = false, defaultValue = "0") Integer start,
                                                       @RequestParam(required = false, defaultValue = DEFAULT_PAGING_COUNT) Integer count,
                                                       @RequestParam(required = false) @DateTimeFormat(pattern="yyyy-MM-dd") Date date){
        return eventService.getAllEventsForUser(start, count, date);
    }

    @GetMapping("/listHistoryForUser")
    public PageImpl<EventResponseDto> getEventsHistoryForUser(@RequestHeader(name = "User-Token", required = false) String token,
            @RequestParam(required = false, defaultValue = "0") Integer start,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGING_COUNT) Integer count,
            @RequestParam(name = "previous_events", required = false) Boolean previousEvents, @RequestParam Long userId)
    {
        return eventService.getAllEventsHistoryForUser(start, count, userId, previousEvents);
    }

    @GetMapping("/list")
    public PageImpl<EventsNewDTO> getEventsForEmployeePageable(@RequestHeader(name = "User-Token", required = false) String token,
                                                                   @RequestParam(required = false, defaultValue = "0") Integer start,
                                                                   @RequestParam(required = false, defaultValue = DEFAULT_PAGING_COUNT) Integer count,
                                                                   @RequestParam(required = false) EventStatus status,
                                                                   @RequestParam(required = false) String name,
                                                                   @RequestParam(required = false, name = "fromDate")
                                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                                   LocalDateTime fromDate,
                                                                   @RequestParam(required = false, name = "toDate")
                                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                                   LocalDateTime toDate) {
        return eventService.getEventsForEmployee(start, count, status, fromDate, toDate, name);
    }

    @GetMapping("/list/advertise")
    public PageImpl<EventResponseDto> getAdvertisedEvents(@RequestHeader(name = "User-Token", required = false) String token,
                                                      @RequestParam(required = false, defaultValue = "0") Integer start,
                                                      @RequestParam(required = false, defaultValue = DEFAULT_PAGING_COUNT) Integer count,
                                                      @RequestParam(required = false) EventStatus status,
                                                      @RequestParam(required = false) String name,
                                                      @RequestParam(required = false, name = "fromDate")
                                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
                                                      @RequestParam(required = false, name = "toDate")
                                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        return eventService.getAdvertisedEvents(start, count, status, fromDate, toDate, name);
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
                                @PathVariable Long eventId,
                                @RequestParam(required = false, defaultValue = "false") boolean force){
        eventService.deleteEvent(eventId, force);
    }

    @PostMapping("/interset/{eventId}")
    public void intersetForUser(@RequestHeader(name = "User-Token", required = false) String token,
                                @PathVariable Long eventId) throws MessagingException, IOException {
        eventService.intersetEventForUser(eventId);
    }

    @GetMapping("/all")
    public PageImpl<EventsNewDTO> getAllEventsPageable(
                                                                   @RequestParam(required = false, defaultValue = "0") Integer start,
                                                                   @RequestParam(required = false, defaultValue = DEFAULT_PAGING_COUNT) Integer count ,
                                                                   @RequestParam(required = false, name = "fromDate")
                                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                                   LocalDateTime fromDate ,
                                                                   @RequestParam(required = false ) Long orgId
                                                                   ) {
        return eventService.getAllEvents(start, count , fromDate , orgId);
    }

    @GetMapping("/getEventByName/{name}")
    public PageImpl<EventResponseDto> getEventsByName(@PathVariable("name") String name,
                                                      @RequestParam(required = false, defaultValue = "0") Integer start,
                                                      @RequestParam(required = false, defaultValue = DEFAULT_PAGING_COUNT) Integer count
    ) {
        return eventService.getEventByName(name,start,count);
    }

    @GetMapping("/advertise/all")
    public PageImpl<EventsNewDTO> getAllAdvertisedEvents(@RequestHeader(name = "User-Token", required = false) String token ,
                                                      @RequestParam(required = false, defaultValue = "0") Integer start,
                                                      @RequestParam(required = false ) Long orgId ,
                                                      @RequestParam(required = false, defaultValue = DEFAULT_PAGING_COUNT) Integer count
                                                      ){
        return eventService.getAllAdvertisedEvents(start,count,orgId);
    }

}
