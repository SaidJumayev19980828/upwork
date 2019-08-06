package com.nasnav.service;

import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dao.CommonUserRepository;
import com.nasnav.dao.RoleRepository;
import com.nasnav.dto.UserDTOs.UserLoginObject;
import com.nasnav.enumerations.Roles;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.EntityValidationException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.EntityUtils;
import com.nasnav.persistence.Role;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.ApiResponseBuilder;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SecurityServiceImpl implements SecurityService {
	
	@Autowired
	private CommonUserRepository  userRepo;
	
		
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	
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
		BaseUserEntity userEntity = userRepo.getByEmailAndOrganizationId(loginData.email, loginData.org_id);
		
		
		if(userEntity == null) {
			UserApiResponse failedLoginResponse = EntityUtils
					.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INVALID_CREDENTIALS));
			throw new EntityValidationException("INVALID_CREDENTIALS ", failedLoginResponse, HttpStatus.UNAUTHORIZED);
		}
		
		
		boolean accountNeedActivation = isUserNeedActivation(userEntity);
		if (accountNeedActivation) {
			UserApiResponse failedLoginResponse = EntityUtils
					.createFailedLoginResponse(Collections.singletonList(ResponseStatus.NEED_ACTIVATION));
			throw new EntityValidationException("NEED_ACTIVATION ", failedLoginResponse, HttpStatus.LOCKED);
		}
		
		
		boolean passwordMatched = passwordEncoder.matches(loginData.password, userEntity.getEncryptedPassword());		
		if(!passwordMatched) {
			UserApiResponse failedLoginResponse = EntityUtils
					.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INVALID_CREDENTIALS));
			throw new EntityValidationException("INVALID_CREDENTIALS ", failedLoginResponse, HttpStatus.UNAUTHORIZED);
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
	
	
	
	
	/**
	 * Check if passed user entity's account needs activation.
	 *
	 * @param userEntity User entity to be checked.
	 * @return true if current user entity's account needs activation.
	 */
	private boolean isUserNeedActivation(BaseUserEntity userEntity) {
		String encPassword = userEntity.getEncryptedPassword();
		return EntityUtils.isBlankOrNull(encPassword) || EntityConstants.INITIAL_PASSWORD.equals(encPassword);
	}
	
	
	
	
	/**
	 * Check if passed user entity's account is locked.
	 *
	 * @param userEntity User entity to be checked.
	 * @return true if current user entity's account is locked.
	 */
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
		return EntityUtils.generateUUIDToken();		
	}
	
	
	
	
	
	public UserApiResponse createSuccessLoginResponse(BaseUserEntity userEntity) {
		Long shopId = 0L;
		if(userEntity instanceof EmployeeUserEntity)
			shopId = EmployeeUserEntity.class.cast(userEntity).getShopId();		
		
		Long organizationId = userEntity.getOrganizationId();
		
		return new ApiResponseBuilder()
				.setSuccess(true)
				.setEntityId( userEntity.getId() )
				.setToken( userEntity.getAuthenticationToken() )
				.setRoles( userRepo.getUserRoles(userEntity) )
				.setOrganizationId( organizationId != null ? organizationId : 0L)
				.setStoreId(shopId != null ? shopId : 0L)
				.build();
	}	
	
}

