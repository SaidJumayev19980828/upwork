package com.nasnav.service.rocketchat.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nasnav.dao.RocketChatCustomerTokenRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.rocketchat.RocketChatVisitorDTO;
import com.nasnav.persistence.RocketChatCustomerTokenEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.SecurityService;
import com.nasnav.service.rocketchat.CustomerRocketChatService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomerRocketChatServiceImpl implements CustomerRocketChatService {
	private final RocketChatCustomerTokenRepository rocketChatUserTokenRepository;
	private final UserRepository userRepository;
	private final SecurityService securityService;
	private final RocketChatClient rocketChatClient;

	@Override
	public Mono<RocketChatVisitorDTO> getRocketChatVisitorData() {
		final RocketChatCustomerTokenEntity tokenEntity = getOrCreateTokenEntity();

		final UserEntity user = tokenEntity.getUser();

		final String token = tokenEntity.getToken();

		return rocketChatClient
				.liveChatInit(token, user.getOrganizationId().toString())
				.filter(visitor -> areVisitorDataValid(visitor, user))
				.switchIfEmpty(Mono.defer(() -> registerNewVisitor(user, token)));
	}

	private Mono<RocketChatVisitorDTO> registerNewVisitor(UserEntity user, String token) {
		RocketChatVisitorDTO visitor = RocketChatVisitorDTO
				.builder()
				.name(user.getName())
				.email(user.getEmail())
				.token(token)
				.build();
		return rocketChatClient.liveChatRegisterVisitor(visitor);
	}

	private RocketChatCustomerTokenEntity getOrCreateTokenEntity() {
		UserEntity user = (UserEntity) securityService.getCurrentUser();
		RocketChatCustomerTokenEntity tokenEntity = rocketChatUserTokenRepository
				.findByUserEntity(user)
				.orElseGet(() -> new RocketChatCustomerTokenEntity(UUID.randomUUID().toString()));

		if (tokenEntity.getUser() == null) {
			tokenEntity.setUser(user);
			user.setRocketChatTokenEntity(tokenEntity);
			tokenEntity = rocketChatUserTokenRepository.save(tokenEntity);
			userRepository.save(user);
		}

		return tokenEntity;
	}

	private boolean areVisitorDataValid(RocketChatVisitorDTO visitor, UserEntity user) {
		return visitor != null && visitor.getName().equals(user.getName()) && visitor.getEmail().equals(user.getEmail());
	}
}
