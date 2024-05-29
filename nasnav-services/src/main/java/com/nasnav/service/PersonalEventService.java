package com.nasnav.service;

import com.nasnav.dto.InvitePeopleDTO;
import com.nasnav.dto.PersonalEvent;
import com.nasnav.dto.PersonalEventDTO;
import com.nasnav.persistence.PersonalEventEntity;
import org.springframework.data.domain.PageImpl;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public interface PersonalEventService {

    /**
     * Create Personal Event.
     *
     * @param dto PersonalEventDTO Record that will hold the Event data that will be saved.
     * @return PersonalEventEntity object that will represent the values at DB.
     */
    PersonalEventEntity createPersonalEvent(PersonalEventDTO dto);


    PersonalEventEntity updatePersonalEvent(long eventId,PersonalEventDTO dto);

    /**
     * Cancel Personal Event.
     *
     * @param id PersonalEvent id that will be Considered as a canceled event.
     */
    void cancelPersonalEvent(Long id);


    /**
     * Get all personal events from the database.
     *
     * @param start the start index of the request count. The default value is 0.
     * @param count the number of items to be returned. The default value is 10.
     * @return The method will return a PageImpl object that contains the list of personal events from the database.
     */
    PageImpl<PersonalEvent> getAllPersonalEvents(int start , int count);

    /**
     * Get a personal event by its id.
     *
     * @param id the id of the personal event that will be returned.
     * @return the personal event with the given id.
     */
    PersonalEvent getPersonalEvent(Long id);

    /**
     * Invite people to a personal event.
     *
     * @param id the id of the personal event that will be Considered as a canceled event.
     * @param invite the Object that will hold user id if the invitee is user or employee or email if the invitee is external user that will be invited to the personal event.
     * @throws MessagingException the exception that will be thrown if there is a problem with the email.
     * @throws IOException the exception that will be thrown if there is a problem with the file.
     */
    void inviteToPersonalEvent (Long id , InvitePeopleDTO invite) throws MessagingException, IOException;


    /**
     * Get all personal events for the current user.
     * @return List<Map<String,Object>> the list of personal events for the current user.
     *
     * @description The method will call the personalEventService.findMyAllEvents method to get all personal
     *              events for the current user.
     *              The method will return a list of personal events for the current user.
     *              And all event that user was invited to also.
     */

    Set<Map<String,Object>> findMyAllEvents();


}
