package com.nasnav.service.impl;

import com.nasnav.dao.*;
import com.nasnav.dto.EventProjection;
import com.nasnav.dto.ProductDetailsDTO;
import com.nasnav.dto.ProductFetchDTO;
import com.nasnav.dto.request.EventForRequestDTO;
import com.nasnav.dto.response.EventInterestDTO;
import com.nasnav.dto.response.EventResponseDto;
import com.nasnav.enumerations.EventStatus;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.service.*;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.nasnav.commons.utils.PagingUtils.getQueryPage;
import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@AllArgsConstructor
public class EventServiceImpl implements EventService{
    private final EventRepository eventRepository;
    private final OrganizationRepository organizationRepository;
    private final InfluencerRepository influencerRepository;
    private final ProductRepository productRepository;
    private final SecurityService securityService;
    private final EventLogsRepository eventLogsRepository;
    private final OrganizationService organizationService;
    private final ProductService productService;
    private final EventRequestsRepository eventRequestsRepository;
    private final EventAttachmentsRepository eventAttachmentsRepository;
    private final InfluencerService influencerService;

    @Override
    public EventResponseDto createEvent(EventForRequestDTO dto) {
        InfluencerEntity influencer = null;
        OrganizationEntity org = organizationRepository.findById(dto.getOrganizationId()).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND, G$ORG$0001, dto.getOrganizationId())
        );
        if(dto.getInfluencerId() != null){
            influencer = influencerRepository.findById(dto.getInfluencerId()).orElseThrow(
                    () -> new RuntimeBusinessException(NOT_FOUND, G$INFLU$0001, dto.getOrganizationId())
            );
        }
        dto.getProductsIds().forEach(id -> {
            ProductEntity productEntity = productRepository.findById(id).orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND,P$PRO$0002,id));
            if(!Arrays.asList(ProductTypes.STOCK_ITEM,ProductTypes.BUNDLE).contains(productEntity.getProductType()))
                throw new RuntimeBusinessException(NOT_ACCEPTABLE,P$PRO$0016,id);
        });

        return toDto(eventRepository.save(toEntity(dto, org, influencer, null)));
    }

    @Override
    public EventResponseDto getEventById(Long eventId) {
        EventEntity entity = eventRepository.findById(eventId).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND,G$EVENT$0001,eventId)
        );
        List<EventEntity> relatedEvents = new ArrayList<>();
        List<Long> categoryIds = entity.getProducts().stream().map(o -> o.getCategoryId()).collect(Collectors.toList());
        categoryIds.removeAll(Collections.singletonList(null));
        if(!categoryIds.isEmpty()){
            relatedEvents = eventRepository.getRelatedEvents(categoryIds, eventId);
        }
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
    public PageImpl<EventResponseDto> getAllEventsForUser(Integer start, Integer count, Date dateFilter) {
        PageRequest page = getQueryPage(start, count);
        PageImpl<EventEntity> source = eventRepository.getAllEventFilterByDatePageable(dateFilter, page);
        List<EventResponseDto> dtos = source.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, source.getPageable(), source.getTotalElements());
    }

    @Override
    public PageImpl<EventResponseDto> getEventsForEmployee(Integer start, Integer count, EventStatus status, LocalDateTime fromDate, LocalDateTime endDate) {
        PageRequest page = getQueryPage(start, count);
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if (loggedInUser instanceof EmployeeUserEntity) {
            EmployeeUserEntity employeeUserEntity = (EmployeeUserEntity) loggedInUser;
            Page<EventEntity> source = eventRepository.findAll((root, cq, cb) -> {
                ArrayList<javax.persistence.criteria.Predicate> predicates = new ArrayList<>();

                predicates.add(cb.equal(root.get("organization").get("id"), employeeUserEntity.getOrganizationId()));
                if (status != null) {
                    predicates.add(cb.equal(root.get("status"), status.getValue()));
                }
                if (fromDate != null) {
                    predicates.add(cb.and(
                            root.get("startsAt").isNotNull(),
                            cb.greaterThanOrEqualTo(root.get("startsAt"), fromDate)));
                }

                if (endDate != null) {
                    predicates.add(cb.and(root.get("endsAt").isNotNull(), cb.lessThanOrEqualTo(root.get("endsAt"), endDate)));
                }

                return cb.and(predicates.toArray(Predicate[]::new));
            }, page);
            List<EventResponseDto> dtos = source.getContent().stream().map(this::toDto).collect(Collectors.toList());
            return new PageImpl<>(dtos, source.getPageable(), source.getTotalElements());
        }
        return null;
    }

    @Override
    public List<EventResponseDto> getAdvertisedEvents() {
    return eventRepository.getAllByInfluencerNullAndStartsAtAfter(LocalDateTime.now()).stream().map(this::toDto).collect(Collectors.toList());
//        return eventRepository.getAllByOrganizationOrFindAll(null).stream().map(this::toDto).collect(Collectors.toList());
    }



    @Override
    public List<EventResponseDto> getAdvertisedEventsForInfluencer() {
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        InfluencerEntity influencerEntity = influencerRepository.getByUser_IdOrEmployeeUser_Id(loggedInUser.getId(),loggedInUser.getId());
        if(influencerEntity != null){
            List<OrganizationEntity> orgs = organizationRepository.findYeshteryOrganizationsFilterByCategory(influencerEntity.getCategories().stream()
                    .map(o->o.getId()).collect(Collectors.toList()));
            List<EventEntity> eventEntities = eventRepository.getAllByOrganizationInAndInfluencerNullAndStartsAtAfter(orgs, LocalDateTime.now());
            List<EventEntity> requestsEntities = eventRequestsRepository.getAllByInfluencer_Id(influencerEntity.getId())
                    .stream().map(o -> o.getEvent()).collect(Collectors.toList());
            eventEntities.removeAll(requestsEntities);
            return eventEntities.stream().map(this::toDto).collect(Collectors.toList());
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
    @Transactional
    public void updateEvent(EventForRequestDTO dto, Long eventId) {
        EventEntity entity = eventRepository.findById(eventId).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND,G$EVENT$0001,eventId)
        );
        if(entity.getEndsAt().isBefore(LocalDateTime.now())){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE,EVENT$NOT$EDITABLE$0002,eventId);
        }

        eventAttachmentsRepository.deleteAllByEvent_Id(eventId);
        entity = toEntity(dto, entity.getOrganization(), entity.getInfluencer(), entity.getId());
        eventRepository.save(entity);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId, Boolean force) {
        EventEntity entity = eventRepository.findById(eventId).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND,G$EVENT$0001,eventId)
        );
        if(force){
            eventLogsRepository.deleteAllByEvent_Id(eventId);
            eventRequestsRepository.deleteAllByEvent_Id(eventId);
        }
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

        if (dto.getProductsIds()!= null && dto.getProductsIds().size() > 0) {
            dto.getProductsIds().forEach(i -> {
                Optional<ProductEntity> product = productRepository.findByIdAndOrganizationId(i, org.getId());
                if (product.isPresent()) {
                    products.add(product.get());
                }
            });
        }
        if (dto.getAttachments() != null && dto.getAttachments().size() > 0) {
            dto.getAttachments().forEach(o -> {
                o.setEvent(entity);
            });
        }
        if(id == null){
            entity.setStatus(EventStatus.PENDING.getValue());
        }
        else {
            entity.setStatus(dto.getStatus().getValue());
        }
        return entity;
    }



    public EventResponseDto toDto(EventEntity entity){
        ProductFetchDTO productFetchDTO = new ProductFetchDTO();
        productFetchDTO.setCheckVariants(false);
        productFetchDTO.setIncludeOutOfStock(true);
        productFetchDTO.setOnlyYeshteryProducts(false);
        Set<ProductDetailsDTO> productDetailsDTOS = new HashSet<>();
        entity.getProducts().forEach(o -> {
            try {
                productFetchDTO.setProductId(o.getId());
                productDetailsDTOS.add(productService.getProduct(productFetchDTO));
            } catch (BusinessException e) {
                e.printStackTrace();
            }
        });
        EventResponseDto dto = new EventResponseDto();
        dto.setId(entity.getId());
        dto.setStartsAt(entity.getStartsAt());
        dto.setEndsAt(entity.getEndsAt());
        dto.setOrganization(organizationService.getOrganizationById(entity.getOrganization().getId(),0));
        if(entity.getInfluencer() != null){
            dto.setInfluencer(influencerService.toInfluencerDto(entity.getInfluencer()));
        }
        dto.setVisible(entity.getVisible());
        dto.setAttachments(entity.getAttachments());
        dto.setDescription(entity.getDescription());
        dto.setName(entity.getName());
        dto.setStatus(EventStatus.getEnumByValue(entity.getStatus()));
        dto.setStatusRepresentation(EventStatus.getStatusRepresentation(entity.getStartsAt(), entity.getEndsAt()));
        dto.setProducts(productDetailsDTOS);
        return dto;
    }

    @Override
    public PageImpl<EventProjection> getAllEvents(Integer start, Integer count ,  LocalDateTime fromDate) {
        PageRequest page = getQueryPage(start, count);
        return  eventRepository.findAllOrderedByStartsAtDesc( fromDate , page );
    }

    @Override

    public PageImpl<EventProjection> getAllAdvertisedEvents(Integer start, Integer count, Long orgId) {
        PageRequest page = getQueryPage(start, count);
        OrganizationEntity organization=null;
        if (orgId !=null){
            organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, G$ORG$0001, orgId));
        }
       return  eventRepository.getAllByOrganizationOrFindAll(page,organization);
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

    @Override
    public boolean hasInfluencerOrEmployeeAccessToEvent(BaseUserEntity user, Long eventId) {
        if (user == null || eventId == null) {
            return false;
        }
        EventEntity event = eventRepository.findById(eventId).orElse(null);
        return hasInfluencerOrEmployeeAccessToEvent(user, event);
    }

    @Override
    public boolean hasInfluencerOrEmployeeAccessToEvent(BaseUserEntity user, EventEntity event) {
        if (event == null) {
            return false;
        }
        if (user instanceof EmployeeUserEntity) {
            return user.getOrganizationId().equals(event.getOrganization().getId());
        } else {
            try {
                // change next line if event will have multiple influnecers
                return user.equals(event.getInfluencer().getUser());
            } catch(Exception ex) {
                return false;
            }

        }
    }
}
