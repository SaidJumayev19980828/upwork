package com.nasnav.service.impl;

import static java.lang.Boolean.FALSE;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.AppConfig;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.RoomTemplateRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dto.request.RoomSessionDTO;
import com.nasnav.dto.request.RoomTemplateDTO;
import com.nasnav.dto.response.RoomResponse;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.mappers.RoomMapper;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.RoomSessionEntity;
import com.nasnav.persistence.RoomTemplateEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.service.MetaverseRoomService;
import com.nasnav.service.SecurityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetaverseRoomServiceImpl implements MetaverseRoomService {
	private final RoomTemplateRepository roomTemplateRepository;
	private final OrganizationRepository organizationRepository;
	private final ShopsRepository shopsRepository;
	private final SecurityService securityService;
	private final AppConfig config;
	private final RoomMapper mapper;

	@Transactional
	@Override
	public RoomResponse createOrUpdateTemplate(Long shopId, RoomTemplateDTO dto) {
		RoomTemplateEntity entity = getRoomTemplateForUpdate(shopId).orElseGet(() -> getNewRoomTemplate(shopId));
		mapper.updateTemplateEntityfromDTO(dto, entity);
		entity = roomTemplateRepository.save(entity);
		return mapper.toRoomResponse(entity);
	}

	@Transactional
	@Override
	public RoomResponse createNewSession(Long shopId, RoomSessionDTO roomSessionDto) {
		RoomTemplateEntity template = getRoomTemplateForUpdate(shopId)
				.orElseThrow(
						() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.ROOMS$ROOM$NotFound,
								shopId));
		RoomSessionEntity session = getNewRoomSession(roomSessionDto, template.getSession());
		session.setTemplate(template);
		template.setSession(session);
		template = roomTemplateRepository.save(template);
		return mapper.toRoomResponse(template);
	}

	@Transactional
	@Override
	public void deleteTemplate(Long shopId) {
		Long employeeOrgId = securityService.getCurrentUserOrganizationId();
		int affectedRows = roomTemplateRepository.deleteTemplateByShopIdAndShopOrganizationEntityId(shopId, employeeOrgId);
		if (affectedRows != 1) {
			throw new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.ROOMS$ROOM$NotFound, shopId);
		}
	}

	@Override
	public RoomResponse getRoombyShopId(Long shopId) {
		return getRoomTemplate(shopId)
				.map(mapper::toRoomResponse)
				.orElseThrow(
						() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.ROOMS$ROOM$NotFound,
								shopId));
	}

	@Override
	public Set<RoomResponse> getOrgRooms(Long orgId) {
		Set<RoomTemplateEntity> rooms;

		OrganizationEntity requestedOrg = organizationRepository.findById(orgId)
				.orElseThrow(
						() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.ROOMS$ORG$NotFound,
								orgId));
		if (!config.isYeshteryInstance || requestedOrg.getYeshteryState() == 1) {
			rooms = roomTemplateRepository.findAllByShopOrganizationEntityId(orgId);
		} else {
			throw new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.ROOMS$ORG$NotFound, orgId);
		}
		return rooms.stream().map(mapper::toRoomResponse).collect(Collectors.toSet());
	}

	@Override
	public Set<RoomResponse> getRooms() {
		Set<RoomTemplateEntity> rooms = Collections.emptySet();
		OrganizationEntity userOrg = securityService.getCurrentUserOrganization();
		if (config.isYeshteryInstance) {
			if (userOrg.getYeshteryState() == 1) {
				rooms = roomTemplateRepository.findAllByShopOrganizationEntityYeshteryStateEquals1();
			}
		} else {
			rooms = roomTemplateRepository.findAllByShopOrganizationEntityId(userOrg.getId());
		}
		return rooms.stream().map(mapper::toRoomResponse).collect(Collectors.toSet());
	}

	private Optional<RoomTemplateEntity> getRoomTemplateForUpdate(Long shopId) {
		if (FALSE.equals(securityService.isShopAccessibleToCurrentUser(shopId))) {
			throw new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.ROOMS$ROOM$NotFound, shopId);
		}
		return getRoomTemplate(shopId);
	}

	private Optional<RoomTemplateEntity> getRoomTemplate(Long shopId) {
		return roomTemplateRepository.findByShopId(shopId).filter(room -> !config.isYeshteryInstance || room.getShop().getOrganizationEntity().getYeshteryState() == 1);
	}

	private RoomTemplateEntity getNewRoomTemplate(Long shopId) {
		OrganizationEntity userOrg = securityService.getCurrentUserOrganization();
		ShopsEntity requestedShop = shopsRepository.findById(shopId).filter(shop -> shop.getOrganizationEntity().equals(userOrg))
				.orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.S$0002, shopId));

		RoomTemplateEntity template = new RoomTemplateEntity();
		template.setShop(requestedShop);
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
