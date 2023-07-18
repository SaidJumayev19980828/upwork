package com.nasnav.yeshtery.test.services;

import static org.junit.Assert.assertEquals;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nasnav.dao.UserRepository;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.yeshtery.YeshteryUserService;
import com.nasnav.service.yeshtery.YeshteryUserServiceImpl;

@ExtendWith(MockitoExtension.class)
class YeshteryUserServiceTest {
	@Mock
	private UserRepository nasnavUserRepository;

	@InjectMocks
	private YeshteryUserService yeshteryUserService = new YeshteryUserServiceImpl();

	private static Stream<Arguments> generator() {
		UserEntity user = new UserEntity();
		user.setOrganizationId(99002L);
		user.setYeshteryUserId(5L);

		return Stream.of(
				Arguments.of(user));
	}

	@ParameterizedTest
	@MethodSource("generator")
	@NullSource
	void testGetUserForOrg(UserEntity repoUser) {
		UserEntity user = new UserEntity();
		user.setOrganizationId(99001L);
		user.setYeshteryUserId(5L);

		Mockito.when(nasnavUserRepository.findByYeshteryUserIdAndOrganizationId(user.getYeshteryUserId(),
				99002L))
				.thenReturn(Optional.ofNullable(repoUser));

		UserEntity returnedUser = yeshteryUserService.getUserForOrg(user, 99002L);
		assertEquals(repoUser, returnedUser);

		Mockito.verifyNoMoreInteractions(nasnavUserRepository);
	}
}
