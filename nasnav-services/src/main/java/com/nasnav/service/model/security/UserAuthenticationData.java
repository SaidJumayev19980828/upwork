package com.nasnav.service.model.security;

import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.UserTokensEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@AllArgsConstructor
public class UserAuthenticationData {
	private UserDetails userDetails;
	private BaseUserEntity userEntity;
	private UserTokensEntity token;
}
