package com.nasnav.security;


import com.nasnav.enumerations.Roles;
import com.nasnav.security.oauth2.CustomOAuth2UserService;
import com.nasnav.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.nasnav.security.oauth2.OAuth2AuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
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
@EnableMethodSecurity
@EnableAspectJAutoProxy
public class SecurityConfiguration {

    private static RequestMatcher protectedUrlList;
    private static RequestMatcher publicUrlList;

    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> oAuth2RequestRepository;
    private final CustomOAuth2UserService oAuth2UserService;
    private final OidcUserService customOidcUserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2FailureHandler;

	//- Any url is authenticated by default.
	//- to permit a url to all users without AuthN, add it to PUBLIC_URLS.
	//- to set certain roles who can access the url, add it in "permissions"
	//- note that the request url is matched against the patterns IN ORDER, so
	//	universal patterns like "/**" must always be the last one.
	//- to created a pattern use one of the overloads of "patternOf" method, each adds
	//	more fine grained control of the permission (by HttpMethod, by roles)
	private  List<AuthPattern> permissions = asList(
			// //url pattern	-------------------------	Method	------------	Roles
			patternOf( "/360view/**"						,HttpMethod.POST	, setOf(ORGANIZATION_ADMIN)),
			patternOf( "/room/shop/list_for_user"				,GET				, getAllRoles()),
			patternOf( "/room/shop/session"					,POST				, setOf(CUSTOMER)),
			patternOf( "/room/shop/rateShop"					,POST				, setOf(CUSTOMER)),
			patternOf( "/room/shop/template"					,POST				, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER, STORE_MANAGER)),
			patternOf( "/room/shop"							,DELETE				, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER, STORE_MANAGER)),
			patternOf( "/room/event/list_for_user"			,GET			, getAllRoles()),
			patternOf( "/room/event/session"					,POST				, getAllRoles()),
			patternOf( "/room/event/session/suspend"					,POST				, getAllRoles()),
			patternOf( "/room/event/template"					,POST				, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER, STORE_MANAGER)),
			patternOf( "/room/event"							,DELETE				, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER, STORE_MANAGER)),
			patternOf( "/order"							,HttpMethod.DELETE	, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf( "/order/confirm"					,HttpMethod.POST	, setOf(ORGANIZATION_MANAGER, STORE_MANAGER)),
			patternOf( "/order/reject"						,HttpMethod.POST	, setOf(ORGANIZATION_MANAGER, STORE_MANAGER)),
			patternOf( "/order/cancel"						,HttpMethod.POST	, setOf(CUSTOMER)),
			patternOf( "/order/meta_order/list/user"		,HttpMethod.GET		, setOf(CUSTOMER)),
			patternOf( "/order/track_info"					,HttpMethod.GET		, setOf(CUSTOMER)),
			patternOf( "/order/return/reject"				,HttpMethod.POST	, setOf(ORGANIZATION_MANAGER)),
			patternOf( "/order/return/confirm"				,HttpMethod.POST	, setOf(ORGANIZATION_MANAGER)),
			patternOf( "/order/return/received_item"		,HttpMethod.POST	, setOf(ORGANIZATION_MANAGER, STORE_MANAGER)),
			patternOf( "/order/return"						,HttpMethod.POST	, setOf(CUSTOMER)),
			patternOf( "/order/return/requests"			,HttpMethod.GET		, setOf(ORGANIZATION_ADMIN, STORE_MANAGER)),
			patternOf( "/order/return/request"				,HttpMethod.GET		, setOf(ORGANIZATION_ADMIN, STORE_MANAGER)),
			patternOf( "/order/status/update"				,HttpMethod.POST	, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER, STORE_MANAGER)),
			patternOf( "/order/**"),
			patternOf( "/statistics/**"										, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER) ),
			patternOf( "/stock/**"	 											, getNonCustomersRoles() ),
			patternOf( "/shop/**"												, setOf(ORGANIZATION_MANAGER, STORE_MANAGER) ),
			patternOf( "/shop/stock"	 					, DELETE			, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER) ),
			patternOf( "/user/list"),
			patternOf( "/user/uploadAvatar"											, setOf(CUSTOMER)),
			patternOf( "/user/list/customer"				,HttpMethod.GET		, getNonCustomersRoles()),
			patternOf( "/user/address"						,PUT                , setOf(CUSTOMER)),
			patternOf( "/user/address"						,HttpMethod.DELETE  , setOf(CUSTOMER)),
			patternOf( "/user/info"),
			patternOf( "/user/create"											, setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN, STORE_MANAGER) ),
			patternOf( "/user/update"											, getAllRoles() ),
			patternOf( "/user/notification-token"			,POST				, getAllRoles() ),
			patternOf( "/user/change/password"											, getAllRoles() ),
			patternOf( "/user/logout"											, getAllRoles() ),
			patternOf( "/user/logout_all"										, getAllRoles() ),
			patternOf( "/user/suspend"											, setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN)),
			patternOf("/user/review"					    , HttpMethod.GET   , setOf(CUSTOMER)),
			patternOf("/product/model3d/assign", POST),
			patternOf("/product/model3d/unassign", POST),
			patternOf("/product/model3d", POST),
			patternOf("product/model3d/**", DELETE),
			patternOf("product/model3d/**", PUT),
			patternOf( "/product/review"					,HttpMethod.POST	, setOf(CUSTOMER)),
			patternOf( "/product/**"						,HttpMethod.POST	, setOf(ORGANIZATION_ADMIN)),
			patternOf( "/product/**"						,HttpMethod.GET		, setOf(ORGANIZATION_ADMIN)),
			patternOf( "/product/**"						,HttpMethod.DELETE	, setOf(ORGANIZATION_ADMIN)),
			patternOf( "/product/images"					,HttpMethod.GET		, setOf(ORGANIZATION_ADMIN)),
			patternOf( "/product/image/bulk/template"							, setOf(ORGANIZATION_ADMIN)),
			patternOf( "/product/empty_collections"		,HttpMethod.GET		, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER, NASNAV_ADMIN)),
			patternOf("/product/out-of-stock-products", HttpMethod.GET, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf( "/admin/organization/domain"	   	,HttpMethod.GET		, setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN) ),
			patternOf( "/admin/organization/domains"	   	,HttpMethod.GET		, setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN) ),
			patternOf( "/admin/**"	   	 									, setOf(NASNAV_ADMIN) ),
			patternOf( "/files/**"							,HttpMethod.DELETE  , setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN)),
			patternOf( "/files/**"),
			patternOf( "/permission/**"					,setOf(NASNAV_ADMIN)),
			patternOf( "/roles/**"							,setOf(NASNAV_ADMIN)),
			patternOf( "/package/create"					,HttpMethod.POST,setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf( "/package/{packageId:\\d+}"			,HttpMethod.POST,setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf( "/package/{packageId:\\d+}"			,HttpMethod.POST,setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER) ),
			patternOf( "/package/register-package-profile"		,HttpMethod.POST	, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER) ),
			patternOf( "/organization/info"									, setOf(ORGANIZATION_ADMIN)),
			patternOf( "/organization/brand"									, setOf(ORGANIZATION_ADMIN)),
			patternOf( "/organization/image"				,HttpMethod.POST	, setOf(ORGANIZATION_ADMIN)),
			patternOf( "/organization/image"				,HttpMethod.DELETE	, setOf(ORGANIZATION_ADMIN)),
			patternOf( "/organization/products_feature"	,HttpMethod.POST	, setOf(ORGANIZATION_ADMIN)),
			patternOf( "/organization/products_feature"	,HttpMethod.DELETE	, setOf(ORGANIZATION_ADMIN)),
			patternOf( "/organization/tag/**"									, setOf(ORGANIZATION_ADMIN)),
			patternOf( "/organization/tags"									, setOf(ORGANIZATION_ADMIN)),
			patternOf( "/organization/shipping/**"								, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf( "/organization/promotions/**"							, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf( "/organization/promotion/**"							, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf( "/organization/themes/class"							, setOf(NASNAV_ADMIN)),
			patternOf( "/process/**"											, setOf(NASNAV_ADMIN)),
			patternOf( "/organization/themes"									, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf( "/organization/extra_attribute"							, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf( "/organization/extra_attribute"		,HttpMethod.POST	, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf( "/organization/shops"									, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf( "/organization/settings/**"								, setOf(ORGANIZATION_ADMIN)),
			patternOf( "/organization/subscribed_users"						, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf( "/organization/images_info"								, setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN)),
			patternOf( "/organization/search/**"								, setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN)),
			patternOf( "/organization/seo"										, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf( "/organization/sub_areas"								, setOf(ORGANIZATION_ADMIN)),
			patternOf("/subscription/info"						, HttpMethod.GET ,getNonCustomersRoles()),
			patternOf("/subscription/wert/create"			, HttpMethod.POST,setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER) ),
			patternOf("/subscription/stripe/create"			, HttpMethod.POST,setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER) ),
			patternOf("/subscription/stripe/changePaymentMethod"			, HttpMethod.POST,setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER) ),
			patternOf("/subscription/stripe/cancel"			, HttpMethod.POST,setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER) ),
			patternOf("/subscription/stripe/changePlan"			, HttpMethod.POST,setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER) ),
			patternOf( "/mail/cart/abandoned"									, setOf(ORGANIZATION_ADMIN)),
			patternOf( "/mail/wishlist/stock"									, setOf(ORGANIZATION_ADMIN)),
			patternOf( "/upload/**"											, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER, STORE_MANAGER)),
			patternOf( "/export/**"											, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER, STORE_MANAGER)),
			patternOf( "/integration/import/shops"								, setOf(ORGANIZATION_MANAGER)),
			patternOf( "/integration/import/products"							, setOf(ORGANIZATION_MANAGER)),
			patternOf( "/integration/import/product_images"					, setOf(ORGANIZATION_MANAGER)),
			patternOf( "/integration/module/disable"							, setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN)),
			patternOf( "/integration/module/enable"							, setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN)),
			patternOf( "/integration/module/**"								, setOf(NASNAV_ADMIN)),
			patternOf( "/integration/param/**"									, setOf(NASNAV_ADMIN)),
			patternOf( "/integration/dictionary"								, setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN)),
			patternOf( "/integration/errors"									, setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN)),
			patternOf( "/integration/**"										, setOf(NASNAV_ADMIN))
			,patternOf( "/cart/checkout", POST ,getAllRoles()),
			patternOf( "/cart/{userId:\\d+}"		,GET						, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf( "/cart/store-checkout/initiate"	 ,POST					, setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN, ORGANIZATION_MANAGER,ORGANIZATION_EMPLOYEE, STORE_EMPLOYEE, STORE_MANAGER)),
			patternOf( "/cart/store-checkout/complete"	 ,POST					, setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN, ORGANIZATION_MANAGER,ORGANIZATION_EMPLOYEE, STORE_EMPLOYEE, STORE_MANAGER)),

			patternOf( "/cart/**"											, setOf(CUSTOMER )),
			patternOf( "/pickup/**"											, setOf(CUSTOMER)),
			patternOf( "/wishlist/{userId:\\d+}"		,GET					, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf( "/wishlist/**"										, setOf(CUSTOMER)),
			patternOf( "/shipping/offers"										, setOf(CUSTOMER)),
			patternOf( "/videochat/**"                   , POST    			, getAllRoles()),
			patternOf( "/videochat/**"                   , GET    				, getNonCustomersRoles()),
			patternOf("/chat/visitor"								, POST						, setOf(CUSTOMER)),
			patternOf("/chat/agent/authenticate"								, POST						, getNonNasnavRoles()),
			patternOf( "/availability/org/**"                       				, getAllRoles()),
			patternOf( "/availability/shop/**"                       				, getAllRoles()),
			patternOf( "/availability/user"                       					, setOf(CUSTOMER)),
			patternOf( "/availability/employee/**"                      			, getAllRoles()),
			patternOf( "/availability/**"                       				, getNonCustomersRoles()),
			patternOf( "/employee-user-heart-beats-logs/log"		, POST		, getNonCustomersRoles()),
			patternOf( "/appointment/**"											, setOf(CUSTOMER)),
			patternOf( "/follow/**"					,POST						, setOf(CUSTOMER)),
			patternOf( "/follow/**"					,GET						, getAllRoles()),
			patternOf( "/post/orgSharedProducts"		,GET						,  setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf( "/post/orgReviews"				,GET						,  setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf( "/post/**"						,GET						,  setOf(CUSTOMER)),
			patternOf( "/post/**"						,POST						,  setOf(CUSTOMER)),
			patternOf( "/post/**"						,PUT						,  setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf( "/queue"						,POST						,  setOf(CUSTOMER)),
			patternOf( "/queue"						,GET						,  getNonCustomersRoles()),
			patternOf( "/queue/cancel"					,PUT						,  setOf(CUSTOMER)),
			patternOf( "/queue/accept"					,PUT						,  getNonCustomersRoles()),
			patternOf( "/queue/reject"					,PUT						,  getNonCustomersRoles()),
			patternOf( "/queue/logs"					,GET						,  setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf( "/bank/account"					,GET						,  setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf( "/bank/account"					,PUT						,  setOf(NASNAV_ADMIN)),
			patternOf( "/bank/account/setOpeningBalance",POST						,  setOf(NASNAV_ADMIN)),
			patternOf( "/bank/account"					,POST						,  getAllRoles()),
			patternOf( "/bank/transaction/**"		   ,POST						,  getAllRoles()),
			patternOf( "/bank/pay"		   				,POST						,  getAllRoles()),
			patternOf( "/bank/account/reservations"		   ,GET						,  getAllRoles()),
			patternOf( "/bank/account/reservation/**"		   ,GET						,  getAllRoles()),
			patternOf( "/bank/account/history"		   			,GET					,  getAllRoles()),
			patternOf( "/bank/account/summary"		   ,GET						,  getAllRoles()),
			patternOf("/loyalty/points/update"									, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf("/loyalty/points"					, GET						, setOf(CUSTOMER)),
			patternOf("/loyalty/points/list"				, GET						, setOf(CUSTOMER)),
			patternOf("/loyalty/points/list_by_user"		, GET						, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf("/loyalty/spendable_points"		, GET						, setOf(CUSTOMER)),
			patternOf("/loyalty/points/delete"									, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf("/loyalty/type/**"										, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf("/loyalty/tier/**"										, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf("/loyalty/config/**"										, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf("/loyalty/points/check"									, setOf(CUSTOMER)),
			patternOf("/loyalty/points/redeem"									, setOf(STORE_MANAGER)),
			patternOf("/advertisement",POST, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf("/chat-widget-setting/create",POST, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf("/chat-widget-setting/publish",POST, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf("/chat-widget-setting/get-unpublished",GET, setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER)),
			patternOf( "/post/save" , POST                              , getAllRoles()),
			patternOf( "/post/unsave"       ,POST                         , getAllRoles()),
			patternOf( "/post/saved"        ,GET                        , getAllRoles()),
			patternOf( "/follow/users/list"        ,GET                        , getAllRoles()),
			patternOf( "/service"                     ,POST                        , setOf(NASNAV_ADMIN)),
			patternOf( "/service/{id:\\d+}"           ,PUT                         , setOf(NASNAV_ADMIN)),
			patternOf( "/service/{id:\\d+}"           ,DELETE                      , setOf(NASNAV_ADMIN)),
			patternOf( "/service/{id:\\d+}"           ,GET                         , setOf(NASNAV_ADMIN)),
			patternOf( "/service/org"                 ,GET                         , setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN)),
			patternOf( "/service/org"                 ,PUT                         , setOf(NASNAV_ADMIN, ORGANIZATION_ADMIN)),
			patternOf("/contactUs/**",GET,getAllRoles()),
			patternOf("/organization/add/image/type",POST),
			patternOf( "/organization/delete/image/type",DELETE),
			patternOf( "/organization/update/image/type",POST),
			patternOf( "/organization/images/types",GET),
			patternOf( "/**")
	);



    private List<AuthPattern> PUBLIC_URLS =
            	asList(
						patternOf("/callbacks/**")
					    , patternOf("/360view/**"						, HttpMethod.GET)
						, patternOf("/room/shop"								, GET)
						, patternOf("/room/shop/list"						, GET)
						, patternOf("/room/event"								, GET)
						, patternOf("/room/event/list"						, GET)
            			, patternOf("/navbox/**")
                        , patternOf("/user/recover")
                        , patternOf("/user/recovery/otp-verify")
                        , patternOf("/user/login/**")
                    	, patternOf("/nasnav/token", POST)
                    	, patternOf("/nasnav/token/jwks.json", GET)
                        , patternOf("/user/register")
                        , patternOf("/user/google_register")
						, patternOf("/user/v2/register")
						, patternOf("/user/v2/register/activate")
						, patternOf("/user/v2/register/otp/activate")
						, patternOf("/user/v2/employee/otp/activate")
						, patternOf("/user/v2/register/activate/resend")
						, patternOf( "/user/subscribe")
						, patternOf( "/user/subscribe/activate")
						, patternOf("/employee-user-heart-beats-logs/list-active-employee", GET)
                        , patternOf("/payment/**")
                        , patternOf("/product/bundles"					, HttpMethod.GET)
                        , patternOf("/package"					    , HttpMethod.GET)
                        , patternOf("/product/info"					, HttpMethod.GET)
                        , patternOf("/product/image"					, HttpMethod.GET)
                        , patternOf("/product/model3d/**"				, HttpMethod.GET)
                        , patternOf("/product/variant"					, HttpMethod.GET)
		                , patternOf("/organization/payments"			, HttpMethod.GET)
						,patternOf( "/organization/register"		    ,HttpMethod.POST)
                        , patternOf("/organization/brands"				, HttpMethod.GET)
                        , patternOf("/organization/products_features"	, HttpMethod.GET)
                        , patternOf("/swagger**/**")		//for development only
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
						, patternOf("/advertisement", HttpMethod.GET)
						, patternOf("/subscription/stripe/webhook"			,HttpMethod.POST)
						, patternOf("/frontend/setting", GET)
						, patternOf("/chat-widget-setting/get-published", GET)
						, patternOf("/contactUs**",POST)
						, patternOf("/bank/deposit/bc**",POST)
						, patternOf("/videochat/credentials",GET)


            );

    public SecurityConfiguration(AuthorizationRequestRepository<OAuth2AuthorizationRequest> oAuth2RequestRepository, CustomOAuth2UserService oAuth2UserService, OidcUserService customOidcUserService, OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler, OAuth2AuthenticationFailureHandler oAuth2FailureHandler) {
        this.oAuth2RequestRepository = oAuth2RequestRepository;
        this.oAuth2UserService = oAuth2UserService;
        this.customOidcUserService = customOidcUserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.oAuth2FailureHandler = oAuth2FailureHandler;

        //allow created threads to inherit the parent thread security context
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);

        List<RequestMatcher> protectedrequestMatcherList = permissions.stream().map(this::toAntPathRequestMatcher).collect(toList());
        protectedUrlList = new OrRequestMatcher(protectedrequestMatcherList);


        List<RequestMatcher> publicRequestMatcherList = PUBLIC_URLS.stream().map(this::toAntPathRequestMatcher).collect(toList());
        publicUrlList = new OrRequestMatcher(publicRequestMatcherList);
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        //we need to ignore the public url's from the whole security
        //instead of just permitting it for all users using http.antMatchers(url).permitAll().
        //Because we authenticate using a custom authentication filter, which apparently is
        //applied in all cases of authentication.
        //So we need to bypass the request of the public url from the whole security filter chain.

        return web -> web.ignoring().requestMatchers(publicUrlList);
    }

    private void customizeStaticResourceRequests(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorize) {
        IpAddressMatcher hasIpAddress = new IpAddressMatcher("127.0.0.1");
        authorize
                .requestMatchers(AntPathRequestMatcher.antMatcher(GET, STATIC_FILES_URL + "/**"))
                .access((authentication, context) -> new AuthorizationDecision(hasIpAddress.matches(context.getRequest())));
    }

    private void customizeProtectedResourceRequests(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorize) {
        permissions.forEach(authPattern ->
                authorize.requestMatchers(authPattern.getHttpMethod(), authPattern.getUrlPattern())
                        .hasAnyAuthority(toArray(authPattern.getRoles())));
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider,
                                                JwtAuthenticationProvider jwtAuthenticationProvider) {
        return new ProviderManager(authenticationProvider, jwtAuthenticationProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authenticationManager) throws Exception {

        http
                .headers(headers -> headers.addHeaderWriter(contentTypeHeaderWriter())) //add content-type='application/json' to security responses
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(this::customizeStaticResourceRequests)
                .authorizeHttpRequests(this::customizeProtectedResourceRequests)
                //.oauth2ResourceServer(oauth2 -> oauth2.authenticationManagerResolver(managerResolver))
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .addFilterBefore(authenticationFilter(authenticationManager), AnonymousAuthenticationFilter.class)
                //return a custom response 403 with a body containing {success=false} , don't know why we need it
                .exceptionHandling(exceptionHandler -> exceptionHandler.accessDeniedHandler(new NasnavAccessDeniedHandler()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);

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

        return http.build();
    }

    AuthenticationFilter authenticationFilter(AuthenticationManager authenticationManager) {
        final AuthenticationFilter filter = new AuthenticationFilter(protectedUrlList);
        filter.setAuthenticationManager(authenticationManager);

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

    private static Set<Roles> getAllRoles() {
        return new HashSet<>(Arrays.asList(Roles.values()));
    }

    private static Set<Roles> getNonCustomersRoles() {
        Set<Roles> roles = getAllRoles();
        roles.remove(Roles.CUSTOMER);
        return roles;
    }

    private static Set<Roles> getNonNasnavRoles() {
        return setOf(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER,
                ORGANIZATION_EMPLOYEE, STORE_MANAGER, STORE_EMPLOYEE);
    }

    private String[] toArray(Set<Roles> set) {
        return set.stream().map(Roles::getValue).toArray(String[]::new);
    }


    private static HashSet<Roles> setOf(Roles... roles) {
        return new HashSet<>(Arrays.asList(roles));
    }


    private static AuthPattern patternOf(String urlPattern, Set<Roles> roles) {
        return patternOf(urlPattern, null, roles);
    }

    private static AuthPattern patternOf(String urlPattern, HttpMethod method) {
        return patternOf(urlPattern, method, getAllRoles());
    }


    private static AuthPattern patternOf(String urlPattern) {
        return patternOf(urlPattern, null, getAllRoles());
    }


    private static AuthPattern patternOf(String urlPattern, HttpMethod method, Set<Roles> roles) {
        return new AuthPattern(urlPattern, method, roles);
    }


    private AntPathRequestMatcher toAntPathRequestMatcher(AuthPattern pattern) {
        String method = Optional.ofNullable(pattern.getHttpMethod())
                .map(HttpMethod::toString)
                .orElse(null);
        return new AntPathRequestMatcher(pattern.getUrlPattern(), method);
    }
}
