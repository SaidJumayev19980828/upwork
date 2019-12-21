package com.nasnav.security.oauth2;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.SecurityService;

@Service
public class CustomOidcUserService extends OidcUserService {
	@Autowired
	SecurityService securityService;
	

	@Override
    public OidcUser loadUser(OidcUserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
		System.out.println("in oid oauth in service : " + oAuth2UserRequest);
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        
        
        System.out.println("OAuth USer req : " + oAuth2UserRequest.getClientRegistration().getRegistrationId());
        System.out.println("OAuth2 user : " + oAuth2User);
        System.out.println("OAuth USer name attr name : "+ oAuth2UserRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName());
        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
	}
	
	
	
	
	
	
	private OidcUser processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
//        if(StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
//            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
//        }
        
        oAuth2UserRequest.getClientRegistration().getRegistrationId();
        
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("nasnav_happy_user@nasnav.com");
        userEntity.setEncryptedPassword("12345");
        UserDetails user = getUser(userEntity).get();
        
        return UserPrincipal.create(user, oAuth2User.getAttributes());
	}
	
	
	
	private Optional<UserDetails> getUser(BaseUserEntity userEntity) {		
		List<GrantedAuthority> roles = getUserRoles(userEntity);
		User user= new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), true, true, true, true,roles);
        return Optional.of(user);		
	}





	private List<GrantedAuthority> getUserRoles(BaseUserEntity userEntity) {
		
		return Arrays.asList("ROLE_USER")
					.stream()												
					.map(SimpleGrantedAuthority::new)
					.collect(Collectors.toList());
	}
}
