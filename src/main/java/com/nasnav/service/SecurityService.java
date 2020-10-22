package com.nasnav.service;

import java.util.Optional;

import com.nasnav.dto.UserDTOs;
import com.nasnav.enumerations.Roles;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.UserTokensEntity;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.model.security.UserAuthenticationData;

public interface SecurityService {
	
	Optional<UserAuthenticationData> findUserDetailsByAuthToken(String token);

    UserApiResponse login(UserDTOs.UserLoginObject body) ;

    UserApiResponse logout(String token);

    BaseUserEntity getCurrentUser();
    
    Long getCurrentUserOrganizationId();
    Long getCurrentUserShopId();
    OrganizationEntity getCurrentUserOrganization();

    Boolean userHasRole(Roles role);
    Boolean currentUserHasRole(Roles role);
    Boolean currentUserIsCustomer();

	UserApiResponse socialLogin(String socialLoginToken) throws BusinessException;

	UserApiResponse login(BaseUserEntity userEntity, boolean rememberMe) ;
	
	UserTokensEntity extendUserExpirationTokenIfNeeded(UserTokensEntity token);

    UserApiResponse logoutAll();

	Optional<BaseUserEntity> getCurrentUserOptional();
}
