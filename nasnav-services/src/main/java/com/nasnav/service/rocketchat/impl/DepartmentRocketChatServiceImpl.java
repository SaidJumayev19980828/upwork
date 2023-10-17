package com.nasnav.service.rocketchat.impl;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.RocketChatOrganizationDepartmentRepository;
import com.nasnav.dto.rocketchat.RocketChatDepartmentDTO;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.RocketChatOrganizationDepartmentEntity;
import com.nasnav.service.rocketchat.DepartmentRocketChatService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DepartmentRocketChatServiceImpl implements DepartmentRocketChatService {
	private final OrganizationRepository organizationRepository;
	private final RocketChatOrganizationDepartmentRepository departmentsRepo;
	private final RocketChatClient client;

	@Transactional
	@Override
	public Mono<String> getDepartmentIdCreateDepartmentIfNeeded(Long orgId) {
		return organizationRepository.findById(orgId)
				.map(this::getDepartmentIdCreateDepartmentIfNeeded)
				.orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,
						ErrorCodes.G$ORG$0001,
						orgId));
	}

	public Mono<String> getDepartmentIdCreateDepartmentIfNeeded(OrganizationEntity org) {
		return departmentsRepo.findByOrganizationId(org.getId())
				.map(department -> client.getDepartment(department.getDepartmentId())
						.switchIfEmpty(Mono.defer(() -> createDepartment(department))))
				.orElseGet(() -> createDepartmentDeleteIfFailed(persistDepartment(org)))
				.map(RocketChatDepartmentDTO::getId)
				.onErrorResume(WebClientResponseException.class,
						e -> Mono.error(new RuntimeBusinessException(
								HttpStatus.NOT_ACCEPTABLE,
								ErrorCodes.CHAT$EXTERNAL,
								e.getStatusCode().value())));
	}

	private Mono<RocketChatDepartmentDTO> createDepartmentDeleteIfFailed(RocketChatOrganizationDepartmentEntity department) {
		return createDepartment(department).doOnError(Exception.class, ex -> departmentsRepo.deleteById(department.getOrgId()));
	}

	private Mono<RocketChatDepartmentDTO> createDepartment(RocketChatOrganizationDepartmentEntity department) {
		RocketChatDepartmentDTO dto = RocketChatDepartmentDTO.builder()
				.enabled(true)
				.email("no@email")
				.name(department.getOrgId().toString())
				.description(department.getOrganization().getName())
				.id(department.getDepartmentId())
				.build();
		return client.createDepartment(dto);
	}

	private RocketChatOrganizationDepartmentEntity persistDepartment(OrganizationEntity org) {
		RocketChatOrganizationDepartmentEntity entity = new RocketChatOrganizationDepartmentEntity();
		entity.setOrganization(org);
		entity.setDepartmentId(UUID.randomUUID().toString());
		return departmentsRepo.save(entity);
	}
}
