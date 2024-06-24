package com.nasnav.service.impl;

import com.nasnav.AppConfig;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dao.*;
import com.nasnav.dto.UserDTOs.UserLoginObject;
import com.nasnav.enumerations.Roles;
import com.nasnav.enumerations.UserStatus;
import com.nasnav.enumerations.YeshteryState;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.UserApiResponse;
import com.nasnav.security.oauth2.exceptions.InCompleteOAuthRegistration;
import com.nasnav.service.RoleService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.model.security.UserAuthenticationData;
import com.nasnav.service.yeshtery.YeshteryUserService;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.cache.annotation.CacheResult;
import javax.servlet.http.Cookie;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;

import static com.nasnav.cache.Caches.USERS_BY_TOKENS;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.constatnts.EntityConstants.AUTH_TOKEN_VALIDITY;
import static com.nasnav.constatnts.EntityConstants.NASNAV_DOMAIN;
import static com.nasnav.enumerations.UserStatus.NOT_ACTIVATED;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.lang.Boolean.TRUE;
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
    private CommonUserRepository userRepo;


    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private OrganizationRepository orgRepo;
    @Autowired
    private ShopsRepository shopsRepo;


    @Autowired
    private OAuth2UserRepository oAuthUserRepo;


    @Autowired
    private YeshteryUserService yeshteryUserService;

    @Autowired
    private UserTokenRepository userTokenRepo;

    @Autowired
    private AppConfig config;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionRepository permissionRepository;


    @Override
    @CacheResult(cacheName = USERS_BY_TOKENS)
    public Optional<UserAuthenticationData> findUserDetailsByAuthToken(String token) {
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
        User user = new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), true, true, true, true, roles);
        return new UserAuthenticationData(user, userEntity, userTokens);
    }


    @Override
    @Transactional
    @CacheEvict(cacheNames = {USERS_BY_TOKENS})
    public UserApiResponse logout(String headerToken, String cookieToken) {
        handleLogOut(getCurrentUser());
        Cookie c = createCookie(null, true);
        return new UserApiResponse(c);
    }

    private void handleLogOut(BaseUserEntity base) {
        if (base instanceof UserEntity user) {
            userTokenRepo.deleteByUserEntity(user);
        } else {
            userTokenRepo.deleteByEmployeeUserEntity((EmployeeUserEntity) base);
        }
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {USERS_BY_TOKENS})
    public UserApiResponse logoutAll(BaseUserEntity user) {
        if (user instanceof EmployeeUserEntity) {
            userTokenRepo.deleteByEmployeeUserEntity((EmployeeUserEntity) user);
        } else {
            userTokenRepo.deleteByUserEntity((UserEntity) user);
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
        return roleService.getUserRoles(userEntity).stream()
                .map(SimpleGrantedAuthority::new)
                .collect(toList());
    }


    @Override
    public UserApiResponse login(UserLoginObject loginData, YeshteryState state) {

        if (invalidLoginData(loginData)) {
            throwInvalidCredentialsException();
        }

        BaseUserEntity userEntity = userRepo.getByEmailIgnoreCaseAndOrganizationId(loginData.getEmail(),
                loginData.getOrgId(), loginData.isEmployee());

        validateLoginUser(userEntity);
        validateUserPassword(loginData, userEntity);

        return login(userEntity, loginData.rememberMe, loginData.getNotificationToken());
    }


    private void validateUserPassword(UserLoginObject loginData, BaseUserEntity userEntity) {

        boolean accountNeedActivation = isEmployeeUserNeedActivation(userEntity);
        if (accountNeedActivation) {
            throw new RuntimeBusinessException(LOCKED, U$LOG$0003);
        }

        boolean passwordMatched = passwordEncoder.matches(loginData.password, userEntity.getEncryptedPassword());
        if (!passwordMatched) {
            throwInvalidCredentialsException();
        }
    }


    @Override
    public UserApiResponse login(BaseUserEntity userEntity, boolean rememberMe) {
        return login(userEntity, rememberMe, null);
    }

    @Override
    public UserApiResponse login(BaseUserEntity userEntity, boolean rememberMe, String notificationToken) {
        UserPostLoginData userData = updatePostLogin(userEntity, notificationToken);

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
        if (userEntity == null) {
            throwInvalidCredentialsException();
        }

        if (isAccountLocked(userEntity)) { // NOSONAR
            throw new RuntimeBusinessException(LOCKED, U$LOG$0004);
        }

        if (isUserDeactivated(userEntity)) { // NOSONAR
            throw new RuntimeBusinessException(LOCKED, U$LOG$0003);
        }
    }


    private boolean isUserDeactivated(BaseUserEntity user) {
        return user.getUserStatus().equals(NOT_ACTIVATED.getValue());
    }


    private boolean invalidLoginData(UserLoginObject loginData) {
        return loginData == null || isBlankOrNull(loginData.email);
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


    public UserPostLoginData updatePostLogin(BaseUserEntity userEntity, String notificationToken) {
        LocalDateTime currentSignInDate = userEntity.getCurrentSignInDate();

        String authToken = generateUserToken(userEntity, notificationToken);

        userEntity.setLastSignInDate(currentSignInDate);
        userEntity.setCurrentSignInDate(LocalDateTime.now());
        userEntity.setAuthenticationToken(authToken);
        BaseUserEntity savedUserData = userRepo.saveAndFlush(userEntity);

        return new UserPostLoginData(savedUserData, authToken);
    }


    private String generateUserToken(BaseUserEntity user, String notificationToken) {
        UserTokensEntity token = new UserTokensEntity();
        token.setToken(StringUtils.generateUUIDToken());
        if (user instanceof EmployeeUserEntity) {
            token.setEmployeeUserEntity((EmployeeUserEntity) user);
        } else {
            token.setUserEntity((UserEntity) user);
        }
        userTokenRepo.save(token);
        UserTokensEntity userTokensEntity = userTokenRepo.getUserEntityByToken(token.getToken());
        userTokensEntity.setNotificationToken(notificationToken);
        userTokenRepo.save(userTokensEntity);

        return token.getToken();
    }


    public UserApiResponse createSuccessLoginResponse(UserPostLoginData userData, Cookie cookie) {
        Long shopId = 0L;
        BaseUserEntity userEntity = userData.getUserEntity();
        if (userEntity instanceof EmployeeUserEntity) {
            shopId = ofNullable(EmployeeUserEntity.class.cast(userEntity).getShopId()).orElse(0L);
        }

        Long orgId = ofNullable(userEntity.getOrganizationId()).orElse(0L);
        List<String> userRoles = roleService.getUserRoles(userEntity);

        return new UserApiResponse(userEntity.getId(), cookie.getValue(), userRoles, orgId, shopId,
                userEntity.getName(), userEntity.getEmail(), cookie);
    }


    @Override
    public BaseUserEntity getCurrentUser() {
        return getCurrentUserOptional()
                .orElseThrow(() -> new IllegalStateException("Could not retrieve current user!"));
    }

    @Override
    public BaseUserEntity getCurrentUserForOrg(Long orgId) {

        return getCurrentUserOptional()
                .map(user -> mapUserToOrgUser(user, orgId))
                .orElseThrow(() -> new IllegalStateException("Could not retrieve current user!"));
    }

    private BaseUserEntity mapUserToOrgUser(BaseUserEntity user, Long orgId) {
        if (config.isYeshteryInstance && user instanceof UserEntity) {
            return yeshteryUserService.getUserForOrg((UserEntity) user, orgId);
        }
        return user.getOrganizationId().equals(orgId) ? user : null;
    }

    @Override
    public Optional<BaseUserEntity> getCurrentUserOptional() {
        //TODO icomma extract method
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken token) {
            Jwt jwt = (Jwt) token.getPrincipal();
            String ORGANIZATION_ID_CLAIM = "orgId";
            String USER_ID_CLAIM = "userId";
            String EMPLYEE_CLAIM = "employee";
            String email = jwt.getSubject();
            String userIdClaimValue = jwt.getClaimAsString(USER_ID_CLAIM);
            String orgId = jwt.getClaimAsString(ORGANIZATION_ID_CLAIM);
            boolean isEmployeeClaimValue = jwt.getClaimAsBoolean(EMPLYEE_CLAIM);

            Optional<BaseUserEntity> userEntity = userRepo
                    .findById(Long.valueOf(userIdClaimValue), isEmployeeClaimValue);
            return userEntity;
        }
        Optional<BaseUserEntity> baseUserEntity = ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getDetails)
                .map(BaseUserEntity.class::cast);
        return baseUserEntity;
    }


    @Override
    public Set<Roles> getCurrentUserRoles() {
        return getCurrentUserRolesNames()
                .stream()
                .map(Roles::fromString)
                .collect(toSet());
    }

    @Override
    public Integer getYeshteryState() {
        return getCurrentUserOrganization().getYeshteryState();
    }


    @Override
    public boolean currentUserHasMaxRoleLevelOf(Roles role) {
        var currentUserRoles = getCurrentUserRoles();
        return roleService.hasMaxRoleLevelOf(role, new ArrayList<>(currentUserRoles));
    }


    @Override
    public Long getCurrentUserOrganizationId() {
        return ofNullable(getCurrentUser())
                .map(BaseUserEntity::getOrganizationId)
                .orElseThrow(() -> new IllegalStateException("Current User has no organization!"));
    }


    @Override
    public Long getCurrentUserShopId() {
        return ofNullable(getCurrentUser())
                .filter(user -> user instanceof EmployeeUserEntity)
                .map(user -> (EmployeeUserEntity) user)
                .map(EmployeeUserEntity::getShopId)
                .orElseThrow(() -> new IllegalStateException("Current User has no shop!"));
    }

    @Override
    public ShopsEntity getCurrentUserShop() {
        return ofNullable(getCurrentUser())
                .filter(user -> user instanceof EmployeeUserEntity)
                .map(user -> (EmployeeUserEntity) user)
                .map(EmployeeUserEntity::getShopId)
                .flatMap(shopsRepo::findById)
                .orElseThrow(() -> new IllegalStateException("Current User has no shop!"));
    }


    @Override
    public OrganizationEntity getCurrentUserOrganization() {
        return ofNullable(getCurrentUser())
                .map(BaseUserEntity::getOrganizationId)
                .flatMap(orgRepo::findById)
                .orElseThrow(() -> new IllegalStateException("Current User has no organization!"));
    }


    @Override
    public Boolean userHasRole(Roles role) {
        return getCurrentUserRolesNames()
                .stream()
                .anyMatch(auth -> Objects.equals(auth, role.getValue()));
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
        return roleService.getUserRoles(user)
                .stream()
                .anyMatch(auth -> Objects.equals(auth, role.getValue()));
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
    public UserApiResponse socialLogin(String socialLoginToken, boolean yeshteryInstance) {
        BaseUserEntity userEntity = getUserBySocialLoginToken(socialLoginToken);

        validateLoginUser(userEntity);

        if (yeshteryInstance) {
            yeshteryUserService.createYeshteryEntity(userEntity.getEmail(),
                    userEntity.getName(),
                    (UserEntity) userEntity,
                    config.yeshteryOrgId,
                    userEntity.getOrganizationId());
        }
        return login(userEntity, false);
    }


    private BaseUserEntity getUserBySocialLoginToken(String socialLoginToken) {
        if (!oAuthUserRepo.existsByLoginToken(socialLoginToken)) {
            throw new RuntimeBusinessException(UNAUTHORIZED, UXACTVX0006, socialLoginToken);
        }
        return oAuthUserRepo
                .findByLoginToken(socialLoginToken)
                .map(OAuth2UserEntity::getUser)
                .orElseThrow(() -> new InCompleteOAuthRegistration(socialLoginToken));
    }


    @Override
    @CacheEvict(cacheNames = {USERS_BY_TOKENS}, key = "#userToken.token")
    public UserTokensEntity extendUserExpirationTokenIfNeeded(UserTokensEntity userToken) {
        if (isSemiExpiredToken(userToken)) {
            userToken.setUpdateTime(now());
            return userTokenRepo.saveAndFlush(userToken);
        }
        return userToken;
    }


    private boolean isSemiExpiredToken(UserTokensEntity token) {
        return ofNullable(token)
                .map(UserTokensEntity::getUpdateTime)
                .map(updateTime -> Duration.between(updateTime, now()))
                .filter(liveTime -> liveTime.getSeconds() >= (long) (0.7 * AUTH_TOKEN_VALIDITY))
                .isPresent();
    }

    @Override
    public Boolean isShopAccessibleToCurrentUser(Long shopId) {
        Optional<OrganizationEntity> shopOrganization = shopsRepo.findById(shopId)
                .map(ShopsEntity::getOrganizationEntity);
        if (config.isYeshteryInstance && TRUE.equals(currentUserIsCustomer())) {
            Integer currentUserYeshteryState = getYeshteryState();
            return YeshteryState.ACTIVE.getValue().equals(currentUserYeshteryState)
                    && shopOrganization
                    .map(OrganizationEntity::getYeshteryState)
                    .filter(YeshteryState.ACTIVE.getValue()::equals).isPresent();
        }

        if (TRUE.equals(currentUserIsCustomer()) || TRUE.equals(currentUserHasRole(Roles.ORGANIZATION_ADMIN))
                || TRUE.equals(currentUserHasRole(Roles.ORGANIZATION_MANAGER))) {
            return shopOrganization.filter(getCurrentUserOrganization()::equals).isPresent();
        }

        return currentUserHasRole(Roles.STORE_MANAGER) && getCurrentUserShopId().equals(shopId);
    }

    public Set<String> getValidEmployeeNotificationTokens(EmployeeUserEntity employee) {
        Set<UserTokensEntity> tokenEntities = userTokenRepo.getDistinctByEmployeeUserEntityAndNotificationTokenNotNull(employee);
        return getValidNotificationTokens(tokenEntities);
    }

    @Override
    public Set<String> getValidUserNotificationTokens(UserEntity user) {
        Set<UserTokensEntity> tokenEntities = userTokenRepo.getDistinctByUserEntityAndNotificationTokenNotNull(user);
        return getValidNotificationTokens(tokenEntities);
    }

    @Override
    public Set<String> getValidNotificationTokensForOrgEmployees(Long orgId) {
        Set<UserTokensEntity> tokenEntities = userTokenRepo.getDistinctByEmployeeUserEntityOrganizationIdAndNotificationTokenNotNull(orgId);
        return getValidNotificationTokens(tokenEntities);
    }

    @Override
    public Set<String> getValidNotificationTokensForShopEmployees(Long shopId) {
        Set<UserTokensEntity> tokenEntities = userTokenRepo.getDistinctByEmployeeUserEntityShopIdAndNotificationTokenNotNull(shopId);
        return getValidNotificationTokens(tokenEntities);
    }


    private Set<String> getValidNotificationTokens(Set<UserTokensEntity> tokenEntities) {
        return tokenEntities.stream().filter(Predicate.not(this::isExpired))
                .map(UserTokensEntity::getNotificationToken).collect(toSet());
    }

    @Override
    public Set<String> getValidNotificationTokens(BaseUserEntity user) {
        if (user instanceof UserEntity) {
            return getValidUserNotificationTokens((UserEntity) user);
        } else if (user instanceof EmployeeUserEntity) {
            return getValidEmployeeNotificationTokens((EmployeeUserEntity) user);
        } else {
            throw new UnsupportedOperationException("user type not supported");
        }
    }

    @Override
    public LocalDateTime getLastLoginForUser(BaseUserEntity user) {
        Set<UserTokensEntity> tokens = user instanceof UserEntity ? userTokenRepo.getByUserEntity((UserEntity) user)
                : userTokenRepo.getByEmployeeUserEntity((EmployeeUserEntity) user);
        return tokens.stream()
                .map(UserTokensEntity::getUpdateTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public boolean currentEmployeeUserHasShopRolesOrHigher() {
        return roleService.employeeHasRoleOrHigher((EmployeeUserEntity) getCurrentUser(), Roles.STORE_EMPLOYEE);
    }

    @Override
    public boolean currentEmployeeHasOrgRolesOrHigher() {
        return roleService.employeeHasRoleOrHigher((EmployeeUserEntity) getCurrentUser(), Roles.ORGANIZATION_EMPLOYEE);
    }

    @Override
    public boolean currentEmployeeHasNasnavRoles() {
        return roleService.employeeHasRoleOrHigher((EmployeeUserEntity) getCurrentUser(), Roles.NASNAV_EMPLOYEE);
    }

    @Override
    public void setCurrentUserNotificationToken(String userToken, String notificationToken) {
        UserTokensEntity tokenEntity = getUserToken();
        tokenEntity.setNotificationToken(notificationToken);
        userTokenRepo.save(tokenEntity);
    }


    @Override
    public void setCurrentUserNotificationToken(String userToken, String notificationToken, String jwtTokens) {
        UserTokensEntity tokenEntity = getUserToken();
        String token = StringUtils.isBlankOrNull(userToken) ? jwtTokens : userToken;
        tokenEntity.setToken(token);
        tokenEntity.setNotificationToken(notificationToken);
        userTokenRepo.save(tokenEntity);
    }
    private UserTokensEntity getUserToken() {
        BaseUserEntity base = getCurrentUser();
        if (base instanceof UserEntity user) {
            return userTokenRepo.findAllByUserEntityOrderByUpdateTimeDesc(user)
                    .stream().findFirst().orElse(build(user,null));
        }else {
            EmployeeUserEntity employee = (EmployeeUserEntity) base;
            return userTokenRepo.findAllByEmployeeUserEntityOrderByUpdateTimeDesc(employee)
                    .stream().findFirst().orElse(build(null ,employee));
        }

    }

    private UserTokensEntity build(UserEntity user, EmployeeUserEntity employee) {
        UserTokensEntity userTokenEntity = new UserTokensEntity();
        userTokenEntity.setUserEntity(user);
        userTokenEntity.setEmployeeUserEntity(employee);
        return userTokenEntity;
    }

    @Override
    public boolean hasPermission(String permissionName) {
        BaseUserEntity currentUser = getCurrentUser();
        return permissionRepository.findByUserId(currentUser.getId()).stream().map(Permission::getName).anyMatch(permissionName::equals);
    }

}
@Data
@AllArgsConstructor
class UserPostLoginData {
    private BaseUserEntity userEntity;
    private String token;
}

 