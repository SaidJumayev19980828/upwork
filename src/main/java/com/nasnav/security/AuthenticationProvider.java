package com.nasnav.security;

import com.nasnav.dao.CommonUserRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


import static java.util.Optional.*;

@Component
public class AuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

 @Autowired
 SecurityService securityService;
 
 
 @Autowired
 private CommonUserRepository  userRepo;
 

 @Override
 protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
	 Object token = usernamePasswordAuthenticationToken.getCredentials();
	 BaseUserEntity userEntity =  ofNullable(token)
								   .map(String::valueOf)
								   .flatMap(securityService::getUserByAuthenticationToken)
								   .orElseThrow(() -> new UsernameNotFoundException("Cannot find user with authentication token=" + token));

	 usernamePasswordAuthenticationToken.setDetails(userEntity); 
 }
 
 
 
 
 

 @Override
 protected UserDetails retrieveUser(String userName, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
	
	  Object token = usernamePasswordAuthenticationToken.getCredentials();
	  return ofNullable(token)
			   .map(String::valueOf)
			   .flatMap(securityService::findUserByAuthToken)
			   .orElseThrow(() -> new UsernameNotFoundException("Cannot find user with authentication token=" + token));
 }
 
 
}
