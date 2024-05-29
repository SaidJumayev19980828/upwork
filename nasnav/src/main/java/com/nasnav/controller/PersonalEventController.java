package com.nasnav.controller;

import com.nasnav.dto.InvitePeopleDTO;
import com.nasnav.dto.PersonalEvent;
import com.nasnav.dto.PersonalEventDTO;
import com.nasnav.persistence.PersonalEventEntity;
import com.nasnav.service.PersonalEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static com.nasnav.constatnts.DefaultValueStrings.DEFAULT_PAGING_COUNT;
import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;

@RestController
@RequestMapping(value = "party-room")
@RequiredArgsConstructor
@Validated
public class PersonalEventController {
    private final PersonalEventService personalEventService;


    /**
     *
     * @param userToken header that used to add the token input at Swagger ui
     * @param event PersonalEventDTO object that will be converted to PersonalEventEntity object and saved in
     *              database.
     *              The object will be converted from PersonalEventDTO to PersonalEventEntity using the
     *              personalEventService.createPersonalEvent method.
     *
     *
     * @return  The method will return the PersonalEventEntity object that was saved in the database.
     *                   The ResponseEntity will return the status code 201 and the PersonalEventEntity object that
     *                   was saved in the database.
     */
    @PostMapping
    public ResponseEntity<PersonalEventEntity> createPersonalEvent(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
                                                                  @Valid @RequestBody PersonalEventDTO event
                                                   ) {
       return  ResponseEntity.status(HttpStatus.CREATED).body(personalEventService.createPersonalEvent(event));
    }

    @PutMapping(value = "{eventId}")
    public ResponseEntity<PersonalEventEntity> updatePersonalEvent(
            @PathVariable long eventId,
            @Valid @RequestBody PersonalEventDTO event
    ) {
        return  ResponseEntity.status(HttpStatus.CREATED).body(personalEventService.updatePersonalEvent(eventId,event));
    }
    /**
     *
     * @param userToken header that used to add the token input at Swagger ui
     * @param eventId  the id of the event that will be Considered as a canceled event.
     *
     * @description The method will call the personalEventService.cancelPersonalEvent method to cancel the event
     *              from the database.
     */

    @DeleteMapping(value = "{eventId}")
    public void deletePersonalEvent(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
                                    @PathVariable long eventId) {
        personalEventService.cancelPersonalEvent(eventId);
    }

    /**
     * Get all personal events from the database.
     *
     * @param userToken header that used to add the token input at Swagger ui
     * @param start     the start index of the request count. The default value is 0.
     * @param count      the number of events that will be returned in the response. The default value is 10.
     *
     * @return  The method will return a PageImpl object that contains the list of personal events from the database.
     *                   The PageImpl object will contain the list of personal events from the database.
     */
    @GetMapping(value = "all")
    public PageImpl<PersonalEvent> getAllPersonalEvents(
            @RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
            @RequestParam(required = false, defaultValue = "0") Integer start,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGING_COUNT) Integer count

    ) {
        return personalEventService.getAllPersonalEvents(start, count);
    }

    /**
     * Get a personal event by its id.
     *
     * @param userToken header that used to add the token input at Swagger ui
     * @param eventId the id of the personal event that will be returned.
     * @return the personal event with the given id.
     *
     * @description The method will call the personalEventService.getPersonalEvent method to get the personal event
     *              from the database.
     *              The method will return the personal event with the given id.
     *              The method will return the personal event with the given id.
     */

    @GetMapping(value = "{eventId}")
    public PersonalEvent getPersonalEvent(
            @RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
            @PathVariable long eventId) {
        return personalEventService.getPersonalEvent(eventId);
    }

    /**
     * Invite people to a personal event.
     *
     * @param userToken header that used to add the token input at Swagger ui
     * @param eventId the id of the personal event that will be Considered as a canceled event.
     * @param invite the list of emails that will be invited to the personal event.
     *
     * @description The method will call the personalEventService.inviteToPersonalEvent method to invite the
     *              people to the personal event.
     */
    @PostMapping(value = "invite/{eventId}")
    public void inviteToPersonalEvent(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
                                    @PathVariable long eventId,
                                    @RequestBody InvitePeopleDTO invite) throws MessagingException, IOException {
        personalEventService.inviteToPersonalEvent(eventId, invite);
    }

    /**
     * Get all personal events for the current user.
     * @param userToken header that used to add the token input at Swagger ui
     * @return List<Map<String,Object>> the list of personal events for the current user.
     *
     * @description The method will call the personalEventService.findMyAllEvents method to get all personal
     *              events for the current user.
     *              The method will return a list of personal events for the current user.
     *              And all event that user was invited to also.
     */
    @GetMapping(value = "mine")
    public Set<Map<String,Object>> getMyPersonalEvents(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken) {
        return personalEventService.findMyAllEvents();
    }

}
