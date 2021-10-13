package com.nasnav.yeshtery.services.impl;

import com.nasnav.AppConfig;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.*;
import com.nasnav.dto.AddressRepObj;
import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.user.ActivationEmailResendDTO;
import com.nasnav.enumerations.UserStatus;
import com.nasnav.exceptions.EntityValidationException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.service.*;
import com.nasnav.service.helpers.UserServicesHelper;
import com.nasnav.yeshtery.dao.CommonYeshteryUserRepository;
import com.nasnav.yeshtery.dao.YeshteryUserRepository;
import com.nasnav.yeshtery.dao.YeshteryUserTokenRepository;
import com.nasnav.persistence.BaseYeshteryUserEntity;
import com.nasnav.persistence.YeshteryUserEntity;
import com.nasnav.persistence.YeshteryUserTokensEntity;
import com.nasnav.yeshtery.response.YeshteryUserApiResponse;
import com.nasnav.yeshtery.services.interfaces.YeshteryUserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;

import static com.nasnav.commons.utils.StringUtils.generateUUIDToken;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.constatnts.EmailConstants.*;
import static com.nasnav.constatnts.EmailConstants.ACTIVATION_ACCOUNT_URL_PARAMETER;
import static com.nasnav.constatnts.EntityConstants.AUTH_TOKEN_VALIDITY;
import static com.nasnav.constatnts.EntityConstants.NASNAV_DOMAIN;
import static com.nasnav.enumerations.Roles.NASNAV_ADMIN;
import static com.nasnav.enumerations.UserStatus.ACTIVATED;
import static com.nasnav.enumerations.UserStatus.NOT_ACTIVATED;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.response.ResponseStatus.ACTIVATION_SENT;
import static com.nasnav.response.ResponseStatus.NEED_ACTIVATION;
import static com.nasnav.service.helpers.LoginHelper.isInvalidRedirectUrl;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.*;

@Service
public class YeshteryUserServiceImpl implements YeshteryUserService {

    private final Logger logger = LogManager.getLogger();

    @Autowired
    private SecurityService securityService;
    @Autowired
    private UserServicesHelper userServicesHelper;
    @Autowired
    private OrganizationRepository orgRepo;
    @Autowired
    private YeshteryUserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private DomainService domainService;
    @Autowired
    private OrganizationService orgService;
    @Autowired
    private MailService mailService;
    @Autowired
    private UserAddressRepository userAddressRepo;
    @Autowired
    private CommonYeshteryUserRepository commonUserRepo;
    @Autowired
    private UserSubscriptionRepository subsRepo;
    @Autowired
    private YeshteryUserTokenRepository userTokenRepo;
    @Autowired
    private AppConfig config;
    @Autowired
    private UserRepository nasNavUserRepository;


    @Autowired
    AppConfig appConfig;


    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
        nasNavUserRepository.deleteByYeshteryUserId(userId);
    }

    @Override
    public BaseYeshteryUserEntity getUserById(Long userId) {
        return commonUserRepo.findById(userId, false);
    }

    @Override
    public BaseYeshteryUserEntity update(BaseYeshteryUserEntity userEntity) {
        return commonUserRepo.saveAndFlush(userEntity);
    }

    @Override
    public void sendEmailRecovery(String email, Long orgId) {
        YeshteryUserEntity userEntity = getUserEntityByEmailAndOrgId(email, orgId);
        generateResetPasswordToken(userEntity);
        userEntity = userRepository.saveAndFlush(userEntity);
        sendRecoveryMail(userEntity);
    }

    private YeshteryUserEntity getUserEntityByEmailAndOrgId(String email, Long orgId) {
        userServicesHelper.validateEmail(email);
        userServicesHelper.validateOrgId(orgId);

        return ofNullable(userRepository.getByEmailIgnoreCaseAndOrganizationId(email, orgId))
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, UXACTVX0001, email, orgId));
    }



    private void sendRecoveryMail(YeshteryUserEntity userEntity) {
        String userName = ofNullable(userEntity.getName()).orElse("User");
        String orgName = orgRepo.getOne(userEntity.getOrganizationId()).getName();
        try {
            // create parameter map to replace parameter by actual UserEntity data.
            Map<String, String> parametersMap = new HashMap<>();
            parametersMap.put(USERNAME_PARAMETER, userName);
            parametersMap.put(CHANGE_PASSWORD_URL_PARAMETER,
                    appConfig.mailRecoveryUrl.concat(userEntity.getResetPasswordToken()));
            // send Recovery mail to user
            this.mailService.send(orgName, userEntity.getEmail(), CHANGE_PASSWORD_EMAIL_SUBJECT,
                    CHANGE_PASSWORD_EMAIL_TEMPLATE, parametersMap);
        } catch (Exception e) {
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, GEN$0003, e.getMessage());
        }
    }

    @Override
    public YeshteryUserApiResponse recoverUser(UserDTOs.PasswordResetObject body) {
        return null;
    }

    @Override
    @Transactional
    public YeshteryUserApiResponse recoverYeshteryUser(UserDTOs.PasswordResetObject data) {
        return recoverUser(data);
    }

    public Boolean isUserDeactivated(BaseYeshteryUserEntity user) {
        YeshteryUserEntity userEntity = (YeshteryUserEntity)user;
        return userEntity.getUserStatus().equals(NOT_ACTIVATED.getValue());
    }

    @Override
    public YeshteryUserApiResponse registerYeshteryUserV2(UserDTOs.UserRegistrationObjectV2 userJson) {
            validateNewUserRegistration(userJson);

            YeshteryUserEntity user = createNewUserEntity(userJson);
            setUserAsDeactivated(user);
            generateResetPasswordToken(user);
            userRepository.saveAndFlush(user);

            sendActivationMail(user, userJson.getRedirectUrl());
            List<OrganizationEntity> yeshteryOrgs = orgService.getYeshteryOrgs();
            yeshteryOrgs.forEach(org -> createNewYeshtryUserForOrg(userJson, org, user.getId()));

            return new YeshteryUserApiResponse(user.getId(), asList(NEED_ACTIVATION, ACTIVATION_SENT));
    }

    private void  createNewYeshtryUserForOrg(UserDTOs.UserRegistrationObjectV2 userJson, OrganizationEntity org, Long referencedUserId) {
        Optional<UserEntity> userEntity = nasNavUserRepository.findByEmailAndOrganizationId(userJson.getEmail(), org.getId());
        UserEntity user = new UserEntity();
        if(userEntity.isPresent()){
            user = userEntity.get();
        } else {
            user.setName(userJson.getName());
            user.setEmail(userJson.getEmail());
            user.setOrganizationId(userJson.getOrgId());
            user.setEncryptedPassword(passwordEncoder.encode(userJson.password));
            user.setPhoneNumber(userJson.getPhoneNumber());
            user.setOrganizationId(org.getId());
            user.setUserStatus(NOT_ACTIVATED.getValue());
            String generatedToken = generateResetPasswordToken();
            user.setResetPasswordToken(generatedToken);
            user.setResetPasswordSentAt(now());
            user.setAllowReward(true);
        }
        user.setYeshteryUserId(referencedUserId);
        nasNavUserRepository.saveAndFlush(user);
    }


    @Override
    public YeshteryUserApiResponse activateYeshteryUserAccount(String token)  {
            YeshteryUserEntity user = userRepository.findByResetPasswordToken(token);

            checkUserActivation(user);

            activateUserInDB(user);
            activateOrgUser(user);
            return login(user, false);
    }

    @Override
    public UserRepresentationObject getYeshteryUserData(Long id, Boolean isEmployee) {
        BaseYeshteryUserEntity currentUser = getCurrentUser();

        if(!securityService.currentUserHasRole(NASNAV_ADMIN)) {
            return getUserRepresentationWithUserRoles(currentUser);
        }

        Boolean isEmp = ofNullable(isEmployee).orElse(false);
        Long requiredUserId = ofNullable(id).orElse(currentUser.getId());

        BaseYeshteryUserEntity user =
                commonUserRepo.findById(requiredUserId, isEmp);

        return getUserRepresentationWithUserRoles(user);
    }

    @Override
    public void resendActivationYeshteryEmail(ActivationEmailResendDTO accountInfo) {
            String email = accountInfo.getEmail();
            Long orgId = accountInfo.getOrgId();
            BaseYeshteryUserEntity baseUser = commonUserRepo.getByEmailAndOrganizationId(email, orgId);
            validateActivationEmailResend(accountInfo, baseUser);

            YeshteryUserEntity user = (YeshteryUserEntity)baseUser;
            if(isUserDeactivated(user)) {
                generateResetPasswordToken(user);
                userRepository.save(user);
            }

            sendActivationMail(user, accountInfo.getRedirectUrl());
    }

    @Override
    public RedirectView activateYeshteryUserAccount(String token, String redirect) {
            YeshteryUserEntity user = userRepository.findByResetPasswordToken(token);

            checkUserActivation(user);
            validateActivationRedirectUrl(redirect, user.getOrganizationId());

            activateUserInDB(user);
            activateOrgUser(user);
        return redirectUser(login(user, false).getToken(), redirect);
    }

    private void activateOrgUser(YeshteryUserEntity yeshteryUser) {
        List<UserEntity> nasNavUsers = nasNavUserRepository.findByYeshteryUserId(yeshteryUser.getId());
        nasNavUsers.forEach(this::activateUserInDB);
    }

    private void activateUserInDB(UserEntity user) {
        user.setResetPasswordToken(null);
        user.setUserStatus(ACTIVATED.getValue());
        nasNavUserRepository.save(user);
    }

    @Override
    @Transactional
    public void subscribeYeshteryEmail(String email, Long orgId) {
        OrganizationEntity org = orgRepo.findById(orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, G$ORG$0001, orgId));

        if (subsRepo.existsByEmailAndOrganization_Id(email, orgId)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$LOG$0009);
        }
        userServicesHelper.validateEmail(email);

        UserSubscriptionEntity sub = new UserSubscriptionEntity();
        sub.setEmail(email);
        sub.setOrganization(org);
        sub.setToken(generateSubscriptionToken());
        subsRepo.save(sub);

        sendSubscriptionInvitationMail(email, sub.getToken(), orgId);

    }
    @Override
    public RedirectView activateYeshterySubscribedEmail(String token, Long orgId) {
            String url = domainService.getOrganizationDomainAndSubDir(orgId);
            if (!subsRepo.existsByToken(token)) {
                return new RedirectView(url);
            }
            UserSubscriptionEntity sub = subsRepo.findByToken(token);
            sub.setToken(null);
            subsRepo.save(sub);
            return new RedirectView(url);
    }

    private void validateNewUserRegistration(UserDTOs.UserRegistrationObjectV2 userJson) {
        if (!userJson.confirmationFlag) {
            throw new EntityValidationException("Registration not confirmed by user!", null, NOT_ACCEPTABLE);
        }

        userServicesHelper.validateBusinessRules(userJson.getName(), userJson.getEmail(), userJson.getOrgId());
        userServicesHelper.validateNewPassword(userJson.password);

        Long orgId = userJson.getOrgId();

        orgRepo.findById(orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, G$ORG$0001));

        if (userRepository.existsByEmailIgnoreCaseAndOrganizationId(userJson.email, orgId)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$LOG$0007, userJson.getEmail(), userJson.getOrgId());
        }

        validateActivationRedirectUrl(userJson.getRedirectUrl(), orgId);
    }

    private YeshteryUserEntity createNewUserEntity(UserDTOs.UserRegistrationObjectV2 userJson) {
        YeshteryUserEntity user = new YeshteryUserEntity();
        user.setName(userJson.getName());
        user.setEmail(userJson.getEmail());
        user.setOrganizationId(userJson.getOrgId());
        user.setEncryptedPassword(passwordEncoder.encode(userJson.password));
        user.setPhoneNumber(userJson.getPhoneNumber());
        return user;
    }

    private void setUserAsDeactivated(YeshteryUserEntity user) {
        user.setUserStatus(NOT_ACTIVATED.getValue());
    }

    private void generateResetPasswordToken(YeshteryUserEntity userEntity) {
        String generatedToken = generateResetPasswordToken();
        userEntity.setResetPasswordToken(generatedToken);
        userEntity.setResetPasswordSentAt(now());
    }

    private String generateResetPasswordToken() {
        String generatedToken = generateUUIDToken();
        boolean existsByToken = userRepository.existsByResetPasswordToken(generatedToken);
        if (existsByToken) {
            return reGenerateResetPasswordToken();
        }
        return generatedToken;
    }

    private String reGenerateResetPasswordToken() {
        String generatedToken = generateUUIDToken();
        boolean existsByToken = userRepository.existsByResetPasswordToken(generatedToken);
        if (existsByToken) {
            return reGenerateResetPasswordToken();
        }
        return generatedToken;
    }

    private void sendActivationMail(YeshteryUserEntity userEntity, String redirectUrl) {
        try {
            OrganizationRepresentationObject organization = orgService.getOrganizationById(userEntity.getOrganizationId(), 1);
            Map<String, String> parametersMap = createActivationEmailParameters(userEntity, redirectUrl);
            mailService.send(organization.getName(), userEntity.getEmail(), ACTIVATION_ACCOUNT_EMAIL_SUBJECT,
                    NEW_EMAIL_ACTIVATION_TEMPLATE, parametersMap);
        } catch (Exception e) {
            logger.error(e, e);
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR,  GEN$0003, e.getMessage());
        }
    }

    private Map<String, String> createActivationEmailParameters(YeshteryUserEntity userEntity, String redirectUrl) {
        String domain = domainService.getBackendUrl();
        String orgDomain = domainService.getOrganizationDomainAndSubDir(userEntity.getOrganizationId());

        String activationRedirectUrl = buildActivationRedirectUrl(userEntity, redirectUrl);
        String orgLogo = domain + "/files/"+ orgService.getOrgLogo(userEntity.getOrganizationId());
        String orgName = orgRepo.findById(userEntity.getOrganizationId()).get().getName();
        String year = LocalDateTime.now().getYear()+"";

        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(USERNAME_PARAMETER, userEntity.getName());
        parametersMap.put(ACTIVATION_ACCOUNT_URL_PARAMETER, activationRedirectUrl);
        parametersMap.put("orgDomain", orgDomain);
        parametersMap.put("orgLogo", orgLogo);
        parametersMap.put("orgName", orgName);
        parametersMap.put("year", year);
        return parametersMap;
    }

    private String buildActivationRedirectUrl(YeshteryUserEntity userEntity, String redirectUrl) {
        URIBuilder builder;
        try {
            builder = new URIBuilder(redirectUrl);
            builder.addParameter("activation_token", userEntity.getResetPasswordToken());
            return builder.build().toString();
        } catch (URISyntaxException e) {
            logger.error(e, e);
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, UXACTVX0004, redirectUrl);
        }
    }

    private void validateActivationRedirectUrl(String redirectUrl, Long orgId) {
        List<String> orgDomains = domainService.getOrganizationDomainOnly(orgId);
        if(isNull(redirectUrl) || isInvalidRedirectUrl(redirectUrl, orgDomains)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, UXACTVX0004, redirectUrl);
        }
    }

    private void checkUserActivation(YeshteryUserEntity user) {
        if (user == null)
            throw new RuntimeBusinessException(UNAUTHORIZED, UXACTVX0006);

        userServicesHelper.checkResetPasswordTokenExpiry(user.getResetPasswordSentAt());

        if (!isUserDeactivated(user))
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$LOG$0008);
    }

    private void activateUserInDB(YeshteryUserEntity user) {
        user.setResetPasswordToken(null);
        user.setUserStatus(ACTIVATED.getValue());
        userRepository.save(user);
    }

    private UserRepresentationObject getUserRepresentationWithUserRoles(BaseYeshteryUserEntity user) {
        UserRepresentationObject userRepObj = user.getRepresentation();
        userRepObj.setAddresses(getUserAddresses(userRepObj.getId()));
        userRepObj.setRoles(new HashSet<>(commonUserRepo.getUserRoles(user)));
        return userRepObj;
    }

    private List<AddressRepObj> getUserAddresses(Long userId){
        return userAddressRepo
                .findByUser_Id(userId)
                .stream()
                .filter(Objects::nonNull)
                .map(a -> (AddressRepObj) a.getRepresentation())
                .collect(toList());
    }

    private void validateActivationEmailResend(ActivationEmailResendDTO accountInfo, BaseYeshteryUserEntity user) {
        String email = accountInfo.getEmail();
        Long orgId = accountInfo.getOrgId();
        if(!(user instanceof YeshteryUserEntity)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, UXACTVX0001, email, orgId);
        }else if(!isUserDeactivated(user)){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, UXACTVX0002, email);
        }else if(resendRequestedTooSoon(accountInfo)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, UXACTVX0003, email);
        }
    }

    private boolean resendRequestedTooSoon(ActivationEmailResendDTO accountInfo) {
        // TODO Auto-generated method stub
        return false;
    }

    private RedirectView redirectUser(String authToken, String loginUrl) {
        RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();
        attributes.addAttribute("auth_token", authToken);

        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(loginUrl);
        redirectView.setAttributesMap(attributes);

        return redirectView;
    }

    private String generateSubscriptionToken() {
        String generatedToken = generateUUIDToken();
        boolean existsByToken = subsRepo.existsByToken(generatedToken);
        if (existsByToken) {
            return regenerateSubscriptionToken();
        }
        return generatedToken;
    }

    private String regenerateSubscriptionToken() {
        String generatedToken = generateUUIDToken();
        boolean existsByToken = subsRepo.existsByToken(generatedToken);
        if (existsByToken) {
            return regenerateSubscriptionToken();
        }
        return generatedToken;
    }

    private void sendSubscriptionInvitationMail(String email, String activationToken, Long orgId) {
        try {
            String domain = domainService.getBackendUrl();
            String orgDomain = domainService.getOrganizationDomainAndSubDir(orgId);
            String orgLogo = domain + "/files/"+ orgService.getOrgLogo(orgId);
            String orgName = orgRepo.findById(orgId).get().getName();
            String subscriptionUrl =  domain + "/user/subscribe/activate?org_id="+orgId+"&token=" + activationToken;
            String year = LocalDateTime.now().getYear()+"";

            Map<String, String> parametersMap = new HashMap<>();
            parametersMap.put("subscriptionUrl", subscriptionUrl);
            parametersMap.put("orgDomain", orgDomain);
            parametersMap.put("orgLogo", orgLogo);
            parametersMap.put("orgName", orgName);
            parametersMap.put("year", year);
            mailService.send(orgName, email, "Subscribe to newsletter",
                    USER_SUBSCRIPTION_TEMPLATE, parametersMap);
        } catch (Exception e) {
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, GEN$0003, e.getMessage());
        }
    }

    public BaseYeshteryUserEntity getCurrentUser() {
        return getCurrentUserOptional()
                .orElseThrow(()-> new IllegalStateException("Could not retrieve current user!"));
    }

    public Optional<YeshteryUserEntity> getCurrentUserOptional() {
        // TODO should fetch yeshtery entity directly from YeshteryUserRepositoy
        // related to CommonUserRepositoryImpl todo note
        Long id =  ofNullable( SecurityContextHolder.getContext() )
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getDetails)
                .map(UserEntity.class::cast)
                .map(UserEntity::getYeshteryUserId)
                .orElse(-1L);
        return userRepository.findById(id);
    }

    public YeshteryUserApiResponse login(UserDTOs.UserLoginObject loginData) {

        if(invalidLoginData(loginData)) {
            throwInvalidCredentialsException();
        }

        BaseYeshteryUserEntity userEntity = commonUserRepo.getByEmailIgnoreCaseAndOrganizationId(loginData.getEmail(), loginData.getOrgId(), loginData.isEmployee());

        validateLoginUser(userEntity);
        validateUserPassword(loginData, userEntity);

        return login(userEntity, loginData.rememberMe);
    }

    public YeshteryUserApiResponse login(BaseYeshteryUserEntity userEntity, boolean rememberMe) {
        UserPostLoginData userData = updatePostLogin(userEntity);

        Cookie cookie = createCookie(userData.getToken(), rememberMe);

        return createSuccessLoginResponse(userData, cookie);
    }

    public UserPostLoginData updatePostLogin(BaseYeshteryUserEntity userEntity) {
        LocalDateTime currentSignInDate = userEntity.getCurrentSignInDate();

        String authToken = generateUserToken(userEntity);

        userEntity.setLastSignInDate(currentSignInDate);
        userEntity.setCurrentSignInDate(LocalDateTime.now());
        userEntity.setAuthenticationToken(authToken);
        BaseYeshteryUserEntity savedUserData = commonUserRepo.saveAndFlush(userEntity);

        return new UserPostLoginData(savedUserData, authToken);
    }

    private void validateLoginUser(BaseYeshteryUserEntity userEntity) {
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

    private boolean isAccountLocked(BaseYeshteryUserEntity userEntity) {
        return userEntity.getUserStatus().equals(UserStatus.ACCOUNT_SUSPENDED.getValue());
    }

    private void throwInvalidCredentialsException() {
        throw new RuntimeBusinessException(UNAUTHORIZED, U$LOG$0002);
    }

    private String generateUserToken(BaseYeshteryUserEntity user) {
        YeshteryUserTokensEntity token = new YeshteryUserTokensEntity();
        token.setToken(StringUtils.generateUUIDToken());
        token.setYeshteryUserEntity((YeshteryUserEntity) user);

        userTokenRepo.save(token);

        return token.getToken();
    }

    private boolean invalidLoginData(UserDTOs.UserLoginObject loginData) {
        return loginData == null || isBlankOrNull(loginData.email) ;
    }

    private void validateUserPassword(UserDTOs.UserLoginObject loginData, BaseYeshteryUserEntity userEntity) {
        boolean passwordMatched = passwordEncoder.matches(loginData.password, userEntity.getEncryptedPassword());
        if(!passwordMatched) {
            throwInvalidCredentialsException();
        }
    }

    private Cookie createCookie(String token, boolean rememberMe) {
        Cookie cookie = new Cookie("User-Token", token);

        cookie.setHttpOnly(true);
        cookie.setDomain(NASNAV_DOMAIN);
        cookie.setPath("/");
        if (rememberMe) {
            cookie.setMaxAge(AUTH_TOKEN_VALIDITY);
        }
        if (config.secureTokens)
            cookie.setSecure(true);

        return cookie;
    }

    public YeshteryUserApiResponse createSuccessLoginResponse(UserPostLoginData userData, Cookie cookie) {
        Long shopId = 0L;
        BaseYeshteryUserEntity userEntity = userData.getUserEntity();

        Long orgId = ofNullable(userEntity.getOrganizationId()).orElse(0L);
        List<String> userRoles = commonUserRepo.getUserRoles(userEntity);

        return new YeshteryUserApiResponse(userEntity.getId(), cookie.getValue(), userRoles, orgId, shopId,
                userEntity.getName(), userEntity.getEmail(), cookie);
    }
}

@Data
@AllArgsConstructor
class UserPostLoginData{
    private BaseYeshteryUserEntity userEntity;
    private String token;
}
