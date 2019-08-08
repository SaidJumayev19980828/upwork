package com.nasnav.security;


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
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {


    private static  RequestMatcher protectedUrlList ;
    
    //TODO: currently the AuthenticationFilter calls the authentication process
    //and it takes the PROTECTED_URLS list to work on
    //which means,any url is permitted by default, this should be changed, but 
    //currently PUBLIC_URLS can't intersected with PROTECTED_URLS.
    //i.e if a url matches a pattern in both protected URL's and PUBLIC_URL
    //it will be authenticated.
    private static final List<String> PROTECTED_URLS = 
    		Arrays.asList("/order/**"
    					, "/stock/**"
    					, "/shop/**"
    					, "/user/list"
    					, "/user/update");

    private static final List<String> PUBLIC_URLS =
            Arrays.asList("/navbox/**"
                        , "/user/recover"
                        , "/user/login"
                        , "/user/register"
                        , "/payment/**");

    AuthenticationProvider provider;

    public SecurityConfiguration(final AuthenticationProvider authenticationProvider) {
        super();
        this.provider = authenticationProvider;        
        
        List<RequestMatcher> requestMatcherList = PROTECTED_URLS.stream().map(AntPathRequestMatcher::new).collect(Collectors.toList());
        protectedUrlList = new OrRequestMatcher( requestMatcherList );
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
        http
        .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)                
                .and()
                .authenticationProvider(provider)
                .addFilterBefore(authenticationFilter(), AnonymousAuthenticationFilter.class)
                .authorizeRequests()
                .requestMatchers(protectedUrlList)
                .authenticated()
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
        //filter.setAuthenticationSuccessHandler(successHandler());
        return filter;
    }



    @Bean
    AuthenticationEntryPoint unauthorizedEntryPoint() {
        return new NasnavHttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
    }
}
