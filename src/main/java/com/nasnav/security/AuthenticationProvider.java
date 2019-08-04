package com.nasnav.security;

import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.nasnav.dao.BaseUserRepository;

//@Component
public class AuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

 @Autowired
 BaseUserRepository commonUserRepo;

 @Override
 protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
  //
 }
 
 

 @Override
 protected UserDetails retrieveUser(String userName, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
	
//	  Object token = usernamePasswordAuthenticationToken.getCredentials();
//	  return Optional
//			   .ofNullable(token)
//			   .map(String::valueOf)
//			   .flatMap(commonUserRepo::findByToken)
//			   .orElseThrow(() -> new UsernameNotFoundException("Cannot find user with authentication token=" + token));
	 return null;
 }
 
 
}
