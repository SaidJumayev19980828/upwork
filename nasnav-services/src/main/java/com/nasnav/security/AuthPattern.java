package com.nasnav.security;

import com.nasnav.enumerations.Roles;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpMethod;

import java.util.Set;

@Data
@AllArgsConstructor
public class AuthPattern {
	private String urlPattern;
	private HttpMethod httpMethod; 
	private Set<Roles> roles;
}
