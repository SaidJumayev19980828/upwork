package com.nasnav.service;

import com.nasnav.AppConfig;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dao.CommonUserRepository;
import com.nasnav.dao.OAuth2UserRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.UserTokenRepository;
import com.nasnav.dto.UserDTOs.UserLoginObject;
import com.nasnav.enumerations.Roles;
import com.nasnav.enumerations.UserStatus;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.UserApiResponse;
import com.nasnav.security.oauth2.exceptions.InCompleteOAuthRegistration;
import com.nasnav.service.helpers.UserServicesHelper;
import com.nasnav.service.model.security.UserAuthenticationData;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.cache.annotation.CacheResult;
import javax.servlet.http.Cookie;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.nasnav.cache.Caches.USERS_BY_TOKENS;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.constatnts.EntityConstants.AUTH_TOKEN_VALIDITY;
import static com.nasnav.constatnts.EntityConstants.NASNAV_DOMAIN;
import static com.nasnav.enumerations.UserStatus.NOT_ACTIVATED;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpStatus.*;

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

	@Autowired
	private UserServicesHelper helper;



	@Override
	@CacheResult(cacheName = USERS_BY_TOKENS)
	public Optional<UserAuthenticationData> findUserDetailsByAuthToken(String token){
		return ofNullable(token)
		 		.flatMap(this::getUserByAuthenticationToken)
		 		.map(this::createUserAuthData);
	}

	
	

	private Optional<UserTokensEntity> getUserByAuthenticationToken(String token) {
		UserTokensEntity userTokens = userTokenRepo.getUserEntityByToken(token);
		if (userTokens == null || isExpired(userTokens)) {
			return empty();
		}
		return ofNullable(userTokens);
	}
	
	
	
	
	private boolean isExpired(UserTokensEntity userTokens) {
		return ofNullable(userTokens)
				.map(UserTokensEntity::getUpdateTime)
				.map(updateTime -> Duration.between(updateTime, now()))
				.filter(liveTime -> liveTime.getSeconds() >= AUTH_TOKEN_VALIDITY)
				.isPresent();					
	}



	
	

	private UserAuthenticationData createUserAuthData(UserTokensEntity userTokens) {
		BaseUserEntity userEntity = 
				userTokens
				.getBaseUser()
				.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, U$LOG$0001));
		
		List<GrantedAuthority> roles = getUserRoles(userEntity);
		User user= new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), true, true, true, true,roles);
        return new UserAuthenticationData(user, userEntity, userTokens);		
	}


	
	
	
	
	@Override
	@Transactional
	@CacheEvict(cacheNames = {USERS_BY_TOKENS})
	public UserApiResponse logout(String token) {
		userTokenRepo.deleteByToken(token);
		Cookie c = createCookie(null, true);

		return new UserApiResponse(c);
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = {USERS_BY_TOKENS})
	public UserApiResponse logoutAll(BaseUserEntity user) {
		if ( user instanceof EmployeeUserEntity) {
			userTokenRepo.deleteByEmployeeUserEntity( (EmployeeUserEntity)user);
		} else {
			userTokenRepo.deleteByUserEntity( (UserEntity) user);
		}
		Cookie c = createCookie(null, true);

		return new UserApiResponse(c);
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = {USERS_BY_TOKENS})
	public UserApiResponse logoutAll() {
		BaseUserEntity user = getCurrentUser();
		return logoutAll(user);
	}



	
	
	
	private List<GrantedAuthority> getUserRoles(BaseUserEntity userEntity) {
		return userRepo.getUserRoles(userEntity).stream()												
						.map(SimpleGrantedAuthority::new)
						.collect(toList());
	}





	@Override
	public UserApiResponse login(UserLoginObject loginData) {

		if(invalidLoginData(loginData)) {
			throwInvalidCredentialsException();
		}

		BaseUserEntity userEntity = userRepo.getByEmailIgnoreCaseAndOrganizationId(loginData.getEmail(), loginData.getOrgId(), loginData.isEmployee());

		validateLoginUser(userEntity);
		validateUserPassword(loginData, userEntity);

		return login(userEntity, loginData.rememberMe);
	}





	private void validateUserPassword(UserLoginObject loginData, BaseUserEntity userEntity) {

		boolean accountNeedActivation = isEmployeeUserNeedActivation(userEntity);
		if (accountNeedActivation) {
			throw new RuntimeBusinessException(LOCKED,  U$LOG$0003);
		}
		
		boolean passwordMatched = passwordEncoder.matches(loginData.password, userEntity.getEncryptedPassword());		
		if(!passwordMatched) {
			throwInvalidCredentialsException();
		}
	}




	@Override
	public UserApiResponse login(BaseUserEntity userEntity, boolean rememberMe) {
		UserPostLoginData userData = updatePostLogin(userEntity);

		Cookie cookie = createCookie(userData.getToken(), rememberMe);

		return createSuccessLoginResponse(userData, cookie);
	}


	
	
	
	
	private Cookie createCookie(String token, boolean rememberMe) {
		Cookie cookie = new Cookie("User-Token", token);

		cookie.setHttpOnly(true);
		cookie.setDomain(NASNAV_DOMAIN);
		cookie.setPath("/");

		if (rememberMe) {
			//TODO: >>> the validity should be extended when the user uses the token. 
			//as we don't need a database write each time a request is sent with a token, we can should instead do this extension
			//if the token is going to expire in < 1/6 of the validity time for example.
			//this will probably be added after the token security check.
			//TODO: >>> the extension functionality will need a unit test , but that will need  AUTH_TOKEN_VALIDITY to be variable, so that we 
			//decrease it to 1 second for example and make Thread.sleep to wait until it becomes nearly invalidated.
			//which will require it to be in seconds instead of hours as well.
			cookie.setMaxAge(AUTH_TOKEN_VALIDITY);
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
			throw new RuntimeBusinessException(LOCKED,  U$LOG$0004);
		}
		
		if (isUserDeactivated(userEntity)) { // NOSONAR
			throw new RuntimeBusinessException(LOCKED,  U$LOG$0003);
		}
	}


	private boolean isUserDeactivated(BaseUserEntity user) {
		return user.getUserStatus().equals(NOT_ACTIVATED.getValue());
	}



	private boolean invalidLoginData(UserLoginObject loginData) {
		return loginData == null || isBlankOrNull(loginData.email) ;
	}





	private void throwInvalidCredentialsException() {
		throw new RuntimeBusinessException(UNAUTHORIZED, U$LOG$0002);
	}
	
	
	
	
	private boolean isEmployeeUserNeedActivation(BaseUserEntity userEntity) {
		String encPassword = userEntity.getEncryptedPassword();
		return StringUtils.isBlankOrNull(encPassword) || EntityConstants.INITIAL_PASSWORD.equals(encPassword);
	}
	
	
	
	
	
	private boolean isAccountLocked(BaseUserEntity userEntity) {
		return userEntity.getUserStatus().equals(UserStatus.ACCOUNT_SUSPENDED.getValue());
	}




	public UserPostLoginData updatePostLogin(BaseUserEntity userEntity) {
		LocalDateTime currentSignInDate = userEntity.getCurrentSignInDate();

		String authToken = generateUserToken(userEntity);
		
		userEntity.setLastSignInDate(currentSignInDate);
		userEntity.setCurrentSignInDate(LocalDateTime.now());
		userEntity.setAuthenticationToken(authToken);
		BaseUserEntity savedUserData = userRepo.saveAndFlush(userEntity);

		return new UserPostLoginData(savedUserData, authToken);
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
	
	
	
	
	
	
	
	public UserApiResponse createSuccessLoginResponse(UserPostLoginData userData, Cookie cookie) {
		Long shopId = 0L;
		BaseUserEntity userEntity = userData.getUserEntity();
		if(userEntity instanceof EmployeeUserEntity) {
			shopId = ofNullable(EmployeeUserEntity.class.cast(userEntity).getShopId()).orElse(0L);
		}
		
		Long orgId = ofNullable(userEntity.getOrganizationId()).orElse(0L);
		List<String> userRoles = userRepo.getUserRoles(userEntity);

		return new UserApiResponse(userEntity.getId(), cookie.getValue(), userRoles, orgId, shopId,
								   userEntity.getName(), userEntity.getEmail(), cookie);
	}





	@Override
	public BaseUserEntity getCurrentUser() {
		return getCurrentUserOptional()
					.orElseThrow(()-> new IllegalStateException("Could not retrieve current user!"));
	}
	
	
	
	@Override
	public Optional<BaseUserEntity> getCurrentUserOptional() {
		return ofNullable( SecurityContextHolder.getContext() )
					.map(SecurityContext::getAuthentication)
					.map(Authentication::getDetails)
					.map(BaseUserEntity.class::cast);
	}


	@Override
	public Set<Roles> getCurrentUserRoles(){
		return getCurrentUserRolesNames()
				.stream()
				.map(Roles::fromString)
				.collect(toSet());
	}


	@Override
	public boolean currentUserHasMaxRoleLevelOf(Roles role) {
		var currentUserRoles = getCurrentUserRoles();
		return helper.hasMaxRoleLevelOf(role, currentUserRoles);
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
	public Boolean userHasRole(Roles role) {
		return getCurrentUserRolesNames()
				.stream()
				.anyMatch(auth -> Objects.equals( auth, role.getValue()));
	}



	private List<String> getCurrentUserRolesNames() {
		return ofNullable(SecurityContextHolder.getContext())
				.map(SecurityContext::getAuthentication)
				.map(Authentication::getAuthorities)
				.orElse(emptyList())
				.stream()
				.map(GrantedAuthority::getAuthority)
				.filter(Objects::nonNull)
				.collect(toList());
	}


	@Override
	public Boolean userHasRole(BaseUserEntity user, Roles role) {
		return userRepo.getUserRoles(user)
				.stream()
				.anyMatch(auth -> Objects.equals( auth, role.getValue()));
	}



	@Override
	public Boolean currentUserHasRole(Roles role) {
		return userHasRole(role);
	}



	@Override
	public Boolean currentUserIsCustomer() {
		BaseUserEntity user = getCurrentUser();
		return user instanceof UserEntity;
	}


	@Override
	public UserApiResponse socialLogin(String socialLoginToken) {
		BaseUserEntity userEntity = getUserBySocialLoginToken(socialLoginToken);
		
		validateLoginUser(userEntity);			
		
		return login(userEntity, false);
	}





	private BaseUserEntity getUserBySocialLoginToken(String socialLoginToken) {
		if( !oAuthUserRepo.existsByLoginToken(socialLoginToken)) {
			throw new RuntimeBusinessException(UNAUTHORIZED, UXACTVX0006, socialLoginToken);
		}
		return oAuthUserRepo
				.findByLoginToken(socialLoginToken)
				.map(OAuth2UserEntity::getUser)
				.orElseThrow(() -> new InCompleteOAuthRegistration());
	}




	@Override
	@CacheEvict(cacheNames = {USERS_BY_TOKENS}, key = "#userToken.token")
	public UserTokensEntity extendUserExpirationTokenIfNeeded(UserTokensEntity userToken) {
		if(isSemiExpiredToken(userToken)) {
			userToken.setUpdateTime(now());
			return userTokenRepo.saveAndFlush(userToken);
		}
		return userToken;
	}




	private boolean isSemiExpiredToken(UserTokensEntity token) {
		return ofNullable(token)
				.map(UserTokensEntity::getUpdateTime)
				.map(updateTime -> Duration.between(updateTime, now()))
				.filter(liveTime -> liveTime.getSeconds() >= (long)(0.7*AUTH_TOKEN_VALIDITY))
				.isPresent();			
	}
	
}





@Data
@AllArgsConstructor
class UserPostLoginData{
	private BaseUserEntity userEntity;
	private String token;
}

