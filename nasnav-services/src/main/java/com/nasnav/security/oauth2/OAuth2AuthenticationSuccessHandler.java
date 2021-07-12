package com.nasnav.security.oauth2;

import com.nasnav.dao.OAuth2UserRepository;
import com.nasnav.persistence.OAuth2UserEntity;
import com.nasnav.service.DomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import static com.nasnav.commons.utils.StringUtils.generateUUIDToken;
import static com.nasnav.security.oauth2.CookieUtils.getCookie;
import static com.nasnav.security.oauth2.OAuth2AuthorizationRequestRepository.ORG_ID_COOKIE_NAME;
import static com.nasnav.security.oauth2.OAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;
import static com.nasnav.service.helpers.LoginHelper.isInvalidRedirectUrl;
import static java.lang.String.format;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {




	@Autowired
    private AuthorizationRequestRepository<OAuth2AuthorizationRequest> requestRepository;

	
	@Autowired
	private OAuth2UserRepository oAuthUserRepo;
	
	
	@Autowired
	private OAuth2Helper helper;


	@Autowired
	private DomainService domainService;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        
        String token = generateOAuth2Token();        
        
        UserPrincipal user = (UserPrincipal)authentication.getPrincipal();
        Long orgId = CookieUtils.getCookie(request, ORG_ID_COOKIE_NAME)
        							.map(Cookie::getValue)
        							.map(this::getOrgIdAsLongVal)
					                .orElseThrow(() -> getNoOrgProvidedException(user) );
        
        String targetUrl = determineTargetUrl(request, token, orgId);
        
        try {
        	String oAuth2Id = user.getId(); 
            oAuthUserRepo.findByoAuth2IdAndOrganizationId(oAuth2Id , orgId)
    					.map(usr -> {return saveTokenToDB(usr, token);} )
    				 	.orElseGet(() -> helper.registerNewOAuth2User(user, token, orgId));
        }catch(Throwable t) {
        	logger.error(t,t);
        	targetUrl = getErrorTargetUrl(request, t, orgId);
        }
        
        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);              
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
    
    
        
  


	private String getErrorTargetUrl(HttpServletRequest request, Throwable t, Long orgId) {
		UriComponents redirectUri = getRedirectParamFromRequest(request, orgId);
		String encodedErrorMsg = "";
		try {
			encodedErrorMsg = URLEncoder.encode("\""+t.getMessage() +"\"", "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(e,e);
		}
		
        return UriComponentsBuilder
        			.fromUriString(redirectUri.toUriString())
	                .queryParam("error", encodedErrorMsg)
	                .build()
	                .toUriString();
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
    
    
    

    protected String determineTargetUrl(HttpServletRequest request, String token, Long orgId) {
    	UriComponents redirectUri = getRedirectParamFromRequest(request, orgId);

        return UriComponentsBuilder
        			.fromUriString(redirectUri.toUriString())
	                .queryParam("token", token)
	                .build()
	                .toUriString();
    }






	private UriComponents getRedirectParamFromRequest(HttpServletRequest request, Long orgId) {
		UriComponents redirectUri = getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
									.map(Cookie::getValue)
									.map(UriComponentsBuilder::fromUriString)
									.map(UriComponentsBuilder::build)
									.orElseGet(this::getDefaultRedirectUri);
		List<String> orgDomains = domainService.getOrganizationDomainOnly(orgId);
		if(isInvalidRedirectUrl(redirectUri, orgDomains)) {
            throw new IllegalStateException("Invalid redirect URL : " + redirectUri);
        }
		return redirectUri;
	}






	private OAuth2UserEntity saveTokenToDB(OAuth2UserEntity oAuthUser, String token) {
		oAuthUser.setLoginToken(token);
		OAuth2UserEntity newOAuthUser = oAuthUserRepo.save(oAuthUser);
		return newOAuthUser;
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

}