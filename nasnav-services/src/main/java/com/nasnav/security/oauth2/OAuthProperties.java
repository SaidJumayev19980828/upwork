package com.nasnav.security.oauth2;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client" )
public class OAuthProperties {
	private Map<String,String> registration = new HashMap<>();
}
