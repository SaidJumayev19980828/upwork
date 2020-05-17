package com.nasnav.service;

import static com.nasnav.cache.Caches.USER_TOKENS;
import static com.nasnav.commons.utils.EntityUtils.createFailedLoginResponse;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.response.ResponseStatus.*;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.LOCKED;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.nasnav.AppConfig;
import com.nasnav.dao.UserTokenRepository;
import com.nasnav.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dao.CommonUserRepository;
import com.nasnav.dao.OAuth2UserRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dto.UserDTOs.UserLoginObject;
import com.nasnav.enumerations.Roles;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.EntityValidationException;
import com.nasnav.response.ApiResponseBuilder;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;
import com.nasnav.security.oauth2.exceptions.InCompleteOAuthRegisteration;
import org.springframework.transaction.annotation.Transactional;

import javax.cache.annotation.CacheResult;
import javax.servlet.http.Cookie;

@Service
public class SecurityServiceImpl implements SecurityService {
	
	@Autowired
	private CommonUserRepository  userRepo;
	
		
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	
	@Autowired
	private OrganizationRepository orgRepo;
	
	
	@Autowired
	private OAuth2UserRepository oAuthUserRepo;
	
	
	@Autowired
	private UserService userService;

	@Autowired
	private UserTokenRepository userTokenRepo;

	@Autowired
	private AppConfig config;



	@Override
	public Optional<UserDetails> findUserByAuthToken(String token){
		return ofNullable(token)
		 		.flatMap(this::getUserByAuthenticationToken)//userRepo::findByAuthenticationToken)
		 		.flatMap(this::getUser);
	}


	@Override
	//TODO: >>> need to add caching to this method
	//TODO: >>> the cache entry of a specific token should be evicted when logout is called with that specific token.
	@CacheResult(cacheName = USER_TOKENS)
	public Optional<BaseUserEntity> getUserByAuthenticationToken(String token) {
		UserTokensEntity userTokens = userTokenRepo.getUserEntityByToken(token);
		if (userTokens == null)
			return Optional.empty();

		if (userTokens.getUserEntity() != null)
			return ofNullable(userTokens.getUserEntity());

		return ofNullable(userTokens.getEmployeeUserEntity());
	}
	
	
	
	
	private Optional<UserDetails> getUser(BaseUserEntity userEntity) {
		List<GrantedAuthority> roles = getUserRoles(userEntity);
		User user= new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), true, true, true, true,roles);
        return Optional.of(user);		
	}


	@Override
	@Transactional
	@CacheEvict(allEntries = true, cacheNames = {USER_TOKENS})
	public UserApiResponse logout(String token) {
		userTokenRepo.deleteByToken(token);
		Cookie c = createCookie(null, true);

		return new ApiResponseBuilder().setCookie(c).setSuccess(true).build();
	}


	private List<GrantedAuthority> getUserRoles(BaseUserEntity userEntity) {
		
		return userRepo.getUserRoles(userEntity).stream()												
								.map(SimpleGrantedAuthority::new)
								.collect(toList());
	}





	@Override
	public UserApiResponse login(UserLoginObject loginData) throws BusinessException {
		
		if(invalidLoginData(loginData)) {
			throwInvalidCredentialsException();
		}
		
		BaseUserEntity userEntity = userRepo.getByEmailIgnoreCaseAndOrganizationId(loginData.email, loginData.orgId, loginData.employee);
		
		validateLoginUser(userEntity);			
		validateUserPassword(loginData, userEntity);

		return login(userEntity, loginData.rememberMe);
	}





	private void validateUserPassword(UserLoginObject loginData, BaseUserEntity userEntity) {

		boolean accountNeedActivation = isEmployeeUserNeedActivation(userEntity);
		if (accountNeedActivation) {
			UserApiResponse failedLoginResponse = 
					createFailedLoginResponse(singletonList(NEED_ACTIVATION));
			throw new EntityValidationException("NEED_ACTIVATION ", failedLoginResponse, LOCKED);
		}
		
		boolean passwordMatched = passwordEncoder.matches(loginData.password, userEntity.getEncryptedPassword());		
		if(!passwordMatched) {
			throwInvalidCredentialsException();
		}
	}




	@Override
	public UserApiResponse login(BaseUserEntity userEntity, boolean rememberMe) throws BusinessException {
		// generate new AuthenticationToken and perform post login updates
		userEntity = updatePostLogin(userEntity);

		Cookie cookie = createCookie(userEntity.getAuthenticationToken(), rememberMe);

		return createSuccessLoginResponse(userEntity, cookie);
	}


	private Cookie createCookie(String token, boolean rememberMe) {
		Cookie cookie = new Cookie("User-Token", token);

		cookie.setHttpOnly(true);
		cookie.setDomain(EntityConstants.NASNAV_DOMAIN);
		cookie.setPath("/");

		if (rememberMe) {
			//TODO: >>> the validity should be extended when the user uses the token. 
			//as we don't need a database write each time a request is sent with a token, we can should instead do this extension
			//if the token is going to expire in < 1/6 of the validity time for example.
			//this will probably be added after the token security check.
			//TODO: >>> the extension functionality will need a unit test , but that will need  AUTH_TOKEN_VALIDITY to be variable, so that we 
			//decrease it to 1 second for example and make Thread.sleep to wait until it becomes nearly invalidated.
			//which will require it to be in seconds instead of hours as well.
			cookie.setMaxAge(EntityConstants.AUTH_TOKEN_VALIDITY);
		}

		if (config.secureTokens)
			cookie.setSecure(true);

		return cookie;
	}


	private void validateLoginUser(BaseUserEntity userEntity) {
		if(userEntity == null) {
			throwInvalidCredentialsException();
		}
		
		if (isAccountLocked(userEntity)) { // NOSONAR
			UserApiResponse failedLoginResponse = createFailedLoginResponse(singletonList(ACCOUNT_SUSPENDED));
			throw new EntityValidationException("ACCOUNT_SUSPENDED ", failedLoginResponse, LOCKED);
		}
		
		if (userService.isUserDeactivated(userEntity)) { // NOSONAR
			UserApiResponse failedLoginResponse = createFailedLoginResponse(singletonList(NEED_ACTIVATION));
			throw new EntityValidationException("ACCOUNT_INACTIVE", failedLoginResponse, LOCKED);
		}
	}






	private boolean invalidLoginData(UserLoginObject loginData) {
		return loginData == null || isBlankOrNull(loginData.email) || isBlankOrNull(loginData.orgId);
	}





	private void throwInvalidCredentialsException() {
		UserApiResponse failedLoginResponse = EntityUtils
				.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INVALID_CREDENTIALS));
		throw new EntityValidationException("INVALID_CREDENTIALS ", failedLoginResponse, HttpStatus.UNAUTHORIZED);
	}
	
	
	
	
	private boolean isEmployeeUserNeedActivation(BaseUserEntity userEntity) {
		String encPassword = userEntity.getEncryptedPassword();
		return StringUtils.isBlankOrNull(encPassword) || EntityConstants.INITIAL_PASSWORD.equals(encPassword);
	}
	
	
	
	
	
	private boolean isAccountLocked(BaseUserEntity userEntity) {
		// TODO : change implementation later
		return false;
	}




	@CacheEvict(allEntries = true, cacheNames = {USER_TOKENS})
	public BaseUserEntity updatePostLogin(BaseUserEntity userEntity) throws BusinessException {
		LocalDateTime currentSignInDate = userEntity.getCurrentSignInDate();
		userEntity.setLastSignInDate(currentSignInDate);
		userEntity.setCurrentSignInDate(LocalDateTime.now());

		String authToken = generateUserToken(userEntity);

		userEntity.setAuthenticationToken(authToken);

		return userRepo.saveAndFlush(userEntity);
	}


	private String generateUserToken(BaseUserEntity user) {
		UserTokensEntity token = new UserTokensEntity();
		token.setToken(StringUtils.generateUUIDToken());
        if (user instanceof EmployeeUserEntity) {
            token.setEmployeeUserEntity((EmployeeUserEntity) user);
        } else {
            token.setUserEntity((UserEntity) user);
        }
		userTokenRepo.save(token);

		return token.getToken();
	}
	
	
	
	private String generateAuthenticationToken() {
		//it is nearly impossible for type 4 UUID to be repeated by chance.
		//also each users table have unique index on auth tokens.
		return StringUtils.generateUUIDToken();		
	}
	
	
	
	
	
	public UserApiResponse createSuccessLoginResponse(BaseUserEntity userEntity, Cookie cookie) {
		Long shopId = 0L;
		if(userEntity instanceof EmployeeUserEntity)
			shopId = EmployeeUserEntity.class.cast(userEntity).getShopId();		
		
		Long organizationId = userEntity.getOrganizationId();
		
		return new ApiResponseBuilder()
					.setSuccess(true)
					.setEntityId( userEntity.getId() )
					.setName(userEntity.getName())
					.setEmail(userEntity.getEmail())
					.setToken( userEntity.getAuthenticationToken() )
					.setRoles( userRepo.getUserRoles(userEntity) )
					.setOrganizationId( organizationId != null ? organizationId : 0L)
					.setStoreId(shopId != null ? shopId : 0L)
					.setCookie(cookie)
					.build();
	}





	@Override
	public BaseUserEntity getCurrentUser() {
		return ofNullable( SecurityContextHolder.getContext() )
					.map(c -> c.getAuthentication())
					.map(Authentication::getDetails)
					.map(BaseUserEntity.class::cast)
					.orElseThrow(()-> new IllegalStateException("Could not retrieve current user!"));
	}


	
	

	@Override
	public Long getCurrentUserOrganizationId() {
		return ofNullable( getCurrentUser() )
				.map(BaseUserEntity::getOrganizationId)
				.orElseThrow(() -> new IllegalStateException("Current User has no organization!"));
	}


	@Override
	public Long getCurrentUserShopId() {
		return ofNullable( getCurrentUser() )
				.filter(user -> user instanceof EmployeeUserEntity)
				.map(user -> (EmployeeUserEntity)user)
				.map(EmployeeUserEntity::getShopId)
				.orElseThrow(() -> new IllegalStateException("Current User has no shop!"));
	}



	@Override
	public OrganizationEntity getCurrentUserOrganization() {
		return ofNullable( getCurrentUser() )
				.map(BaseUserEntity::getOrganizationId)
				.flatMap(orgRepo::findById)
				.orElseThrow(() -> new IllegalStateException("Current User has no organization!"));
	}





	@Override
	public Boolean userHasRole(BaseUserEntity user, Roles role) {
		return ofNullable( SecurityContextHolder.getContext() )
				.map(c -> c.getAuthentication())
				.map(Authentication::getAuthorities)
				.orElse(emptyList())
				.stream()
				.map(GrantedAuthority::getAuthority)
				.filter(Objects::nonNull)
				.anyMatch(auth -> Objects.equals( auth, role.getValue()));
	}





	@Override
	public Boolean currentUserHasRole(Roles role) {
		BaseUserEntity user = getCurrentUser();
		return userHasRole(user, role);
	}





	@Override
	public UserApiResponse socialLogin(String socialLoginToken) throws BusinessException {
		BaseUserEntity userEntity = getUserBySocialLoginToken(socialLoginToken);
		
		validateLoginUser(userEntity);			
		
		return login(userEntity, false);
	}





	private BaseUserEntity getUserBySocialLoginToken(String socialLoginToken) throws BusinessException {
		if( !oAuthUserRepo.existsByLoginToken(socialLoginToken)) {
			throw new BusinessException(
			               format("No User did OAuth2 login with token[%s]", socialLoginToken) 
			               , "INVALID_TOKEN"
			               , HttpStatus.UNAUTHORIZED);
		}
		
		return oAuthUserRepo.findByLoginToken(socialLoginToken)
							.map(OAuth2UserEntity::getUser)
							.orElseThrow(() -> new InCompleteOAuthRegisteration());
	}
	
}

