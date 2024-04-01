package com.nasnav.service.impl;

import com.nasnav.commons.utils.CustomPaginationPageRequest;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.PersonalEventEntityRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.InvitePeopleDTO;
import com.nasnav.dto.PersonalEvent;
import com.nasnav.dto.PersonalEventDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.EventInviteeEntity;
import com.nasnav.persistence.PersonalEventEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.MailService;
import com.nasnav.service.PersonalEventService;
import com.nasnav.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nasnav.constatnts.EmailConstants.INVITE_MAIL;
import static com.nasnav.exceptions.ErrorCodes.PE_EVENT_$001;
import static com.nasnav.exceptions.ErrorCodes.PE_EVENT_$002;
import static com.nasnav.exceptions.ErrorCodes.U$0001;
import static com.nasnav.exceptions.ErrorCodes.U$EMP$0002;


@Service
@RequiredArgsConstructor
public class PersonalEventImpl implements PersonalEventService {
    private final PersonalEventEntityRepository personalEventRepository;
    private final SecurityService securityService;
    private final  EmployeeUserRepository employeeRepository;
    private final UserRepository userRepo;
    private final MailService mailService;


    @Override
    public PersonalEventEntity createPersonalEvent(PersonalEventDTO dto) {
        return personalEventRepository.save(personalEvent(dto));
    }

    @Override
    public void cancelPersonalEvent(Long id) {
        PersonalEventEntity personalEvent = getPersonalEventByIdAndLoggedInUser(id);
        personalEvent.setCanceled(true);
        personalEventRepository.save(personalEvent);
    }

    @Override
    public PageImpl<PersonalEvent> getAllPersonalEvents(int start , int count) {
        Pageable page = new CustomPaginationPageRequest(start, count, Sort.Direction.ASC , "startsAt" );
        return personalEventRepository.findAllBy(page);
    }

    @Override
    public PersonalEvent getPersonalEvent(Long id) {
        return personalEventRepository.findAllById(id).orElseThrow(()->new RuntimeBusinessException(HttpStatus.NOT_FOUND , PE_EVENT_$002,id));
    }
    @Override
    public void inviteToPersonalEvent(Long id, InvitePeopleDTO invite) throws MessagingException, IOException {
        PersonalEventEntity personalEvent = getPersonalEventByIdAndLoggedInUser(id);
        EventInviteeEntity eventInvitee = createEventInvitee(invite);

        personalEvent.addInvitee(eventInvitee);
        personalEventRepository.save(personalEvent);

        String orgName = securityService.getCurrentUserOrganization().getName();
        String creatorName = getCreatorName(personalEvent);
        String inviteeMail = getInviteeMail(eventInvitee , invite);

        prepareAndSendMail(orgName, inviteeMail, creatorName, personalEvent.getStartsAt(), personalEvent.getEndsAt());
    }

    @Override
    public List<Map<String,Object>> findMyAllEvents() {
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        UserEntity user = null ;
        EmployeeUserEntity employee = null;
        if (loggedInUser instanceof EmployeeUserEntity employeeUser) {
            employee = employeeUser;
        } else {
            user = (UserEntity) loggedInUser;
        }
        return personalEventRepository.findMyAllEvents(user,employee);
    }

    private EventInviteeEntity createEventInvitee(InvitePeopleDTO invite) {
        EventInviteeEntity eventInvitee = new EventInviteeEntity();
        if (invite.getUser() != null && invite.isEmployee()) {
            EmployeeUserEntity employee = getEmployeeById(invite.getUser());
            eventInvitee.setEmployee(employee);
        } else if (invite.getUser() != null && !invite.isEmployee()) {
            UserEntity user = getUserById(invite.getUser());
            eventInvitee.setUser(user);
        }else {
            eventInvitee.setExternalUser(invite.getExternalMail());
        }
        return eventInvitee;
    }

    private EmployeeUserEntity getEmployeeById(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, U$EMP$0002, id));
    }

    private UserEntity getUserById(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, U$0001, id));
    }

    private String getCreatorName(PersonalEventEntity personalEvent) {
        return personalEvent.getUser() != null ?
                personalEvent.getUser().getName() :
                personalEvent.getEmployee().getName();
    }

    private String getInviteeMail(EventInviteeEntity invite ,InvitePeopleDTO dto) {
        if (invite.getUser() != null) {
            return invite.getUser().getEmail();
        } else if ( invite.getEmployee() != null) {
            return invite.getEmployee().getEmail();
        }
        return dto.getExternalMail();
    }
    private PersonalEventEntity getPersonalEventByIdAndLoggedInUser(Long id) {
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        UserEntity user = null ;
        EmployeeUserEntity employee = null;
        if (loggedInUser instanceof EmployeeUserEntity employeeUser) {
            employee = employeeUser;
        } else {
            user = (UserEntity) loggedInUser;
        }

        return personalEventRepository.findByIdAndStartsAtAfter(id , LocalDateTime.now(),user, employee).orElseThrow(()-> new RuntimeBusinessException(HttpStatus.NOT_FOUND , PE_EVENT_$001,id));
    }
    private PersonalEventEntity personalEvent(PersonalEventDTO dto) {
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        PersonalEventEntity personalEvent = new PersonalEventEntity();

        if (loggedInUser instanceof EmployeeUserEntity user) {
            setEmployeePersonalEvent( user, personalEvent);
        } else {
            setUserPersonalEvent((UserEntity) loggedInUser, personalEvent);
        }

        setPersonalEventDetails(dto, personalEvent);
        return personalEvent;
    }

    private void setEmployeePersonalEvent(EmployeeUserEntity loggedInUser, PersonalEventEntity personalEvent) {
        personalEvent.setEmployee(loggedInUser);
    }

    private void setUserPersonalEvent(UserEntity loggedInUser, PersonalEventEntity personalEvent) {
        personalEvent.setUser(loggedInUser);
    }

    private void setPersonalEventDetails(PersonalEventDTO dto, PersonalEventEntity personalEvent) {
        personalEvent.setName(dto.name());
        personalEvent.setStartsAt(dto.startAt());
        personalEvent.setEndsAt(dto.endAt());
        personalEvent.setDescription(dto.description());
    }

    @Async
    private void prepareAndSendMail(String orgName  , String inviteeMail, String creatorName , LocalDateTime startAt , LocalDateTime endAt ) throws MessagingException, IOException {
        this.sendInviteEmail(orgName ,inviteeMail,creatorName, startAt , endAt ,"Event Invitation",INVITE_MAIL );
    }
    public void sendInviteEmail(String orgName ,String inviteeMail, String creatorName, LocalDateTime startAt , LocalDateTime endAt, String emailSubject , String mailTemplate) throws MessagingException, IOException {
        Map<String, String> parametersMap = prepareMailContent(creatorName,inviteeMail,startAt,endAt);
        this.mailService.send( orgName , inviteeMail, emailSubject, mailTemplate,parametersMap);
    }




    public Map<String, String> prepareMailContent(String creatorName,String inviteeMail ,  LocalDateTime startAt , LocalDateTime endAt ) {
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put("#inviteeMail#", inviteeMail);
        parametersMap.put("#creator#", creatorName);
        parametersMap.put("#startDate#",  startAt.toLocalDate().toString());
        parametersMap.put("#endDate#", endAt.toLocalDate().toString());
        parametersMap.put("#startime#", startAt.toLocalTime().toString());
        parametersMap.put("#endtime#", endAt.toLocalTime().toString());
        return parametersMap;
    }

}
