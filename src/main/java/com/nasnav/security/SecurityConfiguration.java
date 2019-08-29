package com.nasnav.security;


import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.logging.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
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


    @SuppressWarnings("unchecked")
	private static Map<String, Set<Roles>> permissions = Stream.of(new Object[][] {
				    { "/order/**" 		, getAllRoles() },
				    { "/stock/**" 		, getNonCustomersRoles() },
				    { "/shop/**"		, setOf(Roles.ORGANIZATION_MANAGER, Roles.STORE_MANAGER) },
				    { "/user/list"		, getAllRoles() },
				    { "/user/create"	, setOf(Roles.NASNAV_ADMIN, Roles.ORGANIZATION_ADMIN, Roles.STORE_ADMIN) },
				    { "/user/update"	, getAllRoles() },
				    { "/product/**"		, setOf(Roles.ORGANIZATION_ADMIN)},
				    { "/admin/**"	    , setOf(Roles.NASNAV_ADMIN)},
				    { "/files/**"		, getAllRoles() },
                    { "/admin/organization", setOf(Roles.NASNAV_ADMIN)},
                    { "/organization/info", setOf(Roles.ORGANIZATION_ADMIN)},
                    { "/organization/brand", setOf(Roles.ORGANIZATION_ADMIN)},
    }).collect(Collectors.toMap(data -> (String) data[0], data -> (Set<Roles>) data[1]));

    //TODO: currently the AuthenticationFilter calls the authentication process
    //and it takes the PROTECTED_URLS list to work on
    //which means,any url is permitted by default, this should be changed, but 
    //currently PUBLIC_URLS can't intersected with PROTECTED_URLS.
    //i.e if a url matches a pattern in both protected URL's and PUBLIC_URL
    //it will be authenticated.
    private static final Set<String> PROTECTED_URLS = permissions.keySet();

    private static final List<String> PUBLIC_URLS =
            Arrays.asList("/navbox/**"
                        , "/user/recover"
                        , "/user/login"
                        , "/user/register"
		                , "/shop/update"
		                , "/order/list"
                        , "/payment/**");

    AuthenticationProvider provider;

    public SecurityConfiguration(final AuthenticationProvider authenticationProvider) {
        super();
        this.provider = authenticationProvider;        
        
        List<RequestMatcher> protectedrequestMatcherList = PROTECTED_URLS.stream().map(AntPathRequestMatcher::new).collect(Collectors.toList());
        protectedUrlList = new OrRequestMatcher( protectedrequestMatcherList );


        List<RequestMatcher> publicRequestMatcherList = PUBLIC_URLS.stream().map(AntPathRequestMatcher::new).collect(Collectors.toList());
        publicUrlList = new OrRequestMatcher( publicRequestMatcherList );
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(provider);
    }

    @Override
    public void configure(final WebSecurity webSecurity) {
        PUBLIC_URLS.stream().forEach(webSecurity.ignoring()::antMatchers);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
    	permissions.forEach((url, roles) -> configureUrlAllowedRoles(http, url, roles));

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

//    @Bean
//    AccessDeniedHandler accessDeniedEntryPoint() {
//        return new NasnavAccessDeniedEntryPoint();
//    }

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



    private void configureUrlAllowedRoles(HttpSecurity http, String url, Set<Roles> roles) {
    	try {
			http.authorizeRequests().antMatchers(url).hasAnyAuthority(toArray(roles));
		} catch (Exception e) {
			logger.error(e,e);
			throw new IllegalStateException("Security configuration failed! ", e);
		}
    }



    private static HashSet<Roles> setOf(Roles... roles) {
		return new HashSet<Roles>(Arrays.asList(roles));
	}
}
