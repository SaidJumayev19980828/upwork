package com.nasnav.security.oauth2;
import static com.nasnav.commons.utils.StringUtils.generateUUIDToken;
import static com.nasnav.security.oauth2.OAuth2AuthorizationRequestRepository.ORG_ID_COOKIE_NAME;
import static com.nasnav.security.oauth2.OAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;
import static java.lang.String.format;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.nasnav.dao.OAuth2UserRepository;
import com.nasnav.persistence.OAuth2UserEntity;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {



	@Autowired
    private AuthorizationRequestRepository<OAuth2AuthorizationRequest> requestRepository;

	
	@Autowired
	private OAuth2UserRepository oAuthUserRepo;
	
	
	@Autowired
	private OAuth2Helper helper;

	
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
    	 
        System.out.println("On Success !");
        System.out.println("On Success - request :  "   + request);
        System.out.println("On Success - response :  "   + response.getHeaderNames());
        
        
        String token = generateOAuth2Token();        
        
        UserPrincipal user = (UserPrincipal)authentication.getPrincipal();
        Long orgId = CookieUtils.getCookie(request, ORG_ID_COOKIE_NAME)
        							.map(Cookie::getValue)
        							.map(this::getOrgIdAsLongVal)
					                .orElseThrow(() -> getNoOrgProvidedException(user) );
        
        System.out.println("user at onSuccess : " + user);
        
        String oAuth2Id = user.getId(); 
        oAuthUserRepo.findByOAuth2IdAndOrganizationId(oAuth2Id , orgId)
					.map(usr -> saveTokenToDB(usr, token) )
				 	.orElse(helper.registerNewOAuth2User(user, token, orgId));

    	String targetUrl = determineTargetUrl(request, token);        
        
        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);              
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
    
    
    
    
    











    
    
  


	private Long getOrgIdAsLongVal(String orgIdStr) {
    	try {
    		return Long.valueOf(orgIdStr);
    	}catch(Exception e) {
    		throw new IllegalStateException(format("Invalid Organization Id value [%s]", orgIdStr));
    	}
    	
    }
    
    

	private RuntimeException getNoOrgProvidedException(UserPrincipal user) {
    	return new IllegalStateException(
						format("No Organization id provided for auth. of user[%s] for provider[%s]"
									, user.getId(), user.getProvider()));
    }
    
    
    

    protected String determineTargetUrl(HttpServletRequest request, String token) {
    	UriComponents redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
    												.map(Cookie::getValue)
        											.map(UriComponentsBuilder::fromUriString)
        											.map(UriComponentsBuilder::build)
        											.orElseGet(this::getDefaultRedirectUri);
        if(redirectUri.getHost() != null) {
            throw new IllegalStateException("Invalid redirect URL : " + redirectUri);
        }

        return UriComponentsBuilder
        			.fromUriString(redirectUri.toUriString())
	                .queryParam("token", token)
	                .build()
	                .toUriString();
    }




	private OAuth2UserEntity saveTokenToDB(OAuth2UserEntity oAuthUser, String token) {
		oAuthUser.setLoginToken(token);
		return oAuthUserRepo.save(oAuthUser);
	}




	private String generateOAuth2Token() {
		return generateUUIDToken();
	}




	private UriComponents getDefaultRedirectUri() {
		return UriComponentsBuilder.fromUriString(getDefaultTargetUrl()).build();
	}
    
    
    

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        requestRepository.removeAuthorizationRequest(request, response);
    }

    
    
    
    
    private boolean isAuthorizedRedirectUri(String uri) {
    	return true;
//        URI clientRedirectUri = URI.create(uri);
//
//        return appProperties.getOauth2().getAuthorizedRedirectUris()
//                .stream()
//                .anyMatch(authorizedRedirectUri -> {
//                    // Only validate host and port. Let the clients use different paths if they want to
//                    URI authorizedURI = URI.create(authorizedRedirectUri);
//                    if(authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
//                            && authorizedURI.getPort() == clientRedirectUri.getPort()) {
//                        return true;
//                    }
//                    return false;
//                });
    }
}