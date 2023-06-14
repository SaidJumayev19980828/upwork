package com.nasnav.security;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.nasnav.persistence.UserEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserAuditorAware implements AuditorAware<UserEntity> {

	@Override
	public Optional<UserEntity> getCurrentAuditor() {
		Object securityDetails = SecurityContextHolder.getContext().getAuthentication().getDetails();
		UserEntity castedUser = securityDetails instanceof UserEntity ? (UserEntity) securityDetails : null;
		return Optional.ofNullable(castedUser);
	}
}
