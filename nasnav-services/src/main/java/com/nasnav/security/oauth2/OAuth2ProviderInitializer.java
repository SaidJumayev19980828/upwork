package com.nasnav.security.oauth2;

import com.nasnav.dao.OAuth2ProviderRepository;
import com.nasnav.persistence.OAuth2ProviderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class OAuth2ProviderInitializer {
	
	@Autowired
	OAuthProperties oAuthProps;
	
	
	@Autowired
	OAuth2ProviderRepository providerRepo;

	@PostConstruct
	public void insertOAuthMissingProviders() {
		oAuthProps.getRegistration()
				  .keySet()
				  .stream()				  
				  .map(this::firstPropertyPath)
				  .distinct()
				  .filter( provider -> !providerRepo.existsByProviderNameIgnoreCase(provider))
				  .map(OAuth2ProviderEntity::new)
				  .forEach(providerRepo::save); 
	}
	
	
	
	
	private String firstPropertyPath(String prop) {
		String path = prop.substring(0, prop.indexOf("."));
		return path;
	}
}
