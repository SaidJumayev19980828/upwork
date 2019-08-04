package com.nasnav.service;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public interface SecurityService {
	
	Optional<UserDetails> findUserByAuthToken(String token);

}
