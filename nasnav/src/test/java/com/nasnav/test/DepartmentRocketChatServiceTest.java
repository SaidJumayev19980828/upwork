package com.nasnav.test;

import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;

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
	private OrganizationRepository organizationRepository;
	private DepartmentRocketChatService departmentRocketChatService;

	private static final RocketChatOrganizationDepartmentEntity departmentEntity = createDepartmentEntity();

	@BeforeEach
	void setup() {
		departmentRocketChatService = new DepartmentRocketChatServiceImpl(organizationRepository, departmentsRepo,
				client);
		Mockito.when(organizationRepository.findById(departmentEntity.getOrgId()))
				.thenReturn(Optional.of(departmentEntity.getOrganization()));
	}

	@Test
	void returnPDepartmentIdIfFound() {
		Mockito.when(departmentsRepo.findByOrganizationId(departmentEntity.getOrganization().getId()))
				.thenReturn(Optional.of(departmentEntity));
		Mockito.when(client.getDepartment(departmentEntity.getDepartmentId()))
				.thenReturn(Mono.just(RocketChatDepartmentDTO.builder()
						.id(departmentEntity.getDepartmentId())
						.build()));
		Mono<String> departmentIdMono = departmentRocketChatService
				.getDepartmentIdCreateDepartmentIfNeeded(departmentEntity.getOrganization().getId());
		StepVerifier.create(departmentIdMono)
				.expectNext(departmentEntity.getDepartmentId())
				.verifyComplete();
	}

	@Test
	void createDepartmentIfNotFound() {
		Mockito.when(departmentsRepo.findByOrganizationId(departmentEntity.getOrganization().getId()))
				.thenReturn(Optional.empty());
		Mockito.when(client.createDepartment(any()))
				.thenReturn(
						Mono.just(RocketChatDepartmentDTO.builder()
								.id(departmentEntity.getDepartmentId())
								.build()));
		Mockito.when(departmentsRepo.save(any())).thenReturn(departmentEntity);

		Mono<String> departmentIdMono = departmentRocketChatService
				.getDepartmentIdCreateDepartmentIfNeeded(departmentEntity.getOrganization().getId());
		StepVerifier.create(departmentIdMono)
				.expectNext(departmentEntity.getDepartmentId())
				.verifyComplete();
	}

	private static RocketChatOrganizationDepartmentEntity createDepartmentEntity() {
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
