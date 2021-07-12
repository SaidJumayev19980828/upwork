package com.nasnav.security;

import com.nasnav.service.SecurityService;
import com.nasnav.service.model.security.UserAuthenticationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;

@Component
public class AuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	 @Autowired
	 SecurityService securityService;
	 
	 
	
	 @Override
	 protected UserDetails retrieveUser(String userName, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
		
		  Object token = usernamePasswordAuthenticationToken.getCredentials();
		  return ofNullable(token)
				   .map(String::valueOf)
				   .flatMap(securityService::findUserDetailsByAuthToken)
				   .map(this::extendTokenExpirationIfNeeded)
				   .map(authData -> addUserEntityToTheSecurityContext(authData, usernamePasswordAuthenticationToken))
				   .map(UserAuthenticationData::getUserDetails)
				   .orElseThrow(() -> noValidTokenFoundException());
	 }
	 
	 
	 
	
	 @Override
	 protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
	
	 }
	
	 
	 
	 private UserAuthenticationData extendTokenExpirationIfNeeded(UserAuthenticationData userData) {
		 securityService.extendUserExpirationTokenIfNeeded(userData.getToken());
		 return userData;
	 }
	
	
	
	
	private UsernameNotFoundException noValidTokenFoundException() {
		return new UsernameNotFoundException("Cannot find user with given authentication token!");
	}
 
 
 
	 private UserAuthenticationData addUserEntityToTheSecurityContext(UserAuthenticationData authData, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) {
		 usernamePasswordAuthenticationToken.setDetails(authData.getUserEntity()); 
		 return authData;
	 }
 
 
}
