package com.nasnav.service.rocketchat.impl;

import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.nasnav.dao.RocketChatCustomerTokenRepository;
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
	private final SecurityService securityService;
	private final RocketChatClient rocketChatClient;
	private final DepartmentRocketChatService departmentService;

	@Transactional
	@Override
	public Mono<RocketChatVisitorDTO> getRocketChatVisitorData() {
		return getRocketChatVisitorDataForUser((UserEntity)securityService.getCurrentUser());
	}

	@Transactional
	@Override
	public Mono<RocketChatVisitorDTO> getRocketChatVisitorData(Long orgId) {
		return getRocketChatVisitorDataForUser((UserEntity)securityService.getCurrentUserForOrg(orgId));
	}

	private Mono<RocketChatVisitorDTO> getRocketChatVisitorDataForUser(UserEntity user) {
		final RocketChatCustomerTokenEntity tokenEntity = getOrCreateTokenEntity(user);

		final String token = tokenEntity.getToken();

		Long orgId = user.getOrganizationId();

		return departmentService.getDepartmentIdCreateDepartmentIfNeeded(orgId)
				.flatMap(departmentId -> rocketChatClient.liveChatInit(token, departmentId)
						.filter(visitor -> areVisitorDataValid(visitor, user))
						.switchIfEmpty(Mono.defer(() -> registerNewVisitor(user, token, departmentId))))
				.onErrorResume(WebClientResponseException.class,
						e -> Mono.error(new RuntimeBusinessException(
								HttpStatus.NOT_ACCEPTABLE,
								ErrorCodes.CHAT$EXTERNAL,
								e.getStatusCode().value())));
	}

	private Mono<RocketChatVisitorDTO> registerNewVisitor(UserEntity user, String token, String departmentId) {
		RocketChatVisitorDTO visitor = RocketChatVisitorDTO
				.builder()
				.name(user.getName())
				.email(user.getEmail())
				.department(departmentId)
				.orgId(user.getOrganizationId().toString())
				.userId(user.getId().toString())
				.token(token)
				.build();
		return rocketChatClient.liveChatRegisterVisitor(visitor);
	}

	private RocketChatCustomerTokenEntity getOrCreateTokenEntity(UserEntity user) {
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
