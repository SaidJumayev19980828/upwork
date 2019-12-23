package com.nasnav.security.oauth2;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client" )
public class OAuthProperties {
	private Map<String,String> registration = new HashMap<>();
}
