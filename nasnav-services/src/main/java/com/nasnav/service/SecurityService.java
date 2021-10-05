package com.nasnav.service;

import com.nasnav.dto.UserDTOs;
import com.nasnav.enumerations.Roles;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.UserTokensEntity;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.model.security.UserAuthenticationData;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SecurityService {
	
	Optional<UserAuthenticationData> findUserDetailsByAuthToken(String token);

    UserApiResponse login(UserDTOs.UserLoginObject body) ;

    UserApiResponse logout(String token);

    BaseUserEntity getCurrentUser();
    
    Long getCurrentUserOrganizationId();
    Long getCurrentUserShopId();
    OrganizationEntity getCurrentUserOrganization();

    Boolean userHasRole(BaseUserEntity user, Roles role);
    Boolean userHasRole(Roles role);
    Boolean currentUserHasRole(Roles role);
    Boolean currentUserIsCustomer();

	UserApiResponse socialLogin(String socialLoginToken) throws BusinessException;

	UserApiResponse login(BaseUserEntity userEntity, boolean rememberMe) ;
	
	UserTokensEntity extendUserExpirationTokenIfNeeded(UserTokensEntity token);

    UserApiResponse logoutAll(BaseUserEntity entity);

    UserApiResponse logoutAll();

	Optional<BaseUserEntity> getCurrentUserOptional();

    boolean currentUserHasMaxRoleLevelOf(Roles role);

    Set<Roles> getCurrentUserRoles();

    Integer getYeshteryState();
}
