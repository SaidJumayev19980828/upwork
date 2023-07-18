package com.nasnav.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.nasnav.AppConfig;
import com.nasnav.dao.CommonUserRepository;
import com.nasnav.dao.OAuth2UserRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.UserTokenRepository;
import com.nasnav.enumerations.Roles;
import com.nasnav.enumerations.YeshteryState;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.SecurityService;
import com.nasnav.service.helpers.UserServicesHelper;
import com.nasnav.service.impl.SecurityServiceImpl;
import com.nasnav.service.yeshtery.YeshteryUserService;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {
	@Mock
	private CommonUserRepository userRepo;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private OrganizationRepository orgRepo;
	@Mock
	private ShopsRepository shopsRepo;
	@Mock
	private OAuth2UserRepository oAuthUserRepo;
	@Mock
	private YeshteryUserService yeshteryUserService;
	@Mock
	private UserTokenRepository userTokenRepo;
	@Mock
	private AppConfig config;
	@Mock
	private UserServicesHelper helper;

	@InjectMocks
	private SecurityService securityService = new SecurityServiceImpl();

	@Mock
	private Authentication authentication;
	@Mock
	private SecurityContext securityContext;

	@BeforeEach
	public void init() {
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}

	private static Stream<Arguments> sameOrgGenerator() {
		return Stream.of(
				Arguments.of(false, YeshteryState.DISABLED, true),
				Arguments.of(false, YeshteryState.ACTIVE, true),
				Arguments.of(true, YeshteryState.DISABLED, false),
				Arguments.of(true, YeshteryState.ACTIVE, true));
	}

	@ParameterizedTest
	@MethodSource("sameOrgGenerator")
	void shopOfSameOrgAccessability(boolean isYeshteryInstance, YeshteryState expectedResult,
			boolean shouldBeAccessible) {
		injectAppConfigIfNeeded(isYeshteryInstance);
		boolean result = sameOrgShop(expectedResult);
		assertEquals(shouldBeAccessible, result);
	}

	private static Stream<Arguments> differentOrgGenerator() {
		return IntStream.of(8).boxed()
				.map(i -> Arguments.of((i & 4) == 4, toYeshteryState((i & 2) == 2), toYeshteryState((i & 1) == 1),
						i == 7));
	}

	@ParameterizedTest
	@MethodSource("differentOrgGenerator")
	void shopOfDifferentOrgAccessiblity(boolean isYeshteryInstance, YeshteryState shopOrgYeshteryState,
			YeshteryState userOrgYeshteryState,
			boolean expectedResult) {
		injectAppConfigIfNeeded(isYeshteryInstance);
		boolean result = differentOrgShop(shopOrgYeshteryState, userOrgYeshteryState);
		assertEquals(expectedResult, result);
	}

	@Test
	void SameOrgYeshteryStateActiveEmployeeYeshtery() {
		injectAppConfigIfNeeded(true);
		OrganizationEntity shopOrg = createMockOrg(99001L, YeshteryState.ACTIVE);
		OrganizationEntity userOrg = createMockOrg(99002L, YeshteryState.ACTIVE);
		Mockito.when(orgRepo.findById(userOrg.getId())).thenReturn(Optional.of(userOrg));
		Mockito.when(orgRepo.findById(shopOrg.getId())).thenReturn(Optional.of(shopOrg));
		createMockShop(99901L, shopOrg);
		EmployeeUserEntity employee = creaEmployeeUser(userOrg);
		Mockito.when(authentication.getDetails()).thenReturn(employee);

		Collection<SimpleGrantedAuthority> authorities = List
				.of(new SimpleGrantedAuthority(Roles.ORGANIZATION_MANAGER.getValue()));
		Mockito.doReturn(authorities).when(authentication).getAuthorities();

		boolean result = securityService.isShopAccessibleToCurrentUser(99901L);
		assertFalse("an employee shouldn't be allowed to access another org", result);

		employee.setOrganizationId(shopOrg.getId());
		result = securityService.isShopAccessibleToCurrentUser(99901L);
		assertTrue(result);

		authorities = List.of(new SimpleGrantedAuthority(Roles.STORE_MANAGER.getValue()));
		Mockito.doReturn(authorities).when(authentication).getAuthorities();

		employee.setShopId(99901L);
		result = securityService.isShopAccessibleToCurrentUser(99901L);
		assertTrue(result);

		employee.setShopId(99902L);
		result = securityService.isShopAccessibleToCurrentUser(99901L);
		assertFalse(result);
	}

	private static Stream<Arguments> generator() {
		UserEntity authUser = new UserEntity();
		authUser.setOrganizationId(99001L);
		authUser.setYeshteryUserId(5L);

		UserEntity userFromYeshteryService = new UserEntity();
		userFromYeshteryService.setOrganizationId(99002L);
		userFromYeshteryService.setYeshteryUserId(5L);

		return Stream.of(
				Arguments.of(false, 99001L, authUser, false, null, false, authUser),
				Arguments.of(false, 99002L, authUser, false, null, true, null),
				Arguments.of(true, 99001L, authUser, false, null, false, authUser),
				Arguments.of(true, 99002L, authUser, true, userFromYeshteryService, false, userFromYeshteryService),
				Arguments.of(true, 99003L, authUser, true, null, true, null));
	}

	@ParameterizedTest
	@MethodSource("generator")
	void getUserForOrg(boolean isYeshteryInstance, Long requiredOrg, UserEntity authUser, boolean expectYeshteryCall,
			UserEntity userFromYeshteryService, boolean shouldThrow, UserEntity expectedValue) {
		injectAppConfigIfNeeded(isYeshteryInstance);

		Mockito.when(authentication.getDetails()).thenReturn(authUser);

		if (expectYeshteryCall) {
			Mockito.when(yeshteryUserService.getUserForOrg(authUser, requiredOrg)).thenReturn(userFromYeshteryService);
		}

		AtomicReference<BaseUserEntity> returnedUser = new AtomicReference<>(null);
		Executable getUserForOrg = () -> {
			returnedUser.set(securityService.getCurrentUserForOrg(requiredOrg));
		};

		if (shouldThrow) {
			assertThrows(IllegalStateException.class, getUserForOrg);
		} else {
			assertDoesNotThrow(getUserForOrg);
			assertEquals(expectedValue, returnedUser.get());
		}
		Mockito.verifyNoMoreInteractions(yeshteryUserService);
	}

	private static YeshteryState toYeshteryState(boolean isActve) {
		return isActve ? YeshteryState.ACTIVE : YeshteryState.DISABLED;
	}

	private boolean differentOrgShop(YeshteryState shopOrgYeshteryState, YeshteryState userOrgYeshteryState) {
		OrganizationEntity shopOrg = createMockOrg(99001L, shopOrgYeshteryState);
		OrganizationEntity userOrg = createMockOrg(99002L, userOrgYeshteryState);
		Mockito.when(orgRepo.findById(userOrg.getId())).thenReturn(Optional.of(userOrg));
		createMockShop(99901L, shopOrg);
		UserEntity user = createUser(userOrg);
		Mockito.when(authentication.getDetails()).thenReturn(user);

		return securityService.isShopAccessibleToCurrentUser(99901L);
	}

	private Boolean sameOrgShop(YeshteryState yeshteryState) {
		OrganizationEntity org = createMockOrg(99001L, yeshteryState);
		Mockito.when(orgRepo.findById(org.getId())).thenReturn(Optional.of(org));
		createMockShop(99901L, org);
		UserEntity user = createUser(org);
		Mockito.when(authentication.getDetails()).thenReturn(user);

		return securityService.isShopAccessibleToCurrentUser(99901L);
	}

	private EmployeeUserEntity creaEmployeeUser(OrganizationEntity org) {
		EmployeeUserEntity employeeUser = new EmployeeUserEntity();
		employeeUser.setOrganizationId(org.getId());
		return employeeUser;
	}

	private UserEntity createUser(OrganizationEntity org) {
		UserEntity user = new UserEntity();
		user.setOrganizationId(org.getId());
		return user;
	}

	private void createMockShop(Long shopId, OrganizationEntity org) {
		ShopsEntity shop = new ShopsEntity();
		shop.setId(shopId);
		shop.setOrganizationEntity(org);
		Mockito.when(shopsRepo.findById(shopId)).thenReturn(Optional.of(shop));
	}

	private OrganizationEntity createMockOrg(Long orgId, YeshteryState yeshteryState) {
		OrganizationEntity org = new OrganizationEntity();
		org.setId(orgId);
		org.setYeshteryState(yeshteryState.getValue());
		return org;
	}

	private void injectAppConfigIfNeeded(boolean yeshteryInstance) {
		AppConfig config = new AppConfig(yeshteryInstance);
		ReflectionTestUtils.setField(securityService, "config", config);
	}
}
