package com.nasnav.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.AppConfig;
import com.nasnav.commons.utils.PagingUtils;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.EventRepository;
import com.nasnav.dao.EventRoomTemplateRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dto.request.RoomSessionDTO;
import com.nasnav.dto.request.RoomTemplateDTO;
import com.nasnav.dto.response.EventRoomResponse;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.mappers.EventRoomMapper;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EventEntity;
import com.nasnav.persistence.EventRoomTemplateEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.RoomSessionEntity;
import com.nasnav.service.EventRoomService;
import com.nasnav.service.EventService;
import com.nasnav.service.SecurityService;

import lombok.RequiredArgsConstructor;

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

	@Transactional
	@Override
	public EventRoomResponse createOrUpdateTemplate(Long eventId, RoomTemplateDTO dto) {
		EventRoomTemplateEntity entity = getRoomTemplateForUpdate(eventId).orElseGet(() -> getNewRoomTemplate(eventId));
		mapper.updateTemplateEntityfromDTO(dto, entity);
		entity = roomTemplateRepository.save(entity);
		return mapper.toResponse(entity);
	}

	@Transactional
	@Override
	public EventRoomResponse createNewSession(Long eventId, RoomSessionDTO roomSessionDto) {
		EventRoomTemplateEntity template = getRoomTemplateForUpdate(eventId)
				.orElseThrow(
						() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.ROOMS$ROOM$NotFound,
								ROOM_TYPE, eventId));
		RoomSessionEntity session = getNewRoomSession(roomSessionDto, template.getSession());
		session.setTemplate(template);
		template.setSession(session);
		template = roomTemplateRepository.save(template);
		return mapper.toResponse(template);
	}

	@Transactional
	@Override
	public void deleteTemplate(Long eventId) {
		Long employeeOrgId = securityService.getCurrentUserOrganizationId();
		int affectedRows = roomTemplateRepository.deleteTemplateByEventIdAndEventOrganizationId(eventId, employeeOrgId);
		if (affectedRows != 1) {
			throw new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.ROOMS$ROOM$NotFound, ROOM_TYPE,
					eventId);
		}
	}

	@Transactional
	@Override
	public EventRoomResponse getRoombyEventId(Long eventId) {
		return getRoomTemplate(eventId)
				.map(mapper::toResponse)
				.orElseThrow(
						() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.ROOMS$ROOM$NotFound,
								ROOM_TYPE,
								eventId));
	}

	@Override
	public Page<EventRoomResponse> getOrgRooms(Long orgId, Boolean started, Integer start, Integer count) {
		Page<EventRoomTemplateEntity> rooms;

		Pageable pageable = PagingUtils.getQueryPageAddIdSort(start, count);

		OrganizationEntity requestedOrg = organizationRepository.findById(orgId)
				.orElseThrow(
						() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.ROOMS$ORG$NotFound,
								orgId));
		if (!config.isYeshteryInstance || requestedOrg.getYeshteryState() == 1) {
			rooms = started == null ? roomTemplateRepository.findAllByEventOrganizationId(orgId, pageable)
					: roomTemplateRepository.findAllByEventOrganizationIdAndSessionNullEquals(orgId, started, pageable);
		} else {
			throw new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.ROOMS$ORG$NotFound, orgId);
		}
		return rooms.map(mapper::toResponse);
	}

	@Override
	public Page<EventRoomResponse> getRooms(Boolean started, Integer start, Integer count) {
		Page<EventRoomTemplateEntity> rooms = Page.empty();
		Pageable pageable = PagingUtils.getQueryPageAddIdSort(start, count);
		OrganizationEntity userOrg = securityService.getCurrentUserOrganization();
		if (config.isYeshteryInstance) {
			if (userOrg.getYeshteryState() == 1) {
				rooms = started == null
						? roomTemplateRepository.findAllByEventOrganizationYeshteryStateEquals1(pageable)
						: roomTemplateRepository.findAllByEventOrganizationYeshteryStateEquals1AndStarted(started,
								pageable);
			}
		} else {
			rooms = started == null ? roomTemplateRepository.findAllByEventOrganizationId(userOrg.getId(), pageable)
					: roomTemplateRepository.findAllByEventOrganizationIdAndSessionNullEquals(userOrg.getId(), started, pageable);
		}
		return rooms.map(mapper::toResponse);
	}

	private Optional<EventRoomTemplateEntity> getRoomTemplateForUpdate(Long eventId) {
		BaseUserEntity currentUser = securityService.getCurrentUser();
		if (!eventService.hasInfluencerOrEmployeeAccessToEvent(currentUser, eventId)) {
			throw new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.ROOMS$ROOM$NotFound, ROOM_TYPE,
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
						() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.G$EVENT$0001, eventId));

		EventRoomTemplateEntity template = new EventRoomTemplateEntity();
		template.setEvent(requestedEvent);
		return template;
	}

	private RoomSessionEntity getNewRoomSession(RoomSessionDTO sessionDto, RoomSessionEntity oldSession) {
		RoomSessionEntity session = new RoomSessionEntity();
		if (sessionDto != null && StringUtils.isNotBlankOrNull(sessionDto.getSessionExternalId())) {
			session.setExternalId(sessionDto.getSessionExternalId());
		} else {
			String externalId = oldSession != null && StringUtils.isNotBlankOrNull(oldSession.getExternalId())
					? oldSession.getExternalId()
					: UUID.randomUUID().toString();
			session.setExternalId(externalId);
		}
		return session;
	}
}
