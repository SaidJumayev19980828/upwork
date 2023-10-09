package com.nasnav.test;

import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.RocketChatOrganizationDepartmentRepository;
import com.nasnav.dto.rocketchat.RocketChatDepartmentDTO;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.RocketChatOrganizationDepartmentEntity;
import com.nasnav.service.rocketchat.DepartmentRocketChatService;
import com.nasnav.service.rocketchat.impl.DepartmentRocketChatServiceImpl;
import com.nasnav.service.rocketchat.impl.RocketChatClient;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class DepartmentRocketChatServiceTest {
	@Mock
	private RocketChatOrganizationDepartmentRepository departmentsRepo;
	@Mock
	private RocketChatClient client;
	@Mock
	OrganizationRepository organizationRepository;
	private DepartmentRocketChatService departmentRocketChatService;

	@BeforeEach
	void setup() {
		departmentRocketChatService = new DepartmentRocketChatServiceImpl(organizationRepository, departmentsRepo, client);
	}

	@Test
	void returnPDepartmentIdIfFound() {
		RocketChatOrganizationDepartmentEntity departmentEntity = createDepartmentEntity();
		Mockito.when(departmentsRepo.findByOrganizationId(departmentEntity.getOrganization().getId()))
				.thenReturn(Mono.just(departmentEntity));
		Mono<String> departmentIdMono = departmentRocketChatService
				.getDepartmentIdCreateDepartmentIfNeeded(departmentEntity.getOrganization());
		StepVerifier.create(departmentIdMono)
				.expectNext(departmentEntity.getDepartmentId())
				.verifyComplete();
	}

	@Test
	void createDepartmentIfNotFound() {
		RocketChatOrganizationDepartmentEntity departmentEntity = createDepartmentEntity();
		Mockito.when(departmentsRepo.findByOrganizationId(departmentEntity.getOrganization().getId()))
				.thenReturn(Mono.empty());
		Mockito.when(client.createDepartment(any()))
				.thenReturn(
						Mono.just(RocketChatDepartmentDTO.builder()
								.id(departmentEntity.getDepartmentId())
								.build()));
		Mockito.when(departmentsRepo.save(any())).thenReturn(departmentEntity);

		Mono<String> departmentIdMono = departmentRocketChatService
				.getDepartmentIdCreateDepartmentIfNeeded(departmentEntity.getOrganization());
		StepVerifier.create(departmentIdMono)
				.expectNext(departmentEntity.getDepartmentId())
				.verifyComplete();
	}

	@Test
	void createDepartmentRace() {
		RocketChatOrganizationDepartmentEntity departmentEntity = createDepartmentEntity();
		Mockito.when(departmentsRepo.findByOrganizationId(departmentEntity.getOrganization().getId()))
				.thenReturn(Mono.empty());
		Mockito.when(client.createDepartment(any()))
				.thenReturn(
						Mono.just(RocketChatDepartmentDTO.builder()
								.id(departmentEntity.getDepartmentId())
								.build()));
		Mockito.when(departmentsRepo.save(any())).thenThrow(new RuntimeException());
		Mockito.when(client.deleteDepartment(departmentEntity.getDepartmentId())).thenReturn(Mono.empty());

		Mono<String> departmentIdMono = departmentRocketChatService
				.getDepartmentIdCreateDepartmentIfNeeded(departmentEntity.getOrganization());
		StepVerifier.create(departmentIdMono)
				.expectError(IllegalStateException.class)
				.verify();
	}

	RocketChatOrganizationDepartmentEntity createDepartmentEntity() {
		final Long orgId = 51L;
		final String testDepartmentId = "TEST_DEPARTMENT_ID";
		OrganizationEntity org = new OrganizationEntity();
		org.setId(orgId);
		RocketChatOrganizationDepartmentEntity department = new RocketChatOrganizationDepartmentEntity();
		department.setDepartmentId(testDepartmentId);
		department.setOrganization(org);
		department.setOrgId(orgId);
		return department;
	}
}
