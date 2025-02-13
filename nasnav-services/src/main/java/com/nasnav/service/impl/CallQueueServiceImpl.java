package com.nasnav.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.CallQueueRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dto.request.notification.PushMessageDTO;
import com.nasnav.dto.response.CallQueueDTO;
import com.nasnav.dto.response.CallQueueStatusDTO;
import com.nasnav.enumerations.CallQueueStatus;
import com.nasnav.enumerations.NotificationType;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.VideoChatResponse;
import com.nasnav.service.CallQueueService;
import com.nasnav.service.MailService;
import com.nasnav.service.OrganizationService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.VideoChatService;
import com.nasnav.service.notification.NotificationService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.nasnav.commons.utils.PagingUtils.getQueryPage;
import static com.nasnav.constatnts.EmailConstants.ENTER_QUEUE_CALL_CUSTOMER_TEMPLATE_PATH;
import static com.nasnav.constatnts.EmailConstants.ENTER_QUEUE_CALL_EMPLOYEE_TEMPLATE_PATH;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.lang.String.format;

@Service
public class CallQueueServiceImpl implements CallQueueService {
    @Autowired
    private CallQueueRepository callQueueRepository;
    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ShopsRepository shopsRepository;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private VideoChatService videoChatService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private  MailService mailService;


    @Override
    @Transactional
    public CallQueueStatusDTO enterQueue(Long orgId, Long shopId) throws MessagingException, IOException {
        UserEntity userEntity = getUser();
        OrganizationEntity organizationEntity = getOrganizationById(orgId);
        ShopsEntity shop = Optional.ofNullable(shopId)
                .map(this :: getShopById)
                .orElse(null);

        rejectOverlappingQueue(userEntity);
        Set<String> notificationTokens = securityService.getValidNotificationTokens(userEntity);
        if (notificationTokens.isEmpty()) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, ErrorCodes.NOTIF$0003, userEntity.getId());
        }
        CallQueueEntity entity = createNewQueueEntry(userEntity, organizationEntity, shop);
        notifyQueue(orgId);
        String response = createQueueResponseJson(userEntity, entity);
        notifyOrganizationEmployees(orgId, response);
        sendMails(userEntity,organizationEntity);
        return getQueueStatus(orgId, entity);
    }

    private OrganizationEntity getOrganizationById(Long orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, G$ORG$0001, orgId));
    }

    private ShopsEntity getShopById(Long shopId) {
        return shopsRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, S$0002, shopId));
    }

    private void rejectOverlappingQueue(UserEntity userEntity) {
        CallQueueEntity entity = callQueueRepository.getByUser_IdAndStatus(userEntity.getId(), CallQueueStatus.OPEN.getValue());
        if (entity != null) {
            entity.setStatus(CallQueueStatus.REJECTED.getValue());
            entity.setReason("User Overlapped the queue");
            entity.setEndsAt(LocalDateTime.now());
            callQueueRepository.save(entity);
        }
    }

    private CallQueueEntity createNewQueueEntry(UserEntity userEntity, OrganizationEntity organizationEntity, ShopsEntity shop) {
        CallQueueEntity entity = new CallQueueEntity();
        entity.setJoinsAt(LocalDateTime.now());
        entity.setUser(userEntity);
        entity.setOrganization(organizationEntity);
        if (shop != null) {
            entity.setShop(shop);
        }
        entity.setStatus(CallQueueStatus.OPEN.getValue());
        return callQueueRepository.save(entity);
    }

    private String createQueueResponseJson(UserEntity userEntity, CallQueueEntity entity) {
        return new JSONObject()
                .put("userName", userEntity.getName())
                .put("queueId", entity.getId())
                .put("joinsAt", entity.getJoinsAt())
                .toString();
    }

    private void notifyOrganizationEmployees(Long orgId, String response) {
        notificationService.sendMessageToOrganizationEmplyees(
                orgId,
                new PushMessageDTO<>("Customer Joining the Queue", response, NotificationType.ORGANIZATION_QUEUE_UPDATES)
        );
    }


    @Override
    public void quitQueue() {
        CallQueueEntity entity = callQueueRepository.getByUser_IdAndStatus(getUser().getId(),CallQueueStatus.OPEN.getValue());
        if(entity == null) {
            throw new RuntimeBusinessException(HttpStatus.NOT_FOUND,G$QUEUE$0001);
        }
        entity.setStatus(CallQueueStatus.CANCELLED.getValue());
        entity.setReason("User kick his-self out");
        entity.setEndsAt(LocalDateTime.now());
        callQueueRepository.save(entity);

        notifyQueue(entity.getOrganization().getId());
    }

    @Override
    @Transactional
    public VideoChatResponse acceptCall(Long queueId, Boolean force) {
        CallQueueEntity entity = callQueueRepository.findById(queueId).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,G$QUEUE$0001));

        VideoChatResponse userResponse = videoChatService.createOrJoinSessionForUser(null, force, entity.getOrganization().getId(), null, entity.getUser());

        Long shopId = Optional.ofNullable(entity.getShop())
                .map(ShopsEntity::getId)
                .orElse(null);
        String employeeName = getEmployee().getName();
        String employeeImage = getEmployee().getImage();
        String employeeEmail = getEmployee().getEmail();
        String employeeRole = getEmployee().getRoles().stream().map(Role::getName)
                .collect(Collectors.joining(", "));
        String notificationTitle= "Employee Accept the Call";

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode notificationUserContent = objectMapper.createObjectNode()
                .put("sessionToken",userResponse.getSessionToken())
                .put("sessionName",userResponse.getSessionName())
                .put("employeeName",employeeName)
                .put("employeeImage",employeeImage)
                .put("employeeEmail",employeeEmail)
                .put("employeeRole",employeeRole)
                .put("shopId",shopId);


        notificationService.sendMessage(entity.getUser(), new PushMessageDTO<>(notificationTitle, notificationUserContent.toString(),NotificationType.START_CALL));

        entity.setStatus(CallQueueStatus.LIVE.getValue());
        entity.setStartsAt(LocalDateTime.now());
        entity.setEmployee(getEmployee());
        callQueueRepository.saveAndFlush(entity);

        JsonNode notificationContent = objectMapper.createObjectNode()
                .put("sessionToken",userResponse.getSessionToken())
                .put("sessionName",userResponse.getSessionName())
                .put("employeeName",employeeName)
                .put("employeeImage",employeeImage)
                .put("employeeRole",employeeRole)
                .put("shopId",shopId);
        notificationService.sendMessageToOrganizationEmplyees(entity.getOrganization().getId(), new PushMessageDTO<>(notificationTitle,notificationContent.toString(), NotificationType.START_CALL));

        notifyQueue(entity.getOrganization().getId());

        return videoChatService.createOrJoinSession(userResponse.getSessionName(), force, entity.getOrganization().getId(), null);
    }

    @Override
    public List<CallQueueDTO> rejectCall(Long queueId , String reason) {
        CallQueueEntity entity = callQueueRepository.findById(queueId).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,G$QUEUE$0001));
        entity.setEmployee(getEmployee());
        entity.setStatus(CallQueueStatus.REJECTED.getValue());
        entity.setEndsAt(LocalDateTime.now());
        entity.setReason(reason);
        callQueueRepository.save(entity);
        String response = new JSONObject()
                .put("userName",entity.getUser().getName())
                .put("queueId",entity.getId())
                .put("rejectedBy",entity.getEmployee().getName())
                .put("action","The Agent Reject Your Call")
                .put("rejectionReason",reason)
                .put("EndsAt",entity.getEndsAt())
                .toString();
        notificationService.sendMessage(entity.getUser(), new PushMessageDTO<>("Employee Reject the Call", response,NotificationType.REJECT_CALL));

        notifyQueue(entity.getOrganization().getId());

        return getQueueForEmployee();
    }

    @Override
    public List<CallQueueDTO> getQueueForEmployee() {
        EmployeeUserEntity employeeUserEntity = getEmployee();
        return getQueue(employeeUserEntity.getOrganizationId());
    }

    @Override
    public CallQueueStatusDTO getQueueStatusForUser() {
        CallQueueEntity entity = callQueueRepository.getByUser_IdAndStatus(getUser().getId(), CallQueueStatus.OPEN.getValue());
        return getQueueStatus(entity.getOrganization().getId(), entity);
    }

    @Override
    public PageImpl<CallQueueDTO> getCallLogs(Integer start, Integer count, CallQueueStatus status) {
        PageRequest page = getQueryPage(start, count);
        PageImpl<CallQueueEntity> source = callQueueRepository.getLogs(status == null ? null : status.getValue(), page);
        List<CallQueueDTO> dtos = source.getContent().stream().map(this::toDto).collect(Collectors.toList());

        return new PageImpl<>(dtos, source.getPageable(), source.getTotalElements());
    }

    private void notifyQueue(Long orgId) {
        List<CallQueueEntity> queue = callQueueRepository.getAllByOrganization_IdAndStatus(orgId, CallQueueStatus.OPEN.getValue());
        queue = queue.stream().sorted(Comparator.comparing(CallQueueEntity::getJoinsAt)).collect(Collectors.toList());
        CallQueueEntity entity;
        for(int i = 0; i < queue.size(); i++) {
        try {
            entity = queue.get(i);
            String response = new JSONObject()
                    .put("id",entity.getId())
                    .put("joinsAt",entity.getJoinsAt())
                    .put("position",i+1)
                    .put("message",
                            "We are Sorry for let you wait,our Agent Will be with You As soon as possible")
                    .put("total",queue.size())
                    .toString();
            notificationService.sendMessage(entity.getUser(),
                    new PushMessageDTO<>("Joining the Queue",response,NotificationType.USER_QUEUE_UPDATES));
        }catch (RuntimeBusinessException e){
            continue; // Skip to the next iteration
        }
        }
    }

    private CallQueueStatusDTO getQueueStatus(Long orgId, CallQueueEntity queueEntity) {
        List<CallQueueEntity> queue = callQueueRepository.getAllByOrganization_IdAndStatus(orgId, CallQueueStatus.OPEN.getValue());
        Integer index = queue.stream().sorted(Comparator.comparing(CallQueueEntity::getJoinsAt)).collect(Collectors.toList()).indexOf(queueEntity);
        return new CallQueueStatusDTO(queueEntity.getId(), queueEntity.getJoinsAt(),++index, queue.size());
    }

    private List<CallQueueDTO> getQueue(Long orgId) {
        List<CallQueueEntity> queue = callQueueRepository.getAllByOrganization_IdAndStatus(orgId, CallQueueStatus.OPEN.getValue());
        List<CallQueueDTO> dtos = queue.stream().sorted(Comparator.comparing(CallQueueEntity::getJoinsAt)).collect(Collectors.toList())
                .stream().map(this::toDto).collect(Collectors.toList());

        for(int i = 0; i < dtos.size(); i++){
            dtos.get(i).setPosition(i+1);
            dtos.get(i).setTotal(dtos.size());
        }
        return dtos;
    }

    private CallQueueDTO toDto(CallQueueEntity entity) {
        CallQueueDTO dto = new CallQueueDTO();
        dto.setId(entity.getId());
        dto.setJoinsAt(entity.getJoinsAt());
        dto.setStartsAt(entity.getStartsAt());
        dto.setEndsAt(entity.getEndsAt());
        dto.setShop(entity.getShop());
        dto.setUser(entity.getUser().getRepresentation());
        dto.setOrganization(organizationService.getOrganizationById(entity.getOrganization().getId(),0));
        dto.setStatus(CallQueueStatus.getEnumByValue(entity.getStatus()));
        dto.setReason(entity.getReason());
        if(entity.getEmployee() != null){
            dto.setEmployee(entity.getEmployee().getRepresentation());
        }
        return dto;
    }

    private UserEntity getUser(){
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if(loggedInUser instanceof EmployeeUserEntity){
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE,E$USR$0001);
        }
        return (UserEntity) loggedInUser;
    }

    private EmployeeUserEntity getEmployee() {
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if(loggedInUser instanceof UserEntity){
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE,G$USR$0001);
        }
        return (EmployeeUserEntity) loggedInUser;
    }

    @Async
    private void sendMails(UserEntity user, OrganizationEntity organization) throws MessagingException, IOException {
        String organizationMail =  organizationService.getOrganizationEmail(organization.getId());
        String organizationMailSubject =format("Urgent: Client %s Video Call Request - Immediate Action Required", user.getName());
        Map<String, String> parametersMap = prepareMailContent(organization.getName(), user.getName());
        executeMail(organization.getName(),user.getEmail(),"Assistance in Progress",ENTER_QUEUE_CALL_CUSTOMER_TEMPLATE_PATH,parametersMap);
        executeMail(organization.getName(),organizationMail,organizationMailSubject,ENTER_QUEUE_CALL_EMPLOYEE_TEMPLATE_PATH,parametersMap);
    }

    public Map<String, String> prepareMailContent(String orgName, String customerName) {
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put("#Client#", customerName);
        parametersMap.put("#Organization#", orgName);
        return parametersMap;
    }

    private void executeMail(String orgName , String sendTo , String emailSubject , String mailTemplate , Map<String,String> parametersMap) throws MessagingException, IOException {
        mailService.send(orgName, sendTo, emailSubject, mailTemplate,parametersMap);

    }

}
