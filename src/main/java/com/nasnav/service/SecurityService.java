package com.nasnav.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface SecurityService {
	
	Optional<UserDetails> findUserByAuthToken(String token);

}
