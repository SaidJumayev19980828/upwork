package com.nasnav.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.nasnav.dao.OrganizationThemeRepository;
import com.nasnav.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.AppConfig;
import com.nasnav.commons.utils.PagingUtils;
import com.nasnav.dao.EventRepository;
import com.nasnav.dao.EventRoomTemplateRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dto.request.RoomSessionDTO;
import com.nasnav.dto.request.RoomTemplateDTO;
import com.nasnav.dto.response.EventRoomResponse;
import com.nasnav.enumerations.EventRoomStatus;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.mappers.EventRoomMapper;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EventEntity;
import com.nasnav.persistence.EventRoomTemplateEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.service.EventRoomService;
import com.nasnav.service.EventService;
import com.nasnav.service.SecurityService;

import lombok.RequiredArgsConstructor;

import static com.nasnav.exceptions.ErrorCodes.G$ORG$0001;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class EventRoomServiceImpl implements EventRoomService {
	private static final String ROOM_TYPE = "event";
	private final EventRoomTemplateRepository roomTemplateRepository;
	private final OrganizationRepository organizationRepository;
	private final EventRepository eventRepository;
	private final SecurityService securityService;
	private final AppConfig config;
	private final EventRoomMapper mapper;
	private final EventService eventService;
	private final OrganizationThemeRepository organizationThemeRepository;

	@Transactional
	@Override
	public EventRoomResponse createOrUpdateTemplate(Long eventId, RoomTemplateDTO dto) {
		EventRoomTemplateEntity entity = getRoomTemplateForUpdate(eventId).orElseGet(() -> getNewRoomTemplate(eventId));
		mapper.updateTemplateEntityfromDTO(dto, entity);
		entity = roomTemplateRepository.save(entity);
		return toFullResponse(entity);
	}

	@Transactional
	@Override
	public EventRoomResponse startSession(Long eventId, Optional<RoomSessionDTO> roomSessionDto) {
		EventRoomTemplateEntity template = getRoomTemplateForUpdate(eventId)
				.orElseThrow(
						() -> new RuntimeBusinessException(NOT_FOUND, ErrorCodes.ROOMS$ROOM$NotFound,
								ROOM_TYPE, eventId));
		try {
			template.start(roomSessionDto.map(RoomSessionDTO::getSessionExternalId).orElse(null));
			template = roomTemplateRepository.save(template);
		} catch (IllegalStateException ex) {
			throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, ErrorCodes.ROOMS$ROOM$InvalidStatus,
								template.getStatus(), EventRoomStatus.SUSPENDED);
		}
		return toFullResponse(template);
	}

	@Transactional
	@Override
	public void suspendSession(Long eventId) {
		EventRoomTemplateEntity template = getRoomTemplateForUpdate(eventId)
				.orElseThrow(
						() -> new RuntimeBusinessException(NOT_FOUND, ErrorCodes.ROOMS$ROOM$NotFound,
								ROOM_TYPE, eventId));
		try {
			template.suspend();
			roomTemplateRepository.save(template);
		} catch (IllegalStateException ex) {
			throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, ErrorCodes.ROOMS$ROOM$InvalidStatus,
								template.getStatus(), EventRoomStatus.SUSPENDED);
		}
	}

	@Transactional
	@Override
	public void deleteTemplate(Long eventId) {
		Long employeeOrgId = securityService.getCurrentUserOrganizationId();
		int affectedRows = roomTemplateRepository.deleteTemplateByEventIdAndEventOrganizationId(eventId, employeeOrgId);
		if (affectedRows != 1) {
			throw new RuntimeBusinessException(NOT_FOUND, ErrorCodes.ROOMS$ROOM$NotFound, ROOM_TYPE,
					eventId);
		}
	}

	@Transactional
	@Override
	public EventRoomResponse getRoombyEventId(Long eventId) {
		return getRoomTemplate(eventId)
				.map(this::toFullResponse)
				.orElseThrow(
						() -> new RuntimeBusinessException(NOT_FOUND, ErrorCodes.ROOMS$ROOM$NotFound,
								ROOM_TYPE,
								eventId));
	}

	@Override
	public Page<EventRoomResponse> getOrgRooms(Long orgId, EventRoomStatus status, Integer start, Integer count) {
		Page<EventRoomTemplateEntity> rooms;

		Pageable pageable = PagingUtils.getQueryPageAddIdSort(start, count);

		OrganizationEntity requestedOrg = organizationRepository.findById(orgId)
				.orElseThrow(
						() -> new RuntimeBusinessException(NOT_FOUND, ErrorCodes.ROOMS$ORG$NotFound,
								orgId));
		if (!config.isYeshteryInstance || requestedOrg.getYeshteryState() == 1) {
			rooms = status == null ? roomTemplateRepository.findAllByEventOrganizationId(orgId, pageable)
					: roomTemplateRepository.findAllByEventOrganizationIdAndStatus(orgId, status, pageable);
		} else {
			throw new RuntimeBusinessException(NOT_FOUND, ErrorCodes.ROOMS$ORG$NotFound, orgId);
		}
		return rooms.map(this::toFullResponse);
	}

	@Override
	public Page<EventRoomResponse> getRooms(EventRoomStatus status, Integer start, Integer count) {
		Page<EventRoomTemplateEntity> rooms = Page.empty();
		Pageable pageable = PagingUtils.getQueryPageAddIdSort(start, count);
		OrganizationEntity userOrg = securityService.getCurrentUserOrganization();
		if (config.isYeshteryInstance) {
			if (userOrg.getYeshteryState() == 1) {
				rooms = status == null
						? roomTemplateRepository.findAllByEventOrganizationYeshteryStateEquals1(pageable)
						: roomTemplateRepository.findAllByEventOrganizationYeshteryStateEquals1AndStatus(status,
								pageable);
			}
		} else {
			rooms = status == null ? roomTemplateRepository.findAllByEventOrganizationId(userOrg.getId(), pageable)
					: roomTemplateRepository.findAllByEventOrganizationIdAndStatus(userOrg.getId(), status, pageable);
		}
		return rooms.map(this::toFullResponse);
	}

	@Override
	public PageImpl<EventsRoomNewDTO> getUserRooms(Long orgId, Integer start, Integer count) {

		Pageable pageable = PagingUtils.getQueryPageAddIdSort(start, count);
		OrganizationEntity organization=null;
		if (orgId !=null){
			organization = organizationRepository.findById(orgId)
					.orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, G$ORG$0001, orgId));
		}
		PageImpl<EventRoomProjection>   rooms = roomTemplateRepository.findAllByEventOrganization(organization, pageable);

		List<EventsRoomNewDTO> dtos = rooms.getContent().stream().map(this::mapRoomEventProjectionToEventsRoomNewDTO).collect(Collectors.toList());
		return new PageImpl<>(dtos, rooms.getPageable(), rooms.getTotalElements());
	}

	public  EventsRoomNewDTO mapRoomEventProjectionToEventsRoomNewDTO(EventRoomProjection eventRoomProjection) {
		EventsNewDTO eventsDTO = mapEventProjectionToDTO(eventRoomProjection.getEvent(),eventRoomProjection.getInterest());
		BaseUserEntity loggedInUser = securityService.getCurrentUser();
		Long influencerId = loggedInUser.getRepresentation().getInfluencerId();

		Boolean canStart= influencerId == null ? false  :userCanStartEvent(eventsDTO.getInfluencers(),influencerId);
		EventsRoomNewDTO eventsRoom= EventsRoomNewDTO.eventRomBuilder(eventsDTO,canStart,eventRoomProjection.getTemplate().getSceneId(),eventRoomProjection.getTemplate().getData());

		return eventsRoom;
	}
	public EventsNewDTO mapEventProjectionToDTO(EventProjection eventProjection ,Long interests)  {
		OrganizationProjection orgProjection = eventProjection.getOrganization();
		OrganizationNewDTO orgDTO = new OrganizationNewDTO();
		List<String> logo =organizationThemeRepository. getLogoByOrganizationEntity_Id(eventProjection.getOrganization().getId());
		orgDTO.setId(orgProjection.getId());
		orgDTO.setName(orgProjection.getName());
		orgDTO.setUri(logo.isEmpty() ? "nasnav-logo.png" : logo.get(0));
		return EventsNewDTO.buildNewEventsFromEventProjection(eventProjection ,interests , orgDTO);

	}

	private Boolean userCanStartEvent(List<InflunecerEventDto> entity , Long userId) {
		return entity.stream().anyMatch(user -> user.id().equals(userId));
	}

	private Optional<EventRoomTemplateEntity> getRoomTemplateForUpdate(Long eventId) {
		BaseUserEntity currentUser = securityService.getCurrentUser();
		if (!eventService.hasInfluencerOrEmployeeAccessToEvent(currentUser, eventId)) {
			throw new RuntimeBusinessException(NOT_FOUND, ErrorCodes.ROOMS$ROOM$NotFound, ROOM_TYPE,
					eventId);
		}
		return getRoomTemplate(eventId);
	}

	private Optional<EventRoomTemplateEntity> getRoomTemplate(Long eventId) {
		return roomTemplateRepository.findByEventId(eventId).filter(
				room -> !config.isYeshteryInstance || room.getEvent().getOrganization().getYeshteryState() == 1);
	}

	private EventRoomTemplateEntity getNewRoomTemplate(Long eventId) {
		OrganizationEntity userOrg = securityService.getCurrentUserOrganization();
		EventEntity requestedEvent = eventRepository.findById(eventId)
				.filter(event -> event.getOrganization().equals(userOrg))
				.orElseThrow(
						() -> new RuntimeBusinessException(NOT_FOUND, ErrorCodes.G$EVENT$0001, eventId));

		//TODO : ADD the following line To create Event API
		EventRoomTemplateEntity template = new EventRoomTemplateEntity();
		template.setEvent(requestedEvent);
		return template;
	}

	private EventRoomResponse toFullResponse(EventRoomTemplateEntity entity) {
		BaseUserEntity currentUser = securityService.getCurrentUserOptional().orElse(null);
		EventRoomResponse response = mapper.toResponse(entity);
		response.setCanStart(eventService.hasInfluencerOrEmployeeAccessToEvent(currentUser, entity.getEvent()));
		if (response.getStatus() != EventRoomStatus.STARTED && !response.isCanStart()) {
			response.setSessionExternalId(null);
		}
		// following line needs much optimization 
		response.setEvent(eventService.toDto(entity.getEvent()));
		
		return response;
	}
}
