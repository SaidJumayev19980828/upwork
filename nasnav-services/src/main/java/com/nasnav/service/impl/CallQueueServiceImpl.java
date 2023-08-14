package com.nasnav.service.impl;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.nasnav.dao.CallQueueRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dto.request.notification.NotificationRequestDto;
import com.nasnav.dto.response.CallQueueDTO;
import com.nasnav.dto.response.CallQueueStatusDTO;
import com.nasnav.enumerations.CallQueueStatus;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.VideoChatResponse;
import com.nasnav.service.CallQueueService;
import com.nasnav.service.OrganizationService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.VideoChatService;
import com.nasnav.service.notification.NotificationService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.nasnav.commons.utils.PagingUtils.getQueryPage;
import static com.nasnav.exceptions.ErrorCodes.*;

@Service
public class CallQueueServiceImpl implements CallQueueService {
    @Autowired
    private CallQueueRepository callQueueRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private VideoChatService videoChatService;
    @Autowired
    private NotificationService notificationService;

    @Override
    public CallQueueStatusDTO enterQueue(Long orgId) {
        UserEntity userEntity = getUser();
        OrganizationEntity organizationEntity = organizationRepository.findById(orgId)
                .orElseThrow(()-> new RuntimeBusinessException(HttpStatus.NOT_FOUND,G$ORG$0001,orgId));
        CallQueueEntity entity = callQueueRepository.getByUser_IdAndStatus(userEntity.getId(), CallQueueStatus.OPEN.getValue());
        if (entity != null){
            entity.setStatus(CallQueueStatus.REJECTED.getValue());
            entity.setReason("User Overlapped the queue");
            entity.setEndsAt(LocalDateTime.now());
            callQueueRepository.save(entity);
        }

        entity = new CallQueueEntity();
        entity.setJoinsAt(LocalDateTime.now());
        entity.setUser(userEntity);
        entity.setOrganization(organizationEntity);
        entity.setStatus(CallQueueStatus.OPEN.getValue());

        entity = callQueueRepository.save(entity);

        notifyQueue(orgId);

        return getQueueStatus(orgId, entity);
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
        String userResponseSTR = userResponse.toString();
        notificationService.sendMessage(entity.getUser(), new NotificationRequestDto("call queue started", userResponseSTR));

        entity.setStatus(CallQueueStatus.LIVE.getValue());
        entity.setStartsAt(LocalDateTime.now());
        entity.setEmployee(getEmployee());
        callQueueRepository.save(entity);

        notifyQueue(entity.getOrganization().getId());

        return videoChatService.createOrJoinSession(userResponse.getSessionName(), force, entity.getOrganization().getId(), null);
    }

    @Override
    public List<CallQueueDTO> rejectCall(Long queueId) {
        CallQueueEntity entity = callQueueRepository.findById(queueId).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,G$QUEUE$0001));
        entity.setEmployee(getEmployee());
        entity.setStatus(CallQueueStatus.REJECTED.getValue());
        entity.setEndsAt(LocalDateTime.now());
        entity.setReason("Employee reject the call");
        callQueueRepository.save(entity);

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
            entity = queue.get(i);
            String response = new JSONObject()
                    .put("id",entity.getId())
                    .put("joinsAt",entity.getJoinsAt())
                    .put("position",i+1)
                    .put("total",queue.size())
                            .toString();
            notificationService.sendMessage(entity.getUser(),
                    new NotificationRequestDto("queue summary",response));
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
}
