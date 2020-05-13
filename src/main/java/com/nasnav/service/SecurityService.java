package com.nasnav.service;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;

import com.nasnav.dto.UserDTOs;
import com.nasnav.enumerations.Roles;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.response.UserApiResponse;

public interface SecurityService {
	
	Optional<UserDetails> findUserByAuthToken(String token);

    Optional<BaseUserEntity> getUserByAuthenticationToken(String token);
	/**
     * login user to system
     *
     * @param body json object containing email and password
     * @return UserApiResponse object holding the status
	 * @throws BusinessException 
     */
    UserApiResponse login(UserDTOs.UserLoginObject body) throws BusinessException;

    UserApiResponse logout(String token);

    BaseUserEntity getCurrentUser();
    
    Long getCurrentUserOrganizationId();
    Long getCurrentUserShopId();
    OrganizationEntity getCurrentUserOrganization();

    Boolean userHasRole(BaseUserEntity user, Roles role);
    Boolean currentUserHasRole(Roles role);

	UserApiResponse socialLogin(String socialLoginToken) throws BusinessException;

	UserApiResponse login(BaseUserEntity userEntity, boolean rememberMe) throws BusinessException;
}
