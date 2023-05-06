package com.nasnav.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nasnav.dao.RocketChatCustomerTokenRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.rocketchat.RocketChatVisitorDTO;
import com.nasnav.persistence.RocketChatCustomerTokenEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.SecurityService;
import com.nasnav.service.rocketchat.CustomerRocketChatService;
import com.nasnav.service.rocketchat.impl.CustomerRocketChatServiceImpl;
import com.nasnav.service.rocketchat.impl.RocketChatClient;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class CustomerRocketChatServiceTest {
	private static final String TOKEN = "some-token";

	@Mock
	UserRepository userRepository;

	@Mock
	SecurityService securityService;

	@Mock
	RocketChatCustomerTokenRepository tokenRepository;

	@Mock
	RocketChatClient rocketChatClient;

	CustomerRocketChatService customerRocketChatService;

	UserEntity user;

	@BeforeEach
	void reinit() {
		customerRocketChatService = new CustomerRocketChatServiceImpl(tokenRepository, userRepository, securityService, rocketChatClient);
		user = createUser();
	}

	@Test
	void cusomerDataAlreadySaved() {
		RocketChatCustomerTokenEntity tokenEntity = createRocketChatToken(user);
		RocketChatVisitorDTO visitor = createRocketChatVisitorDTO(user);
		Mockito.when(securityService.getCurrentUser()).thenReturn(user);
		Mockito.when(tokenRepository.findByUserEntity(user)).thenReturn(Optional.of(tokenEntity));
		Mockito.when(rocketChatClient.liveChatInit(TOKEN, user.getOrganizationId().toString())).thenReturn(Mono.just(visitor));
		RocketChatVisitorDTO returnedVisitor = customerRocketChatService.getRocketChatVisitorData().block();
		assertEquals("returned visitor from api is changed", visitor, returnedVisitor);
		Mockito.verify(rocketChatClient).liveChatInit(TOKEN, user.getOrganizationId().toString());
		Mockito.verifyNoMoreInteractions(userRepository, securityService, tokenRepository, rocketChatClient);
	}

	@Test
	void customerHasNoToken() {
		RocketChatCustomerTokenEntity tokenEntity = createRocketChatToken(user);
		RocketChatVisitorDTO visitor = createRocketChatVisitorDTO(user);
		Mockito.when(securityService.getCurrentUser()).thenReturn(user);
		Mockito.when(tokenRepository.findByUserEntity(user)).thenReturn(Optional.empty());
		Mockito.when(tokenRepository.save(any(RocketChatCustomerTokenEntity.class))).thenReturn(tokenEntity);
		Mockito.when(userRepository.save(user)).thenReturn(user);
		Mockito.when(rocketChatClient.liveChatInit(TOKEN, user.getOrganizationId().toString())).thenReturn(Mono.empty());
		Mockito.when(rocketChatClient.liveChatRegisterVisitor(visitor)).thenReturn(Mono.just(visitor));
		RocketChatVisitorDTO returnedVisitor = customerRocketChatService.getRocketChatVisitorData().block();
		assertEquals("returned visitor from api is changed", visitor, returnedVisitor);
		Mockito.verify(rocketChatClient).liveChatInit(TOKEN, user.getOrganizationId().toString());
		Mockito.verify(rocketChatClient).liveChatRegisterVisitor(visitor);
		Mockito.verifyNoMoreInteractions(userRepository, securityService, tokenRepository, rocketChatClient);
	}

	@Test
	void userDataChanged() {
		UserEntity newUser = createUser();
		RocketChatCustomerTokenEntity tokenEntity = createRocketChatToken(newUser);
		RocketChatVisitorDTO visitor = createRocketChatVisitorDTO(user);
		newUser.setName("new name");
		newUser.setEmail("new@email.co");
		Mockito.when(securityService.getCurrentUser()).thenReturn(newUser);
		Mockito.when(tokenRepository.findByUserEntity(newUser)).thenReturn(Optional.of(tokenEntity));
		Mockito.when(rocketChatClient.liveChatInit(TOKEN, newUser.getOrganizationId().toString())).thenReturn(Mono.just(visitor));
		RocketChatVisitorDTO newVisitor = createRocketChatVisitorDTO(newUser);
		Mockito.when(rocketChatClient.liveChatRegisterVisitor(newVisitor)).thenReturn(Mono.just(newVisitor));
		RocketChatVisitorDTO returnedVisitor = customerRocketChatService.getRocketChatVisitorData().block();
		assertEquals("returned visitor from api is changed", newVisitor, returnedVisitor);
		Mockito.verify(rocketChatClient).liveChatInit(TOKEN, user.getOrganizationId().toString());
		Mockito.verify(rocketChatClient).liveChatRegisterVisitor(newVisitor);
		Mockito.verifyNoMoreInteractions(userRepository, securityService, tokenRepository, rocketChatClient);
	}

	private UserEntity createUser() {
		UserEntity user = new UserEntity();
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
				.build();
	}
}
