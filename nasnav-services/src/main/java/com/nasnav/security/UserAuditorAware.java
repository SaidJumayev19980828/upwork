package com.nasnav.security;

import com.nasnav.persistence.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserAuditorAware implements AuditorAware<UserEntity> {

	@Override
	public Optional<UserEntity> getCurrentAuditor() {
		//Fixme : handle JWT case ?
		Object securityDetails = SecurityContextHolder.getContext().getAuthentication().getDetails();
		UserEntity castedUser = securityDetails instanceof UserEntity userEntity ? userEntity : null;
		return Optional.ofNullable(castedUser);
	}
}
