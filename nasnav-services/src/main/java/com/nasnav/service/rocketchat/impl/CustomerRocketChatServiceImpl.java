package com.nasnav.service.rocketchat.impl;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.nasnav.dao.RocketChatCustomerTokenRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.rocketchat.RocketChatVisitorDTO;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.RocketChatCustomerTokenEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.SecurityService;
import com.nasnav.service.rocketchat.CustomerRocketChatService;
import com.nasnav.service.rocketchat.DepartmentRocketChatService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomerRocketChatServiceImpl implements CustomerRocketChatService {
	private final RocketChatCustomerTokenRepository rocketChatUserTokenRepository;
	private final UserRepository userRepository;
	private final SecurityService securityService;
	private final RocketChatClient rocketChatClient;
	private final DepartmentRocketChatService departmentService;

	@Override
	public Mono<RocketChatVisitorDTO> getRocketChatVisitorData() {
		final RocketChatCustomerTokenEntity tokenEntity = getOrCreateTokenEntity();

		final UserEntity user = tokenEntity.getUser();

		final String token = tokenEntity.getToken();

		Long orgId = user.getOrganizationId();

		return departmentService.getDepartmentIdCreateDepartmentIfNeeded(orgId).flatMap(departmentId -> rocketChatClient
				.liveChatInit(token, departmentId))
				.filter(visitor -> areVisitorDataValid(visitor, user))
				.switchIfEmpty(Mono.defer(() -> registerNewVisitor(user, token)))
				.onErrorResume(WebClientResponseException.class,
						e -> Mono.error(new RuntimeBusinessException(
								HttpStatus.NOT_ACCEPTABLE, 
								ErrorCodes.CHAT$CUSTOMER$EXTERNAL,
								e.getStatusCode().value()
						)));
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
				.findByUser(user)
				.orElseGet(() -> new RocketChatCustomerTokenEntity(UUID.randomUUID().toString()));

		if (tokenEntity.getUser() == null) {
			tokenEntity.setUser(user);
			tokenEntity = rocketChatUserTokenRepository.save(tokenEntity);
		}

		return tokenEntity;
	}

	private boolean areVisitorDataValid(RocketChatVisitorDTO visitor, UserEntity user) {
		return visitor != null && visitor.getName().equals(user.getName())
				&& visitor.getEmail().equals(user.getEmail());
	}
}
