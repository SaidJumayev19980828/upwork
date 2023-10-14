package com.nasnav.service.rocketchat.impl;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.RocketChatOrganizationDepartmentRepository;
import com.nasnav.dto.rocketchat.RocketChatDepartmentDTO;
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

	@RequiredArgsConstructor
	private class SaveDepartmentException extends RuntimeException {
		public final String departmentId;
	}

	@Override
	@Transactional
	public Mono<String> getDepartmentIdCreateDepartmentIfNeeded(OrganizationEntity org) {
		return Mono.justOrEmpty(departmentsRepo.findByOrganizationId(org.getId()))
				.switchIfEmpty(Mono.defer(() -> createDepartment(org)))
				.map(RocketChatOrganizationDepartmentEntity::getDepartmentId);
	}

	@Override
	public Mono<String> getDepartmentIdCreateDepartmentIfNeeded(Long orgId) {
		return Mono.fromSupplier(() -> organizationRepository.getOne(orgId))
				.flatMap(this::getDepartmentIdCreateDepartmentIfNeeded);
	}

	private Mono<RocketChatOrganizationDepartmentEntity> createDepartment(OrganizationEntity org) {
		RocketChatDepartmentDTO dto = RocketChatDepartmentDTO.builder()
				.enabled(true)
				.email("no@email")
				.name(org.getId().toString())
				.description(org.getName())
				.build();
		return client.createDepartment(dto)
				.map(response -> persistDepartment(response, org))
				.doOnError(SaveDepartmentException.class, ex -> client.deleteDepartment(ex.departmentId))
				.onErrorMap(SaveDepartmentException.class, ex -> new IllegalStateException());
	}

	private RocketChatOrganizationDepartmentEntity persistDepartment(
			RocketChatDepartmentDTO dto,
			OrganizationEntity org) {
		try {
			RocketChatOrganizationDepartmentEntity entity = new RocketChatOrganizationDepartmentEntity();
			entity.setOrganization(org);
			entity.setDepartmentId(dto.getId());
			return departmentsRepo.save(entity);
		} catch (Exception ex) {
			throw new SaveDepartmentException(dto.getId());
		}
	}
}
