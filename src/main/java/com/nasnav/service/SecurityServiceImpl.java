package com.nasnav.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dto.UserDTOs.UserLoginObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.EntityValidationException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.response.ApiResponseBuilder;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;

@Service
public class SecurityServiceImpl implements SecurityService {
	
	@Autowired
	private CommonUserRepository  userRepo;
	
		
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	
	@Autowired
	private EmployeeUserRepository empRepo;
	
	
	@Autowired
	private OrganizationRepository orgRepo;
	
	
	@Override
	public Optional<UserDetails> findUserByAuthToken(String token){
		return Optional.ofNullable(token)
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
		
		
		BaseUserEntity userEntity = userRepo.getByEmailAndOrganizationId(loginData.email, loginData.orgId);
		
		
		if(userEntity == null) {
			throwInvalidCredentialsException();
		}
		
		
		boolean accountNeedActivation = isUserNeedActivation(userEntity);
		if (accountNeedActivation) {
			UserApiResponse failedLoginResponse = EntityUtils
					.createFailedLoginResponse(Collections.singletonList(ResponseStatus.NEED_ACTIVATION));
			throw new EntityValidationException("NEED_ACTIVATION ", failedLoginResponse, HttpStatus.LOCKED);
		}
		
		
		boolean passwordMatched = passwordEncoder.matches(loginData.password, userEntity.getEncryptedPassword());		
		if(!passwordMatched) {
			throwInvalidCredentialsException();
		}		
		
		if (isAccountLocked(userEntity)) { // NOSONAR
			UserApiResponse failedLoginResponse = EntityUtils
					.createFailedLoginResponse(Collections.singletonList(ResponseStatus.ACCOUNT_SUSPENDED));
			throw new EntityValidationException("ACCOUNT_SUSPENDED ", failedLoginResponse, HttpStatus.LOCKED);
		}
		
		// generate new AuthenticationToken and perform post login updates
		userEntity = updatePostLogin(userEntity);
		return createSuccessLoginResponse(userEntity);				
	}





	private boolean invalidLoginData(UserLoginObject loginData) {
		return loginData == null || StringUtils.isBlankOrNull(loginData.email) || StringUtils.isBlankOrNull(loginData.orgId);
	}





	private void throwInvalidCredentialsException() {
		UserApiResponse failedLoginResponse = EntityUtils
				.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INVALID_CREDENTIALS));
		throw new EntityValidationException("INVALID_CREDENTIALS ", failedLoginResponse, HttpStatus.UNAUTHORIZED);
	}
	
	
	
	
	private boolean isUserNeedActivation(BaseUserEntity userEntity) {
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
	public EmployeeUserEntity getCurrentUser() {
		return Optional.ofNullable( SecurityContextHolder.getContext() )
						.map(c -> c.getAuthentication())
						.map(Authentication::getName)
						.map(empRepo::getOneByEmail)
						.orElseThrow(()-> new IllegalStateException("Could not retrieve current user!"));
	}


	
	

	@Override
	public Long getCurrentUserOrganizationId() {
		return Optional.ofNullable( getCurrentUser() )
						.map(EmployeeUserEntity::getOrganizationId)
						.orElseThrow(() -> new IllegalStateException("Current User has no organization!"));
	}





	@Override
	public OrganizationEntity getCurrentUserOrganization() {
		return Optional.ofNullable( getCurrentUser() )
						.map(EmployeeUserEntity::getOrganizationId)
						.flatMap(orgRepo::findById)
						.orElseThrow(() -> new IllegalStateException("Current User has no organization!"));
	}	
	
}

