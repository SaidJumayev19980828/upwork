package com.nasnav.service;

import com.nasnav.dao.*;
import com.nasnav.dto.CategoryDTO;
import com.nasnav.dto.EventRequestsDTO;
import com.nasnav.dto.InfluencerDTO;
import com.nasnav.dto.request.EventOrganiseRequestDTO;
import com.nasnav.dto.response.EventInterestDTO;
import com.nasnav.dto.response.EventResponseDto;
import com.nasnav.dto.response.GeneralRepresentationDto;
import com.nasnav.enumerations.EventRequestStatus;
import com.nasnav.enumerations.EventStatus;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.nasnav.commons.utils.PagingUtils.getQueryPage;
import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.*;

@Service
public class InfluencerServiceImpl implements InfluencerService {
    @Autowired
    private InfluencerRepository influencerRepository;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EventRequestsRepository eventRequestsRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private CategoriesRepository categoriesRepository;
    @Autowired
    private EventLogsRepository eventLogsRepository;

    @Override
    public InfluencerDTO getInfluencerById(Long id) {
        InfluencerEntity entity = influencerRepository.findById(id)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND,G$INFLU$0001,id));

        return toInfluencerDto(entity);
    }

    @Override
    public PageImpl<InfluencerDTO> getAllInfluencers(Integer start, Integer count, Boolean status) {
        PageRequest page = getQueryPage(start, count);
        PageImpl<InfluencerEntity> source = influencerRepository.findAllPageable(status,page);
        List<InfluencerDTO> resultList = source.getContent().stream().map(this::toInfluencerDto).collect(Collectors.toList());
        return new PageImpl<>(resultList, source.getPageable(), source.getTotalElements());
    }

    @Override
    public void becomeInfluencerRequest(List<Long> categoryIds) {
        List<CategoriesEntity> categories = categoriesRepository.findAllByIdIn(categoryIds);
        InfluencerEntity entity = new InfluencerEntity();
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if(influencerRepository.existsByUser_IdOrEmployeeUser_Id(loggedInUser.getId(),loggedInUser.getId())){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE,G$INFLU$0002,loggedInUser.getId());
        }
        if(loggedInUser instanceof UserEntity){
            entity.setUser((UserEntity) loggedInUser);
        }
        else if(loggedInUser instanceof EmployeeUserEntity){
            entity.setEmployeeUser((EmployeeUserEntity) loggedInUser);
        }
        entity.setApproved(false);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCategories(categories);
        influencerRepository.save(entity);
    }

    @Override
    public void becomeInfluencerResponse(Long influencerId, boolean action) {
        InfluencerEntity influencer = influencerRepository.findById(influencerId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND,G$INFLU$0001,influencerId));
        influencer.setApproved(action);
        influencerRepository.save(influencer);
    }

    @Override
    public void requestEventHosting(EventOrganiseRequestDTO dto) {
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        InfluencerEntity influencer = influencerRepository.getByUser_IdOrEmployeeUser_Id(loggedInUser.getId(), loggedInUser.getId());
        if(influencer == null){
            throw new RuntimeBusinessException(NOT_FOUND,G$INFLU$0001,loggedInUser.getId());
        }
        if(!influencer.getApproved())
            throw new RuntimeBusinessException(NOT_ACCEPTABLE,G$INFLU$0003,influencer.getId());
        EventEntity event = eventRepository.findById(dto.getId()).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND,G$EVENT$0001,dto.getId()));

        if(event.getInfluencer() != null){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE,EVENT$HAS$HOST$0005,event.getId());
        }
        if(eventRequestsRepository.existsByInfluencerAndEvent(influencer, event))
            throw new RuntimeBusinessException(NOT_ACCEPTABLE,EVENT$REQUEST$0004,event.getId());

        EventRequestsEntity eventRequest = new EventRequestsEntity();
        eventRequest.setEvent(event);
        eventRequest.setInfluencer(influencer);
        eventRequest.setStartsAt(dto.getStartsAt());
        eventRequest.setEndsAt(dto.getEndsAt());
        eventRequest.setCreatedAt(LocalDateTime.now());
        eventRequest.setStatus(EventRequestStatus.PENDING.getValue());
        eventRequestsRepository.save(eventRequest);
    }

    @Override
    public EventRequestsDTO getEventRequestById(Long requestId) {
        EventRequestsEntity entity = eventRequestsRepository.findById(requestId)
                .orElseThrow(()-> new RuntimeBusinessException(NOT_FOUND,EVENT$REQUEST$0004,requestId));
        return eventRequestToDto(entity);
    }

    @Override
    public void approveOrCancelEventHostingRequest(Long id, boolean action) {
        EventRequestsEntity entity = eventRequestsRepository.findById(id).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND,G$EVENT$0001,id));
        EventEntity eventEntity = eventRepository.findById(entity.getEvent().getId())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND,G$EVENT$0001,entity.getEvent().getId()));

        if(entity.getStatus() == EventRequestStatus.PENDING.getValue()){
            if(action && onlyOneRequestApproved(eventEntity.getId())){
                entity.setStatus(EventRequestStatus.APPROVED.getValue());
                eventEntity.setInfluencer(entity.getInfluencer());
                eventEntity.setStartsAt(entity.getStartsAt());
                eventEntity.setEndsAt(entity.getEndsAt());
                eventRepository.save(eventEntity);
            }
            else {
                entity.setStatus(EventRequestStatus.REJECTED.getValue());
            }
            eventRequestsRepository.save(entity);
        }
        else {
            throw new RuntimeBusinessException(NOT_MODIFIED,EVENT$MODIFICATION$0003,id);
        }
    }

    @Transactional
    @Override
    public void rejectTheRestIfEventHostingRequestApproved(Long requestId) {
        EventRequestsEntity entity = eventRequestsRepository.findById(requestId).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND,G$EVENT$0001,requestId));
        EventEntity eventEntity = eventRepository.findById(entity.getEvent().getId())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND,G$EVENT$0001,entity.getEvent().getId()));

        entity.setStatus(EventRequestStatus.APPROVED.getValue());
        eventEntity.setInfluencer(entity.getInfluencer());
        eventEntity.setStartsAt(entity.getStartsAt());
        eventEntity.setEndsAt(entity.getEndsAt());
        eventRepository.save(eventEntity);
        eventRequestsRepository.save(entity);
        List<EventRequestsEntity> list = eventRequestsRepository.getAllByEvent_Organization_Id(eventEntity.getOrganization().getId());
        for (EventRequestsEntity requestsEntity : list){
            requestsEntity.setStatus(EventRequestStatus.REJECTED.getValue());
        }
        eventRequestsRepository.saveAll(list);
    }

    private boolean onlyOneRequestApproved(Long eventId) {
        return !eventRequestsRepository.existsByEvent_IdAndStatusEquals(eventId,1);
    }

    @Override
    public void deleteEventHostingRequest(Long id) {
        EventRequestsEntity entity = eventRequestsRepository.findById(id).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND,G$EVENT$0001,id));

        if(entity.getStatus() != EventRequestStatus.APPROVED.getValue()){
            eventRequestsRepository.delete(entity);
        }
        else {
            throw new RuntimeBusinessException(NOT_MODIFIED,EVENT$MODIFICATION$0003,id);
        }
    }

    @Override
    public List<EventResponseDto> getMyEvents() {
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        InfluencerEntity influencerEntity = influencerRepository.getByUser_IdOrEmployeeUser_Id(loggedInUser.getId(),loggedInUser.getId());
        if(influencerEntity == null)
            throw new RuntimeBusinessException(NOT_FOUND,G$INFLU$0001,loggedInUser.getId());
        if(!influencerEntity.getApproved())
            throw new RuntimeBusinessException(NOT_ACCEPTABLE,G$INFLU$0003,influencerEntity.getId());

        return eventRepository.getAllByInfluencer(influencerEntity).stream().map(this::eventToDto).collect(Collectors.toList());
    }

    @Override
    public PageImpl<EventRequestsDTO> getMyEventRequests(Integer start, Integer count, EventRequestStatus status){
        PageRequest page = getQueryPage(start, count);
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        InfluencerEntity influencerEntity = influencerRepository.getByUser_IdOrEmployeeUser_Id(loggedInUser.getId(),loggedInUser.getId());
        if(influencerEntity == null)
            throw new RuntimeBusinessException(NOT_FOUND,G$INFLU$0001,loggedInUser.getId());
        if(!influencerEntity.getApproved())
            throw new RuntimeBusinessException(NOT_ACCEPTABLE,G$INFLU$0003,influencerEntity.getId());

        Integer statusValue = null;
        if(status != null){
            statusValue = status.getValue();
        }
        PageImpl<EventRequestsEntity> source = eventRequestsRepository.getAllByInfluencerIdPageable(influencerEntity.getId(), statusValue, page);
        List<EventRequestsDTO> dtos = source.getContent().stream().map(this::eventRequestToDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, source.getPageable(), source.getTotalElements());
    }

    @Override
    public PageImpl<EventRequestsDTO> getEventsRequestByOrgForEmployee(Integer start, Integer count, EventRequestStatus status) {
        PageRequest page = getQueryPage(start, count);
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if(loggedInUser instanceof EmployeeUserEntity){
            Long orgId =  loggedInUser.getOrganizationId();
            organizationRepository.findById(orgId).orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND,G$ORG$0001,orgId));

            Integer statusValue = null;
            if(status != null){
                statusValue = status.getValue();
            }
            PageImpl<EventRequestsEntity> source = eventRequestsRepository.getAllByOrgIdPageable(orgId, statusValue, page);
            List<EventRequestsDTO> dtos = source.getContent().stream().map(this::eventRequestToDto).collect(Collectors.toList());
            return new PageImpl<>(dtos);
        }
        return null;
    }

    @Override
    public void joinEvent() {
    }

    private EventRequestsDTO eventRequestToDto(EventRequestsEntity entity){
        EventRequestsDTO dto = new EventRequestsDTO();
        dto.setId(entity.getId());
        dto.setStartsAt(entity.getStartsAt());
        dto.setEndsAt(entity.getEndsAt());
        dto.setEvent(eventToDto(entity.getEvent()));
        dto.setStatus(EventRequestStatus.getEnumByValue(entity.getStatus()));
        return dto;
    }

    private EventResponseDto eventToDto(EventEntity entity){
        EventResponseDto dto = new EventResponseDto();
        dto.setId(entity.getId());
        dto.setStartsAt(entity.getStartsAt());
        dto.setEndsAt(entity.getEndsAt());
        dto.setOrganization(new GeneralRepresentationDto(entity.getOrganization().getId(),entity.getOrganization().getName()));
        if(entity.getInfluencer() != null){
            if(entity.getInfluencer().getUser() != null){
                dto.setInfluencer(new GeneralRepresentationDto(entity.getInfluencer().getId(),entity.getInfluencer().getUser().getName()));
            }
            else {
                dto.setInfluencer(new GeneralRepresentationDto(entity.getInfluencer().getId(),entity.getInfluencer().getEmployeeUser().getName()));
            }
        }
        dto.setVisible(entity.getVisible());
        dto.setAttachments(entity.getAttachments());
        dto.setDescription(entity.getDescription());
        dto.setName(entity.getName());
        dto.setProducts(entity.getProducts());
        dto.setStatus(EventStatus.getEnumByValue(entity.getStatus()));
        return dto;
    }

    private InfluencerDTO toInfluencerDto(InfluencerEntity entity) {
        InfluencerDTO dto = new InfluencerDTO();
        dto.setId(entity.getId());
        if(entity.getEmployeeUser() != null){
            dto.setName(entity.getEmployeeUser().getName());
            dto.setEmail(entity.getEmployeeUser().getEmail());
            dto.setPhoneNumber(entity.getEmployeeUser().getPhoneNumber());
            dto.setImage(entity.getEmployeeUser().getImage());
        }
        else {
            dto.setName(entity.getUser().getName());
            dto.setEmail(entity.getUser().getEmail());
            dto.setImage(entity.getUser().getImage());
        }
        dto.setCategories(entity.getCategories().stream().map(this::toCategoryDTO).collect(Collectors.toList()));
        dto.setHostedEvents(eventRepository.getAllByInfluencer(entity).stream().map(this::eventToDto).collect(Collectors.toList()));
        dto.setInterests(eventLogsRepository.countByEvent_Influencer_Id(entity.getId()));
        dto.setAttends(eventLogsRepository.countByEvent_Influencer_IdAndAttendAtNotNull(entity.getId()));
        return dto;
    }

    CategoryDTO toCategoryDTO(CategoriesEntity entity){
        CategoryDTO dto = new CategoryDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setLogo(entity.getLogo());
        dto.setParentId(entity.getParentId());
        dto.setCover(entity.getCover());
        dto.setCoverSmall(entity.getCoverSmall());
        return dto;
    }

    EventInterestDTO toEventInterestDto(EventLogsEntity entity){
        EventInterestDTO dto = new EventInterestDTO();
        dto.setId(entity.getId());
        dto.setDate(entity.getInterestedAt());
        if(entity.getUser() != null){
            dto.setName(entity.getUser().getName());
            dto.setEmail(entity.getUser().getEmail());
        }
        else if(entity.getEmployee() != null){
            dto.setName(entity.getEmployee().getName());
            dto.setEmail(entity.getEmployee().getEmail());
        }
        return dto;
    }

}
