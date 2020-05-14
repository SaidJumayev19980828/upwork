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

import java.util.Optional;

@Component
public class AuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

 @Autowired
 SecurityService securityService;
 
 
 @Autowired
 private CommonUserRepository  userRepo;
 

 @Override
 protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
	 //TODO: >>> you need to change AuthenticationFilter class as well, which extracts the token from the headers. 
	 //it should extract the token from the cookie instead.
	 //TODO: >>> all of our tests will need changes, tokens will need to be added into the new tables instead of user tables.
	 //TODO: >>> TestCommons.httpEntity method should be changed to add the token to a cookie instead of User-token.
	 //TODO: >>> a full test run will be needed as well.
	 Object token = usernamePasswordAuthenticationToken.getCredentials();
	 BaseUserEntity userEntity =  Optional
									   .ofNullable(token)		//TODO: >>> use static imports if possible as it is cleaner
									   .map(String::valueOf)
									   //TODO >>> commented code is a dead code, dead code is not a clean code.
									   .flatMap(securityService::getUserByAuthenticationToken)//userRepo::findByAuthenticationToken)
									   .orElseThrow(() -> new UsernameNotFoundException("Cannot find user with authentication token=" + token));

	 usernamePasswordAuthenticationToken.setDetails(userEntity); 
 }
 
 
 
 
 

 @Override
 protected UserDetails retrieveUser(String userName, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
	
	  Object token = usernamePasswordAuthenticationToken.getCredentials();
	  return Optional
			   .ofNullable(token)
			   .map(String::valueOf)
			   .flatMap(securityService::findUserByAuthToken)
			   .orElseThrow(() -> new UsernameNotFoundException("Cannot find user with authentication token=" + token));
 }
 
 
}
