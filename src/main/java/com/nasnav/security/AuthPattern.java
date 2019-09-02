package com.nasnav.security;

import java.util.Set;

import org.springframework.http.HttpMethod;

import com.nasnav.enumerations.Roles;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthPattern {
	private String urlPattern;
	private HttpMethod httpMethod; 
	private Set<Roles> roles;
}
