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
import static org.springframework.http.HttpMethod.*;

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
            patternOf( "/v1/360view/**"						, POST  , setOf(ORGANIZATION_ADMIN)),
            patternOf( "/v1/room/shop/list_for_user"				, GET	, getAllRoles()),
            patternOf( "/v1/room/shop/session"					, POST	, setOf(CUSTOMER)),
            patternOf( "/v1/room/shop/template"					, POST	, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER, STORE_MANAGER)),
            patternOf( "/v1/room/shop"					        , DELETE, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER, STORE_MANAGER)),
            patternOf( "/v1/room/event/list_for_user"			, GET	, getAllRoles()),
            patternOf( "/v1/room/event/session"					, POST	, getAllRoles()),
            patternOf( "/v1/room/event/session/suspend"			, POST	, getAllRoles()),
            patternOf( "/v1/room/event/template"					, POST	, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER, STORE_MANAGER)),
            patternOf( "/v1/room/event"					        , DELETE, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER, STORE_MANAGER)),
            patternOf( "/v1/order"							, DELETE, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf( "/v1/order/confirm"					, POST	, setOf(ORGANIZATION_MANAGER, STORE_MANAGER)),
            patternOf( "/v1/order/reject"						, POST	, setOf(ORGANIZATION_MANAGER, STORE_MANAGER)),
            patternOf( "/v1/order/cancel"						, POST	, setOf(CUSTOMER)),
            patternOf( "/v1/order/meta_order/list/user"		, GET	, setOf(CUSTOMER)),
            patternOf( "/v1/order/track_info"					, GET	, setOf(CUSTOMER)),
            patternOf( "/v1/order/return/reject"				, POST	, setOf(ORGANIZATION_MANAGER)),
            patternOf( "/v1/order/return/confirm"				, POST	, setOf(ORGANIZATION_MANAGER)),
            patternOf( "/v1/order/return/received_item"		, POST	, setOf(ORGANIZATION_MANAGER, STORE_MANAGER)),
            patternOf( "/v1/order/return"						, POST	, setOf(CUSTOMER)),
            patternOf( "/v1/order/return/requests"			, GET	, setOf(ORGANIZATION_ADMIN, STORE_MANAGER)),
            patternOf( "/v1/order/return/request"				, GET	, setOf(ORGANIZATION_ADMIN, STORE_MANAGER)),
            patternOf( "/v1/order/status/update"				, POST	, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER, STORE_MANAGER)),
            patternOf( "/v1/order/**"),
            patternOf( "/v1/statistics/**"							, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER) ),
            patternOf( "/v1/stock/**"	 								, getNonCustomersRoles() ),
            patternOf( "/v1/shop/**"									, setOf(ORGANIZATION_MANAGER, STORE_MANAGER) ),
            patternOf( "/v1/shop/stock"	 					, DELETE, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER) ),
            patternOf( "/v1/product/review"					, POST	, setOf(CUSTOMER)),
            patternOf( "/v1/product/**"						, POST	, setOf(ORGANIZATION_ADMIN)),
            patternOf( "/v1/product/**"						, GET	, setOf(ORGANIZATION_ADMIN)),
            patternOf( "/v1/product/**"						, DELETE, setOf(ORGANIZATION_ADMIN)),
            patternOf( "/v1/product/images"					, GET	, setOf(ORGANIZATION_ADMIN)),
            patternOf( "/v1/product/image/bulk/template"				, setOf(ORGANIZATION_ADMIN)),
            patternOf( "/v1/product/empty_collections"		, GET	, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER, NASNAV_ADMIN)),
            patternOf( "/v1/admin/organization/domain"	   	, GET	, setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN) ),
            patternOf( "/v1/admin/organization/domains"	   	, GET	, setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN) ),
            patternOf( "/v1/admin/**"	   	 							, setOf(NASNAV_ADMIN) ),
            patternOf( "/v1/files"							, DELETE , setOf(ORGANIZATION_ADMIN)),
            patternOf( "/v1/files/**"),
            patternOf( "/v1/package/create"					,HttpMethod.POST,setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf( "/v1/package/{packageId:\\d+}"			,HttpMethod.POST,setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf( "/v1/package/{packageId:\\d+}"			,HttpMethod.POST,setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER) ),
            patternOf( "/v1/package/register-package-profile"		,HttpMethod.POST	, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER) ),
            patternOf( "/v1/organization/info"						, setOf(ORGANIZATION_ADMIN)),
            patternOf( "/v1/organization/brand"						, setOf(ORGANIZATION_ADMIN)),
            patternOf( "/v1/organization/image"				, POST	, setOf(ORGANIZATION_ADMIN)),
            patternOf( "/v1/organization/image"				, DELETE, setOf(ORGANIZATION_ADMIN)),
            patternOf( "/v1/organization/products_feature"	, POST	, setOf(ORGANIZATION_ADMIN)),
            patternOf( "/v1/organization/products_feature"	, DELETE, setOf(ORGANIZATION_ADMIN)),
            patternOf( "/v1/organization/tag/**"						, setOf(ORGANIZATION_ADMIN)),
            patternOf( "/v1/organization/tags"						, setOf(ORGANIZATION_ADMIN)),
            patternOf( "/v1/organization/shipping/**"					, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf( "/v1/organization/promotions/**"				, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf( "/v1/organization/promotion/**"				, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf( "/v1/organization/themes/class"				, setOf(NASNAV_ADMIN)),
            patternOf( "/v1/organization/themes"						, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf( "/v1/organization/extra_attribute"				, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf( "/v1/organization/shops"						, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf( "/v1/organization/settings/**"					, setOf(ORGANIZATION_ADMIN)),
            patternOf( "/v1/organization/subscribed_users"			, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf( "/v1/organization/images_info"					, setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN)),
            patternOf( "/v1/organization/search/**"					, setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN)),
            patternOf( "/v1/organization/seo"							, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf( "/v1/organization/sub_areas"					, setOf(ORGANIZATION_ADMIN)),
            patternOf("/v1/subscription/info"						, HttpMethod.GET ,getNonCustomersRoles()),
            patternOf("/v1/subscription/wert/create"			, HttpMethod.POST,setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER) ),
            patternOf("/v1/subscription/stripe/create"			, HttpMethod.POST,setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER) ),
            patternOf("/v1/subscription/stripe/changePaymentMethod"			, HttpMethod.POST,setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER) ),
            patternOf("/v1/subscription/stripe/cancel"			, HttpMethod.POST,setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER) ),
            patternOf("/v1/subscription/stripe/changePlan"			, HttpMethod.POST,setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER) ),
            patternOf( "/v1/mail/cart/abandoned"						, setOf(ORGANIZATION_ADMIN)),
            patternOf( "/v1/mail/wishlist/stock"						, setOf(ORGANIZATION_ADMIN)),
            patternOf( "/v1/upload/**"								, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER, STORE_MANAGER)),
            patternOf( "/v1/export/**"								, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER, STORE_MANAGER)),
            patternOf( "/v1/integration/import/shops"					, setOf(ORGANIZATION_MANAGER)),
            patternOf( "/v1/integration/import/products"				, setOf(ORGANIZATION_MANAGER)),
            patternOf( "/v1/integration/import/product_images"		, setOf(ORGANIZATION_MANAGER)),
            patternOf( "/v1/integration/module/disable"				, setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN)),
            patternOf( "/v1/integration/module/enable"				, setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN)),
            patternOf( "/v1/integration/module/**"					, setOf(NASNAV_ADMIN)),
            patternOf( "/v1/integration/param/**"						, setOf(NASNAV_ADMIN)),
            patternOf( "/v1/integration/dictionary"					, setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN)),
            patternOf( "/v1/integration/errors"						, setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN)),
            patternOf( "/v1/integration/**"							, setOf(NASNAV_ADMIN))
            , patternOf( "/v1/user/list"						, GET	, getNonCustomersRoles())
            , patternOf( "/v1/user/list/customer"				, GET	, getNonCustomersRoles())
            , patternOf( "/v1/user/address"					, PUT   , setOf(CUSTOMER))
            , patternOf( "/v1/user/address"					, DELETE, setOf(CUSTOMER))
            , patternOf( "/v1/user/info")
            , patternOf( "/v1/user/create"							, setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN, STORE_MANAGER) )
            , patternOf( "/v1/user/update"							, getAllRoles() )
            , patternOf( "/v1/user/notification-token"		, POST	, getAllRoles() )
            , patternOf( "/v1/user/change/password"							, getAllRoles() )
            , patternOf( "/v1/user/logout"							, getAllRoles() )
            , patternOf( "/v1/user/logout_all"						, getAllRoles() )
            , patternOf( "/v1/user/suspend"						    , setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN))
            ,patternOf( "/v1/cart/checkout", POST ,getAllRoles())
            , patternOf( "/v1/cart/{userId:\\d+}"		,GET		, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER))
            , patternOf( "/v1/cart/item"			,DELETE					, getAllRoles())
            , patternOf( "/v1/cart/**"								, setOf(CUSTOMER))
            , patternOf( "/v1/pickup/**"							    , setOf(CUSTOMER))
            , patternOf( "/v1/wishlist/{userId:\\d+}"		,GET		, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER))
            , patternOf( "/v1/wishlist/**"							, setOf(CUSTOMER))
            , patternOf( "/v1/shipping/**"							, setOf(CUSTOMER))
            , patternOf("/v1/yeshtery/review"					, POST  , setOf(CUSTOMER))
            , patternOf("/v1/user/review"					    , GET   , setOf(CUSTOMER))
			, patternOf("/v1/user/link_nasnav_users_to_yeshtery_users", POST   , setOf(NASNAV_ADMIN)),
            patternOf( "/v1/videochat/**"                   , POST    , getAllRoles()),
            patternOf( "/v1/videochat/**"                   , GET    , getNonCustomersRoles()),
            patternOf("/v1/chat/visitor"								, POST						, setOf(CUSTOMER)),
            patternOf("/v1/chat/agent/authenticate"							, POST						, getNonNasnavRoles()),
            patternOf("/v1/availability/org/**"                       				, getAllRoles()),
            patternOf("/v1/availability/shop/**"                       				, getAllRoles()),
            patternOf("/v1/availability/user"                       					, setOf(CUSTOMER)),
            patternOf("/v1/employee-user-heart-beats-logs/log"		, POST		    , getNonCustomersRoles()),
            patternOf( "/v1/availability/employee/**"                      			, getAllRoles()),
            patternOf("/v1/availability/**"                       				, getNonCustomersRoles()),
            patternOf("/v1/appointment/**"											, setOf(CUSTOMER)),
            patternOf( "/v1/follow/**"					,POST						, setOf(CUSTOMER)),
            patternOf( "/v1/follow/**"					,GET						, getAllRoles()),
            patternOf( "/v1/post/orgSharedProducts"		,GET						,  setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf( "/v1/post/orgReviews"				,GET						,  setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf( "/v1/post/**"						,GET						,  setOf(CUSTOMER)),
            patternOf( "/v1/post/**"						,POST						,  setOf(CUSTOMER)),
            patternOf( "/v1/post/**"						,PUT						,  setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf( "/v1/event"                   , POST    , setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf( "/v1/event"                   , DELETE    , setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf( "/v1/event/list/**"                                , getAllRoles()),
            patternOf( "/v1/event/advertise/all/**"                                , getAllRoles()),
            patternOf( "/post/save" , POST                              , getAllRoles()),
            patternOf( "/post/unsave"       ,POST                         , getAllRoles()),
            patternOf( "/post/saved"        ,GET                        , getAllRoles()),
            patternOf( "/follow/users/list"        ,GET                        , getAllRoles()),
            patternOf( "/queue"						,POST						,  setOf(CUSTOMER)),
            patternOf( "/queue"						,GET						,  getNonCustomersRoles()),
            patternOf( "/queue/cancel"					,PUT						,  setOf(CUSTOMER)),
            patternOf( "/queue/accept"					,PUT						,  getNonCustomersRoles()),
            patternOf( "/queue/reject"					,PUT						,  getNonCustomersRoles()),
            patternOf( "/queue/logs"					,GET						,  setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),


            patternOf( "/v1/influencer/host/**"             , getAllRoles()),
            //TODO change roles so that the testing process continues the old one is setOf(NASNAV_ADMIN) only
            patternOf( "/v1/influencer/response"             ,POST                 ,  setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf( "/v1/influencer/hostingRequests"             ,GET                 , getNonCustomersRoles()),
            patternOf("/v1/loyalty/points/update"						, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf("/v1/loyalty/points"					, GET	, setOf(CUSTOMER)),
			patternOf("/v1/loyalty/points/list"				, GET	, setOf(CUSTOMER)),
            patternOf("/v1/loyalty/points/list_by_user"		, GET	, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf("/v1/loyalty/points_per_org"			, GET	, setOf(CUSTOMER)),
            patternOf("/v1/loyalty/spendable_points"			, GET	, setOf(CUSTOMER)),
            patternOf("/v1/loyalty/spendable_points/{orgId}"			, GET	, setOf(CUSTOMER)),
            patternOf("/v1/loyalty/share_points"			, GET	, setOf(CUSTOMER)),
			patternOf("/v1/loyalty/user_tier"							, setOf(CUSTOMER)),
            patternOf("/v1/loyalty/points/delete"						, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf("/v1/loyalty/type/**"							, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf("/v1/loyalty/family/**"							, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf("/v1/loyalty/tier/**"							, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf("/v1/loyalty/booster/**"						, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf("/v1/loyalty/config/**"							, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf("/v1/loyalty/user_tier"					, GET	, setOf(CUSTOMER)),
            patternOf("/v1/loyalty/points/code/redeem"					, setOf(CUSTOMER)),
            patternOf("/v1/loyalty/points/code/generate"				, setOf(STORE_MANAGER, STORE_EMPLOYEE)),
            patternOf("/v1/chat-widget-setting/create",POST, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf("/v1/chat-widget-setting/publish",POST, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf("/v1/chat-widget-setting/get-unpublished",GET, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
            patternOf("/v1/organization/add/image/type",POST),
            patternOf( "/v1/organization/delete/image/type",DELETE),
            patternOf( "/v1/organization/update/image/type",POST),
            patternOf( "/v1/organization/images/types",GET),
            patternOf("/**")

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
                        , patternOf("/v1/callbacks/**")
                        , patternOf("/v1/product/bundles"				    , GET)
                        , patternOf("/v1/product/info"					, GET)
                        , patternOf("/v1/product/image"					, GET)
                        , patternOf("/v1/product/variant"				    , GET)
                        , patternOf("/v1/organization/payments"			, GET)
                        , patternOf("/v1/organization/brands"			    , GET)
                        , patternOf( "/v1/organization/register"		    ,HttpMethod.POST)
                        , patternOf("/v1/subscription/stripe/webhook"			,HttpMethod.POST)
                        , patternOf("/v1/organization/products_features"	, GET)
                        , patternOf("/v1/files/**"					    , GET)
                        , patternOf("/error/**"							, GET)
                        , patternOf("/v1/yeshtery/**"						, GET)
                        , patternOf("/v1/360view/**"						, GET)
						, patternOf("/v1/room/shop"								, GET)
						, patternOf("/v1/room/shop/list"							, GET)
						, patternOf("/v1/room/event"								, GET)
						, patternOf("/v1/room/event/list"							, GET)
                        , patternOf("/v1/payment/**")
                        , patternOf("/v1/user/recover")
                        , patternOf("/v1/user/recovery/otp-verify")
                        , patternOf("/v1/user/login/**")
                        , patternOf("/v1/user/register")
                        , patternOf("/v1/user/register/activate")
						, patternOf("/v1/user/register/otp/activate")
                        , patternOf("/user/v2/register")
                        , patternOf("/user/v2/register/activate")
                        , patternOf("/v1/user/v2/register/otp/activate")
                        , patternOf("/v1/user/v2/employee/otp/activate")
                        , patternOf("/v1/user/v2/register/activate/resend")
                        , patternOf( "/v1/user/subscribe")
                        , patternOf("/v1/user/employee/otp/activate")
                        , patternOf("/v1/user/register/activate/resend")
                        , patternOf("/v1/user/subscribe")
                        , patternOf("/v1/user/subscribe/activate")
						, patternOf("/v1/employee-user-heart-beats-logs/list-active-employee", GET)
                        ,patternOf( "/v1/organization/register"		    ,HttpMethod.POST)

                        , patternOf("/v1/frontend/setting", GET)
                        ,patternOf( "/v1/package"					, GET)
                        ,              patternOf( "/v1/event/all/**"                                , GET)
                        , patternOf("/v1/chat-widget-setting/get-published", GET)
                        , patternOf("/v1/loyalty/config/all"		, GET)
                        , patternOf("/v1/contactUs")
                        , patternOf("/v1/contactUs/**")
                        , patternOf("/v1/bank/deposit/bc**",POST)
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
    
    
    
    

    /*~~TODO with JWT : (Migrate manually based on https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter)~~>*/
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
            .baseUri("/v1/oauth2/authorize")
            .authorizationRequestRepository(oAuth2RequestRepository)
            .and()
        .redirectionEndpoint()
            .baseUri("/v1/oauth2/callback/*")
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
        .antMatchers(GET, STATIC_FILES_URL+"/**")
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

	private static Set<Roles> getNonNasnavRoles(){
    	return setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER,
				ORGANIZATION_EMPLOYEE, STORE_MANAGER, STORE_EMPLOYEE);
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
