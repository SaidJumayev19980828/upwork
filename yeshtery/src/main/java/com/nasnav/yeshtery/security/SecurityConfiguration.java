package com.nasnav.yeshtery.security;


import com.nasnav.enumerations.Roles;
import com.nasnav.security.*;
import com.nasnav.security.oauth2.CustomOAuth2UserService;
import com.nasnav.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.nasnav.security.oauth2.OAuth2AuthenticationSuccessHandler;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.*;

import static com.nasnav.constatnts.ConfigConstants.STATIC_FILES_URL;
import static com.nasnav.enumerations.Roles.*;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpMethod.PUT;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	private static Logger logger = Logger.getLogger(SecurityConfiguration.class);

    private static  RequestMatcher protectedUrlList ;
    private static  RequestMatcher publicUrlList ;
    
    
    @Autowired
    private AuthorizationRequestRepository<OAuth2AuthorizationRequest> oAuth2RequestRepository;

    
    @Autowired
    private CustomOAuth2UserService oAuth2UserService;
    
    @Autowired
    private OidcUserService customOidcUserService; 
    
    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
    
    @Autowired
    private OAuth2AuthenticationFailureHandler oAuth2FailureHandler;
    
    //- Any url is authenticated by default.
    //- to permit a url to all users without AuthN, add it to PUBLIC_URLS.
    //- to set certain roles who can access the url, add it in "permissions"
    //- note that the request url is matched against the patterns IN ORDER, so  
    //	universal patterns like "/**" must always be the last one.
    //- to created a pattern use one of the overloads of "patternOf" method, each adds
    //	more fine grained control of the permission (by HttpMethod, by roles) 
	private  List<AuthPattern> permissions = asList(
						patternOf( "/**")
						);

   
   
    private List<AuthPattern> PUBLIC_URLS =
            	asList(
                        patternOf("/swagger**/**")		//for development only
                        , patternOf("/webjars/**")		//for development only
                        , patternOf("/v3/api-docs/**")		//for development only
                        , patternOf("/csrf/**")		//for development only
                        , patternOf("/favicon.ico")
                        , patternOf("/static/**")
						, patternOf("/icons/**")
                        , patternOf("/js/**")
                        , patternOf("/css/**")
                        , patternOf("/files/**"							, HttpMethod.GET)
                        , patternOf("/error/**"							, HttpMethod.GET)
                        , patternOf("/yeshtery/**"						, HttpMethod.GET)
                 );

    AuthenticationProvider provider;

    public SecurityConfiguration(final AuthenticationProvider authenticationProvider) {
        super();
        
        //allow created threads to inherit the parent thread security context
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        
        this.provider = authenticationProvider;        
        
        List<RequestMatcher> protectedrequestMatcherList = permissions.stream().map(this::toAntPathRequestMatcher).collect(toList());
        protectedUrlList = new OrRequestMatcher( protectedrequestMatcherList );


        List<RequestMatcher> publicRequestMatcherList = PUBLIC_URLS.stream().map(this::toAntPathRequestMatcher).collect(toList());
        publicUrlList = new OrRequestMatcher( publicRequestMatcherList );
    }
    
    
    
    

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(provider);
    }

    
    
    
    @Override
    public void configure(final WebSecurity webSecurity) {
    	//we need to ignore the public url's from the whole security
    	//instead of just permitting it for all users using http.antMatchers(url).permitAll().
    	//Because we authenticate using a custom authentication filter, which apparently is
    	//applied in all cases of authentication.
    	//So we need to bypass the request of the public url from the whole security filter chain.
    	 
    	webSecurity.ignoring().requestMatchers(publicUrlList);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {

    	configureStaticResourcesUrl(http);
    	
        permissions.forEach(pattern -> configureUrlAllowedRoles(http, pattern));
        
        http
        .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        	.authenticationProvider(provider)
        	.addFilterBefore(authenticationFilter(), AnonymousAuthenticationFilter.class)
        	.exceptionHandling()
        	.accessDeniedHandler(new NasnavAccessDeniedHandler()) //return a custom response 403 with a body containing {success=false} , don't know why we need it ..
        .and()
        	.headers()
        	.addHeaderWriter(contentTypeHeaderWriter()) //add content-type='application/json' to security responses
        .and()
            .csrf().disable()
            .formLogin().disable()
            .httpBasic().disable()
            .logout().disable();

        
        http
        .oauth2Login()
        	.authorizationEndpoint()
            .baseUri("/oauth2/authorize")
            .authorizationRequestRepository(oAuth2RequestRepository)
            .and()
        .redirectionEndpoint()
            .baseUri("/oauth2/callback/*")
            .and()
        .userInfoEndpoint()
        	.oidcUserService(customOidcUserService)
        	.and()
        .userInfoEndpoint()
            .userService(oAuth2UserService)
            .and()
        .successHandler(oAuth2SuccessHandler)
        .failureHandler(oAuth2FailureHandler);
        
	    http.cors();
    }

    
    
    
	private void configureStaticResourcesUrl(HttpSecurity http) throws Exception {
		http
        .authorizeRequests()
        .antMatchers(HttpMethod.GET, STATIC_FILES_URL+"/**")
        .hasIpAddress("127.0.0.1"); //only used internally by the server, the server only forwards requests to this API
	}
    
    
    

    AuthenticationFilter authenticationFilter() throws Exception {
        final AuthenticationFilter filter = new AuthenticationFilter(protectedUrlList);
        filter.setAuthenticationManager(authenticationManager());

        //set the handler returns custom response for AuthN failure
        filter.setAuthenticationFailureHandler(customAuthenticationHandler());
        return filter;
    }
    
    

	@Bean
	public AuthenticationFailureHandler customAuthenticationHandler() {
		return new AuthenticationHandler();
	}
	
	

    @Bean
    AuthenticationEntryPoint unauthorizedEntryPoint() {
        return new NasnavHttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
    }



	public HeaderWriter contentTypeHeaderWriter() {
		return new ContentTypeHeaderWriter();
	}

    private static Set<Roles> getAllRoles(){
    	return new HashSet<>(Arrays.asList(Roles.values()));
    }

    private static Set<Roles> getNonCustomersRoles(){
    	Set<Roles> roles = getAllRoles();
    	roles.remove(Roles.CUSTOMER);
    	return roles;
    }

    private String[] toArray(Set<Roles> set) {
    	return set.stream().map(Roles::getValue).toArray(String[]::new);
    }



    private void configureUrlAllowedRoles(HttpSecurity http, AuthPattern pattern) {
    	try {
			http.authorizeRequests()
					.antMatchers(pattern.getHttpMethod(), pattern.getUrlPattern())	
					.hasAnyAuthority( toArray(pattern.getRoles()) );
		} catch (Exception e) {
			logger.error(e,e);
			throw new IllegalStateException("Security configuration failed! ", e);
		}
    }



    private static HashSet<Roles> setOf(Roles... roles) {
		return new HashSet<Roles>(Arrays.asList(roles));
	}
    
    
    
    private static AuthPattern patternOf(String urlPattern , Set<Roles> roles) {
    	return patternOf(urlPattern, null, roles);
    }
    
    
    
    
    private static AuthPattern patternOf(String urlPattern , HttpMethod method) {
    	return patternOf(urlPattern, method , getAllRoles());
    }
    
    
    
    

    private static AuthPattern patternOf(String urlPattern ) {
    	return patternOf(urlPattern, null , getAllRoles());
    }
    
    
    private static AuthPattern patternOf(String urlPattern , HttpMethod method  , Set<Roles> roles) {
    	return new AuthPattern(urlPattern, method, roles);
    };
    
    
    
    
    
    private AntPathRequestMatcher toAntPathRequestMatcher(AuthPattern pattern) {
    	String method = Optional.ofNullable(pattern.getHttpMethod())
    							.map(HttpMethod::toString)
    							.orElse(null);
    	return new AntPathRequestMatcher( pattern.getUrlPattern(), method);
    }
}
