package com.nasnav.security;


import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;
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
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.nasnav.enumerations.Roles;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	private static Logger logger = Logger.getLogger(SecurityConfiguration.class);

    private static  RequestMatcher protectedUrlList ;
    private static  RequestMatcher publicUrlList ;

    
    //TODO: currently the AuthenticationFilter calls the authentication process
    //and it takes the "permissions" url patterns to work on.
    //which means,**ANY URL IS PERMITTED BY DEFAULT**, this should be changed, but 
    //currently PUBLIC_URLS can't intersected with PROTECTED_URLS.
    //i.e if a url matches a pattern in both protected URL's and PUBLIC_URL
    //it will be authenticated.
	private  List<AuthPattern> permissions = Arrays.asList(
					    patternOf( "/order/**"	 								, getAllRoles() ),
						patternOf( "/stock/**"	 								, getNonCustomersRoles() ),
						patternOf( "/shop/**"									, setOf(Roles.ORGANIZATION_MANAGER, Roles.STORE_MANAGER) ),
						patternOf( "/user/list"									, getAllRoles() ),
						patternOf( "/user/create"								, setOf(Roles.NASNAV_ADMIN, Roles.ORGANIZATION_ADMIN, Roles.STORE_ADMIN) ),
						patternOf( "/user/update"								, getAllRoles() ),
						patternOf( "/product/bundles"		,HttpMethod.POST	, setOf(Roles.ORGANIZATION_ADMIN)),
						patternOf( "/product/bundles"		,HttpMethod.DELETE	, setOf(Roles.ORGANIZATION_ADMIN)),
						patternOf( "/product/info"			,HttpMethod.POST	, setOf(Roles.ORGANIZATION_ADMIN)),
						patternOf( "/product/info"			,HttpMethod.DELETE	, setOf(Roles.ORGANIZATION_ADMIN)),
						patternOf( "/product/image"			,HttpMethod.POST	, setOf(Roles.ORGANIZATION_ADMIN)),
						patternOf( "/product/image"			,HttpMethod.DELETE	, setOf(Roles.ORGANIZATION_ADMIN)),
						patternOf( "/product/variant"		,HttpMethod.POST	, setOf(Roles.ORGANIZATION_ADMIN)),
						patternOf( "/product/variant"		,HttpMethod.DELETE	, setOf(Roles.ORGANIZATION_ADMIN)),
						patternOf( "/admin/**"	   	 							, setOf(Roles.NASNAV_ADMIN) ),
						patternOf( "/files/**"									, getAllRoles() ),
						patternOf( "/admin/organization"						, setOf(Roles.NASNAV_ADMIN)),
						patternOf( "/organization/info"							, setOf(Roles.ORGANIZATION_ADMIN)),
						patternOf( "/organization/brand"						, setOf(Roles.ORGANIZATION_ADMIN))
						);

   
   
    private List<AuthPattern> PUBLIC_URLS =
            Arrays.asList(
            			patternOf("/navbox/**")
                        , patternOf("/user/recover")
                        , patternOf("/user/login")
                        , patternOf("/user/register")
		                , patternOf("/shop/update")
		                , patternOf("/order/list")
                        , patternOf("/payment/**")
                        , patternOf("/product/bundles"	, HttpMethod.GET)
                        , patternOf("/product/info"		, HttpMethod.GET)
                        , patternOf("/product/image"	, HttpMethod.GET)
                        , patternOf("/product/variant"	, HttpMethod.GET)
                 );

    AuthenticationProvider provider;

    public SecurityConfiguration(final AuthenticationProvider authenticationProvider) {
        super();
        this.provider = authenticationProvider;        
        
        List<RequestMatcher> protectedrequestMatcherList = permissions.stream().map(this::toAntPathRequestMatcher).collect(Collectors.toList());
        protectedUrlList = new OrRequestMatcher( protectedrequestMatcherList );


        List<RequestMatcher> publicRequestMatcherList = PUBLIC_URLS.stream().map(this::toAntPathRequestMatcher).collect(Collectors.toList());
        publicUrlList = new OrRequestMatcher( publicRequestMatcherList );
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(provider);
    }

    @Override
    public void configure(final WebSecurity webSecurity) {
        PUBLIC_URLS.stream().forEach( pattern -> webSecurity.ignoring().antMatchers(pattern.getHttpMethod(), pattern.getUrlPattern()));
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
    	permissions.forEach(pattern -> configureUrlAllowedRoles(http, pattern));

        http
        .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        	.authenticationProvider(provider)
        	.addFilterBefore(authenticationFilter(), AnonymousAuthenticationFilter.class)
        	.authorizeRequests()
        		.requestMatchers(publicUrlList).permitAll()
//        		.anyRequest().authenticated()        	//adding this causes unauthenticated responses to have status 403, it overrided the
        .and()
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
    }
    
    
    

    @Bean
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
