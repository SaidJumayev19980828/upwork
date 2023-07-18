package com.nasnav.service;

import com.nasnav.dto.UserDTOs;
import com.nasnav.enumerations.Roles;
import com.nasnav.enumerations.YeshteryState;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.UserTokensEntity;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.model.security.UserAuthenticationData;

import java.util.Optional;
import java.util.Set;

public interface SecurityService {
	
	Optional<UserAuthenticationData> findUserDetailsByAuthToken(String token);

    UserApiResponse login(UserDTOs.UserLoginObject body, YeshteryState state) ;

    UserApiResponse logout(String token, String cookieToken);

    BaseUserEntity getCurrentUser();

    BaseUserEntity getCurrentUserForOrg(Long orgId);

    Long getCurrentUserOrganizationId();
    Long getCurrentUserShopId();
    ShopsEntity getCurrentUserShop();
    OrganizationEntity getCurrentUserOrganization();

    Boolean userHasRole(BaseUserEntity user, Roles role);
    Boolean userHasRole(Roles role);
    Boolean currentUserHasRole(Roles role);
    Boolean currentUserIsCustomer();

    Boolean isShopAccessibleToCurrentUser(Long shopId);

	UserApiResponse socialLogin(String socialLoginToken, boolean yeshteryInstance) throws BusinessException;

	UserApiResponse login(BaseUserEntity userEntity, boolean rememberMe) ;

	UserApiResponse login(BaseUserEntity userEntity, boolean rememberMe, String notificationToken) ;

	UserTokensEntity extendUserExpirationTokenIfNeeded(UserTokensEntity token);

    UserApiResponse logoutAll(BaseUserEntity entity);

    UserApiResponse logoutAll();

	Optional<BaseUserEntity> getCurrentUserOptional();

    boolean currentUserHasMaxRoleLevelOf(Roles role);

    Set<Roles> getCurrentUserRoles();

    Integer getYeshteryState();

    Set<String> getValidEmployeeNotificationTokens(EmployeeUserEntity employee);

    Set<String> getValidEmployeeNotificationTokens(Set<EmployeeUserEntity> employee);

    Set<String> getValidUserNotificationTokens(UserEntity user);

    Set<String> getValidUserNotificationTokens(Set<UserEntity> user);

    Set<String> getInvalidEmployeeNotificationTokens(EmployeeUserEntity employee);

    Set<String> getInvalidUserNotificationTokens(UserEntity user);

    Set<String> getValidNotificationTokens(BaseUserEntity user);
}
