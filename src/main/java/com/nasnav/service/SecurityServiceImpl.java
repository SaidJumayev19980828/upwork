package com.nasnav.service;

import static com.nasnav.commons.utils.EntityUtils.createFailedLoginResponse;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.response.ResponseStatus.ACCOUNT_SUSPENDED;
import static com.nasnav.response.ResponseStatus.NEED_ACTIVATION;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.LOCKED;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.OAuth2UserEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.response.ApiResponseBuilder;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;
import com.nasnav.security.oauth2.exceptions.InCompleteOAuthRegisteration;

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


	@Override
	public Optional<UserDetails> findUserByAuthToken(String token){
		return ofNullable(token)
		 		.flatMap(userRepo::findByAuthenticationToken)
		 		.flatMap(this::getUser);
	}
	
	
	
	
	
	private Optional<UserDetails> getUser(BaseUserEntity userEntity) {		
		List<GrantedAuthority> roles = getUserRoles(userEntity);
		User user= new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), true, true, true, true,roles);
        return Optional.of(user);		
	}





	private List<GrantedAuthority> getUserRoles(BaseUserEntity userEntity) {
		
		return userRepo.getUserRoles(userEntity).stream()												
								.map(SimpleGrantedAuthority::new)
								.collect(Collectors.toList());
	}





	@Override
	public UserApiResponse login(UserLoginObject loginData) throws BusinessException {
		
		if(invalidLoginData(loginData)) {
			throwInvalidCredentialsException();
		}
		
		BaseUserEntity userEntity = userRepo.getByEmailIgnoreCaseAndOrganizationId(loginData.email, loginData.orgId, loginData.employee);
		
		validateLoginUser(userEntity);			
		validateUserPassword(loginData, userEntity);		
		
		return login(userEntity);				
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
	public UserApiResponse login(BaseUserEntity userEntity) throws BusinessException {
		// generate new AuthenticationToken and perform post login updates
		userEntity = updatePostLogin(userEntity);
		return createSuccessLoginResponse(userEntity);
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
	
	
	
	
	public BaseUserEntity updatePostLogin(BaseUserEntity userEntity) throws BusinessException {
		LocalDateTime currentSignInDate = userEntity.getCurrentSignInDate();
		userEntity.setLastSignInDate(currentSignInDate);
		userEntity.setCurrentSignInDate(LocalDateTime.now());
		userEntity.setAuthenticationToken(generateAuthenticationToken());
		return userRepo.saveAndFlush(userEntity);
	}
	
	
	
	
	private String generateAuthenticationToken() {
		//it is nearly impossible for type 4 UUID to be repeated by chance.
		//also each users table have unique index on auth tokens.
		return StringUtils.generateUUIDToken();		
	}
	
	
	
	
	
	public UserApiResponse createSuccessLoginResponse(BaseUserEntity userEntity) {
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
					.build();
	}





	@Override
	public BaseUserEntity getCurrentUser() {
		return Optional.ofNullable( SecurityContextHolder.getContext() )
					.map(c -> c.getAuthentication())
					.map(Authentication::getDetails)
					.map(BaseUserEntity.class::cast)
					.orElseThrow(()-> new IllegalStateException("Could not retrieve current user!"));
	}


	
	

	@Override
	public Long getCurrentUserOrganizationId() {
		return Optional.ofNullable( getCurrentUser() )
						.map(BaseUserEntity::getOrganizationId)
						.orElseThrow(() -> new IllegalStateException("Current User has no organization!"));
	}





	@Override
	public OrganizationEntity getCurrentUserOrganization() {
		return Optional.ofNullable( getCurrentUser() )
						.map(BaseUserEntity::getOrganizationId)
						.flatMap(orgRepo::findById)
						.orElseThrow(() -> new IllegalStateException("Current User has no organization!"));
	}





	@Override
	public Boolean userHasRole(BaseUserEntity user, Roles role) {
		List<GrantedAuthority> roles = getUserRoles(user);
		return roles.stream()
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
		
		return login(userEntity);	
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

