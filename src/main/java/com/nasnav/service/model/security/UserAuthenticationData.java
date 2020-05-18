package com.nasnav.service.model.security;

import org.springframework.security.core.userdetails.UserDetails;

import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.UserTokensEntity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserAuthenticationData {
	private UserDetails userDetails;
	private BaseUserEntity userEntity;
	private UserTokensEntity token;
}
