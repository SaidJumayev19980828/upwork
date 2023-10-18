package com.nasnav.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nasnav.dao.RocketChatCustomerTokenRepository;
import com.nasnav.dto.rocketchat.RocketChatVisitorDTO;
import com.nasnav.persistence.RocketChatCustomerTokenEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.SecurityService;
import com.nasnav.service.rocketchat.CustomerRocketChatService;
import com.nasnav.service.rocketchat.DepartmentRocketChatService;
import com.nasnav.service.rocketchat.impl.CustomerRocketChatServiceImpl;
import com.nasnav.service.rocketchat.impl.RocketChatClient;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class CustomerRocketChatServiceTest {
	private static final String TOKEN = "some-token";
	private static final String DEPARTMENT_ID = "TEST-ID";

	@Mock
	SecurityService securityService;

	@Mock
	RocketChatCustomerTokenRepository tokenRepository;

	@Mock
	RocketChatClient rocketChatClient;

	@Mock
	DepartmentRocketChatService departmentRocketChatService;

	CustomerRocketChatService customerRocketChatService;

	UserEntity user;

	@BeforeEach
	void reinit() {
		customerRocketChatService = new CustomerRocketChatServiceImpl(tokenRepository, securityService,
				rocketChatClient, departmentRocketChatService);
		user = createUser();
		Mockito.when(departmentRocketChatService.getDepartmentIdCreateDepartmentIfNeeded(user.getOrganizationId())).thenReturn(Mono.just(DEPARTMENT_ID));
	}

	@Test
	void cusomerDataAlreadySaved() {
		RocketChatCustomerTokenEntity tokenEntity = createRocketChatToken(user);
		RocketChatVisitorDTO visitor = createRocketChatVisitorDTO(user);
		Mockito.when(securityService.getCurrentUser()).thenReturn(user);
		Mockito.when(tokenRepository.findByUser(user)).thenReturn(Optional.of(tokenEntity));
		Mockito.when(rocketChatClient.liveChatInit(TOKEN, DEPARTMENT_ID)).thenReturn(Mono.just(visitor));
		RocketChatVisitorDTO returnedVisitor = customerRocketChatService.getRocketChatVisitorData().block();
		assertEquals("returned visitor from api is changed", visitor, returnedVisitor);
		Mockito.verify(rocketChatClient).liveChatInit(TOKEN, DEPARTMENT_ID);
	}

	@Test
	void customerHasNoToken() {
		RocketChatCustomerTokenEntity tokenEntity = createRocketChatToken(user);
		RocketChatVisitorDTO visitor = createRocketChatVisitorDTO(user);
		Mockito.when(securityService.getCurrentUser()).thenReturn(user);
		Mockito.when(tokenRepository.findByUser(user)).thenReturn(Optional.empty());
		Mockito.when(tokenRepository.save(any(RocketChatCustomerTokenEntity.class))).thenReturn(tokenEntity);
		Mockito.when(rocketChatClient.liveChatInit(TOKEN, DEPARTMENT_ID)).thenReturn(Mono.empty());
		Mockito.when(rocketChatClient.liveChatRegisterVisitor(visitor)).thenReturn(Mono.just(visitor));
		RocketChatVisitorDTO returnedVisitor = customerRocketChatService.getRocketChatVisitorData().block();
		assertEquals("returned visitor from api is changed", visitor, returnedVisitor);
		Mockito.verify(rocketChatClient).liveChatInit(TOKEN, DEPARTMENT_ID);
		Mockito.verify(rocketChatClient).liveChatRegisterVisitor(visitor);
	}

	@Test
	void userDataChanged() {
		UserEntity newUser = createUser();
		RocketChatCustomerTokenEntity tokenEntity = createRocketChatToken(newUser);
		RocketChatVisitorDTO visitor = createRocketChatVisitorDTO(user);
		newUser.setName("new name");
		newUser.setEmail("new@email.co");
		Mockito.when(securityService.getCurrentUser()).thenReturn(newUser);
		Mockito.when(tokenRepository.findByUser(newUser)).thenReturn(Optional.of(tokenEntity));
		Mockito.when(rocketChatClient.liveChatInit(TOKEN, DEPARTMENT_ID)).thenReturn(Mono.just(visitor));
		RocketChatVisitorDTO newVisitor = createRocketChatVisitorDTO(newUser);
		Mockito.when(rocketChatClient.liveChatRegisterVisitor(newVisitor)).thenReturn(Mono.just(newVisitor));
		RocketChatVisitorDTO returnedVisitor = customerRocketChatService.getRocketChatVisitorData().block();
		assertEquals("returned visitor from api is changed", newVisitor, returnedVisitor);
		Mockito.verify(rocketChatClient).liveChatInit(TOKEN, DEPARTMENT_ID);
		Mockito.verify(rocketChatClient).liveChatRegisterVisitor(newVisitor);
	}

	private UserEntity createUser() {
		UserEntity user = new UserEntity();
		user.setId(85L);
		user.setName("test user");
		user.setEmail("test@email.co");
		user.setOrganizationId(99001L);
		return user;
	}

	private RocketChatCustomerTokenEntity createRocketChatToken(UserEntity user) {
		RocketChatCustomerTokenEntity tokenEntity = new RocketChatCustomerTokenEntity(TOKEN);
		tokenEntity.setUser(user);
		return tokenEntity;
	}

	private RocketChatVisitorDTO createRocketChatVisitorDTO(UserEntity user) {
		return RocketChatVisitorDTO
				.builder()
				.name(user.getName())
				.email(user.getEmail())
				.token(TOKEN)
				.department(DEPARTMENT_ID)
				.orgId(user.getOrganizationId().toString())
				.userId(user.getId().toString())
				.build();
	}
}
