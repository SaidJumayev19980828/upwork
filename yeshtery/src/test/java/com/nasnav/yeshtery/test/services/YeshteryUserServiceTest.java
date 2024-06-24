package com.nasnav.yeshtery.test.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.time.LocalDateTime;
import java.util.*;

import com.nasnav.dao.CommonUserRepository;
import com.nasnav.dao.UserAddressRepository;
import com.nasnav.dao.yeshtery.CommonYeshteryUserRepository;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.enumerations.Roles;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.UserAddressEntity;
import com.nasnav.service.RoleService;
import com.nasnav.service.SecurityService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.yeshtery.YeshteryUserService;
import com.nasnav.service.yeshtery.YeshteryUserServiceImpl;

@ExtendWith(MockitoExtension.class)
class YeshteryUserServiceTest {
	@Mock
	private UserRepository nasnavUserRepository;

	@Mock
	private OrganizationRepository organizationRepository;

	@Mock
	private SecurityService securityService;

	@Mock
	private RoleService roleService;

	@Mock
	private CommonUserRepository commonNasnavUserRepo;

	@Mock
	private CommonYeshteryUserRepository commonUserRepo;

	@Mock
	private UserAddressRepository userAddressRepo;

	private UserEntity currentUser;
	private UserRepresentationObject currentUserRep;


	@InjectMocks
	private YeshteryUserService yeshteryUserService = new YeshteryUserServiceImpl();

	@BeforeEach
	public void setUp() {
		currentUser = new UserEntity();
		currentUser.setId(2L);
		currentUser.setOrganizationId(99002L);

		currentUserRep = new UserRepresentationObject();
		currentUserRep.setId(2L);
	}

	@Test
	void testGetUserForOrg() {
		UserEntity user = new UserEntity();
		user.setOrganizationId(99001L);
		user.setYeshteryUserId(5L);

		when(nasnavUserRepository.findByYeshteryUserIdAndOrganizationId(user.getYeshteryUserId(),
				99002L))
				.thenReturn(Optional.ofNullable(currentUser));

		when(organizationRepository.existsByIdAndYeshteryState(99002L, 1)).thenReturn(true);
		when(organizationRepository.existsByIdAndYeshteryState(99001L, 1)).thenReturn(true);

		UserEntity returnedUser = yeshteryUserService.getUserForOrg(user, 99002L);
		assertEquals(currentUser, returnedUser);

		verifyNoMoreInteractions(nasnavUserRepository, organizationRepository);
	}

	@Test
	public void testGetYeshteryUserData_CurrentUserIsCustomer() throws BusinessException {
		when(securityService.getCurrentUser()).thenReturn(currentUser);
		when(securityService.currentUserIsCustomer()).thenReturn(true);
		mockUserRepresentation(currentUser);

		UserRepresentationObject result = yeshteryUserService.getYeshteryUserData(2L, true);

		assertNotNull(result);
		verify(securityService).getCurrentUser();
		verify(securityService).currentUserIsCustomer();
		verifyNoMoreInteractions(securityService, roleService, commonNasnavUserRepo, commonUserRepo, userAddressRepo);
	}

	@Test
	public void testGetYeshteryUserData_UserIdIsNull() throws BusinessException {
		when(securityService.getCurrentUser()).thenReturn(currentUser);
		mockUserRepresentation(currentUser);

		UserRepresentationObject result = yeshteryUserService.getYeshteryUserData(null, true);

		assertNotNull(result);
		verify(securityService).getCurrentUser();
	}

	@Test
	public void testGetYeshteryUserData_CurrentUserNotAllowedToViewNonEmployees() {
		when(securityService.getCurrentUser()).thenReturn(currentUser);
		when(securityService.currentUserIsCustomer()).thenReturn(false);
		when(roleService.getEmployeeHighestRole(currentUser.getId())).thenReturn(Roles.ORGANIZATION_EMPLOYEE);

		RuntimeBusinessException exception = assertThrows(RuntimeBusinessException.class, () -> {
			yeshteryUserService.getYeshteryUserData(2L, false);
		});

		Assertions.assertEquals(NOT_ACCEPTABLE, exception.getHttpStatus());
		verify(securityService).getCurrentUser();
		verify(securityService).currentUserIsCustomer();
		verify(roleService).getEmployeeHighestRole(currentUser.getId());
		verifyNoMoreInteractions(securityService, roleService, commonNasnavUserRepo, commonUserRepo, userAddressRepo);
	}

	@Test
	public void testGetYeshteryUserData_UserNotFound() {
		when(securityService.getCurrentUser()).thenReturn(currentUser);
		when(securityService.currentUserIsCustomer()).thenReturn(false);
		when(roleService.getEmployeeHighestRole(currentUser.getId())).thenReturn(Roles.ORGANIZATION_ADMIN);
		when(commonNasnavUserRepo.findByIdAndOrganizationId(2L, 99002L, true)).thenReturn(Optional.empty());

		RuntimeBusinessException exception = assertThrows(RuntimeBusinessException.class, () -> {
			yeshteryUserService.getYeshteryUserData(2L, true);
		});

		Assertions.assertEquals(NOT_ACCEPTABLE, exception.getHttpStatus());
		verify(securityService).getCurrentUser();
		verify(securityService).currentUserIsCustomer();
		verify(roleService).getEmployeeHighestRole(currentUser.getId());
		verify(commonNasnavUserRepo).findByIdAndOrganizationId(2L, 99002L, true);
		verifyNoMoreInteractions(securityService, roleService, commonNasnavUserRepo, commonUserRepo, userAddressRepo);
	}

	@Test
	public void testGetYeshteryUserData_Success() throws BusinessException {
		UserEntity user = new UserEntity();
		user.setId(2L);
		UserRepresentationObject userRep = new UserRepresentationObject();
		userRep.setId(2L);

		when(securityService.getCurrentUser()).thenReturn(currentUser);
		when(securityService.currentUserIsCustomer()).thenReturn(false);
		when(roleService.getEmployeeHighestRole(currentUser.getId())).thenReturn(Roles.ORGANIZATION_ADMIN);
		when(commonNasnavUserRepo.findByIdAndOrganizationId(2L, 99002L, true)).thenReturn(Optional.of(user));
		mockUserRepresentation(user);

		UserRepresentationObject result = yeshteryUserService.getYeshteryUserData(2L, true);

		Assertions.assertNotNull(result);
		verify(securityService).getCurrentUser();
		verify(securityService).currentUserIsCustomer();
		verify(roleService).getEmployeeHighestRole(currentUser.getId());
		verify(commonNasnavUserRepo).findByIdAndOrganizationId(2L, 99002L, true);
		verify(roleService).getUserRoles(user);
		verify(userAddressRepo).findByUser_Id(userRep.getId());
	}

	private void mockUserRepresentation(UserEntity user) {
		UserRepresentationObject userRep = user.getRepresentation();
		List<UserAddressEntity> addresses = new ArrayList<>();
		List<String> roles = List.of(Roles.NASNAV_ADMIN.name());

		when(userAddressRepo.findByUser_Id(userRep.getId())).thenReturn(addresses);
		when(roleService.getUserRoles(user)).thenReturn(roles);
		when(securityService.getLastLoginForUser(user)).thenReturn(LocalDateTime.now());

		userRep.setAddresses(new ArrayList<>());
		userRep.setRoles(Set.of(Roles.NASNAV_ADMIN.name()));
		userRep.setIsInfluencer(false);
		userRep.setLastLogin(LocalDateTime.now());
		userRep.setDateOfBirth(LocalDateTime.now());
	}
}
