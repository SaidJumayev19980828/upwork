package com.nasnav.service;

import com.nasnav.dao.*;
import com.nasnav.dto.request.EventForRequestDTO;
import com.nasnav.dto.response.EventInterestDTO;
import com.nasnav.dto.response.EventResponseDto;
import com.nasnav.dto.response.GeneralRepresentationDto;
import com.nasnav.enumerations.EventStatus;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.nasnav.commons.utils.PagingUtils.getQueryPage;
import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class EventServiceImpl implements EventService{
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private InfluencerRepository influencerRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private EventLogsRepository eventLogsRepository;

    @Override
    public void createEvent(EventForRequestDTO dto) {
        InfluencerEntity influencer = null;
        OrganizationEntity org = organizationRepository.findById(dto.getOrganizationId()).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND, G$ORG$0001, dto.getOrganizationId())
        );
        if(dto.getInfluencerId() != null){
            influencer = influencerRepository.findById(dto.getInfluencerId()).orElseThrow(
                    () -> new RuntimeBusinessException(NOT_FOUND, G$INFLU$0001, dto.getOrganizationId())
            );
        }
        eventRepository.save(toEntity(dto, org, influencer, null));
    }

    @Override
    public EventResponseDto getEventById(Long eventId) {
        EventEntity entity = eventRepository.findById(eventId).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND,G$EVENT$0001,eventId)
        );
        List<Long> categoryIds = entity.getProducts().stream().map(o -> o.getCategoryId()).collect(Collectors.toList());
        List<EventEntity> relatedEvents = eventRepository.getRelatedEvents(categoryIds, eventId);
        EventResponseDto dto = toDto(entity);
        dto.setRelatedEvents(relatedEvents.stream().map(this::toDto).collect(Collectors.toList()));
        return dto;
    }

    @Override
    public List<EventResponseDto> getEventsByOrgIdForUsers(Long orgID, EventStatus status) {
        OrganizationEntity org = organizationRepository.findById(orgID).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND, G$ORG$0001, orgID)
        );
        Integer statusValue = null;
        if(status != null){
            statusValue = status.getValue();
        }
        List<EventEntity> eventEntities = eventRepository.getAllEventForUser(orgID, statusValue);
        return eventEntities.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public PageImpl<EventResponseDto> getEventsForEmployee(Integer start, Integer count, EventStatus status) {
        PageRequest page = getQueryPage(start, count);
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if(loggedInUser instanceof EmployeeUserEntity){
            EmployeeUserEntity employeeUserEntity = (EmployeeUserEntity) loggedInUser;
            Integer statusValue = null;
            if(status != null){
                statusValue = status.getValue();
            }
            PageImpl<EventEntity> source = eventRepository.getAllEventForOrg(employeeUserEntity.getOrganizationId(),statusValue, page);
            List<EventResponseDto> dtos = source.getContent().stream().map(this::toDto).collect(Collectors.toList());
            return new PageImpl<>(dtos, source.getPageable(), source.getTotalElements());
        }
        return null;
    }

    @Override
    public List<EventResponseDto> getAdvertisedEvents() {
        return eventRepository.getAllByInfluencerNull().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<EventResponseDto> getAdvertisedEventsForInfluencer() {
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        InfluencerEntity influencerEntity = influencerRepository.getByUser_IdOrEmployeeUser_Id(loggedInUser.getId(),loggedInUser.getId());
        if(influencerEntity != null){
            List<OrganizationEntity> orgs = organizationRepository.findYeshteryOrganizationsFilterByCategory(influencerEntity.getCategories().stream()
                    .map(o->o.getId()).collect(Collectors.toList()));
            return eventRepository.getAllByOrganizationInAndInfluencerNull(orgs).stream().map(this::toDto).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public PageImpl<EventInterestDTO> getInterestsByEventId(Long eventId,Integer start, Integer count) {
        PageRequest page = getQueryPage(start, count);
        PageImpl<EventLogsEntity> source = eventLogsRepository.getAllByEventIdPageable(eventId, page);
        List<EventInterestDTO> dtos = source.getContent().stream().map(this::toEventInterstDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, source.getPageable(), source.getTotalElements());
    }

    @Override
    public void updateEvent(EventForRequestDTO dto, Long eventId) {
        EventEntity entity = eventRepository.findById(eventId).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND,G$EVENT$0001,eventId)
        );
        if(entity.getEndsAt().isBefore(LocalDateTime.now())){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE,EVENT$NOT$EDITABLE$0002,eventId);
        }

        entity = toEntity(dto, entity.getOrganization(), entity.getInfluencer(), entity.getId());
        eventRepository.save(entity);
    }

    @Override
    public void deleteEvent(Long eventId) {
        EventEntity entity = eventRepository.findById(eventId).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND,G$EVENT$0001,eventId)
        );

        eventRepository.delete(entity);
    }

    @Override
    public void intersetEventForUser(Long eventId) {
        EventEntity event = eventRepository.findById(eventId).
                orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND,G$EVENT$0001,eventId));
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if(eventLogsRepository.existsByEvent_IdAndUser_IdOrEmployee_Id(eventId,loggedInUser.getId(),loggedInUser.getId())){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE,EVENT$HAS$INTEREST$0006,eventId);
        }
        EventLogsEntity entity = new EventLogsEntity();
        if(loggedInUser instanceof UserEntity){
            entity.setUser((UserEntity) loggedInUser);
        }
        else {
            entity.setEmployee((EmployeeUserEntity) loggedInUser);
        }
        entity.setEvent(event);
        entity.setInterestedAt(LocalDateTime.now());
        entity.setCreatedAt(LocalDateTime.now());
        eventLogsRepository.save(entity);
    }

    private EventEntity toEntity(EventForRequestDTO dto, OrganizationEntity org, InfluencerEntity influencer, Long id){
        EventEntity entity = new EventEntity();
        List<ProductEntity> products = new ArrayList<>();
        entity.setId(id);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setStartsAt(dto.getStartsAt());
        entity.setEndsAt(dto.getEndsAt());
        entity.setOrganization(org);
        entity.setInfluencer(influencer);
        entity.setProducts(products);
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setVisible(dto.getVisible());
        entity.setAttachments(dto.getAttachments());

        dto.getProductsIds().forEach(i -> {
            Optional<ProductEntity> product = productRepository.findByIdAndOrganizationId(i, org.getId());
            if(product.isPresent()){
                products.add(product.get());
            }
        });
        dto.getAttachments().forEach(o -> {
            o.setEvent(entity);
        });
        if(id == null){
            entity.setStatus(EventStatus.PENDING.getValue());
        }
        else {
            entity.setStatus(dto.getStatus().getValue());
        }
        return entity;
    }

    private EventResponseDto toDto(EventEntity entity){
        List<EventLogsEntity> logs = eventLogsRepository.getAllByEvent_Id(entity.getId());
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
        dto.setStatus(EventStatus.getEnumByValue(entity.getStatus()));
        dto.setProducts(entity.getProducts());
        return dto;
    }

    EventInterestDTO toEventInterstDto(EventLogsEntity entity){
        EventInterestDTO dto = new EventInterestDTO();
        dto.setId(entity.getId());
        dto.setDate(entity.getInterestedAt());
        if(entity.getUser() != null){
            dto.setEmail(entity.getUser().getEmail());
            dto.setName(entity.getUser().getName());
            dto.setImage(entity.getUser().getImage());
            dto.setUserId(entity.getUser().getId());
            dto.setUserType("Customer");
        }
        else {
            dto.setEmail(entity.getEmployee().getEmail());
            dto.setName(entity.getEmployee().getName());
            dto.setImage(entity.getEmployee().getImage());
            dto.setUserId(entity.getEmployee().getId());
            dto.setUserType("Employee");
        }
        return dto;
    }
}
