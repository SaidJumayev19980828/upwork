package com.nasnav.yeshtery.security.jwt;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface JwtUserDetailsService {

    UserDetails loadUser(JwtLoginData loginData) throws UsernameNotFoundException;

}
