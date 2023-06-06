package com.nasnav.service.yeshtery;

import com.nasnav.AppConfig;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.dto.request.ActivateOtpDto;
import com.nasnav.dto.request.user.ActivationEmailResendDTO;
import com.nasnav.enumerations.Roles;
import com.nasnav.enumerations.UserStatus;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.EntityValidationException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.persistence.yeshtery.BaseYeshteryUserEntity;
import com.nasnav.persistence.yeshtery.YeshteryUserEntity;
import com.nasnav.persistence.yeshtery.YeshteryUserOtpEntity;
import com.nasnav.persistence.yeshtery.YeshteryUserTokensEntity;
import com.nasnav.response.RecoveryUserResponse;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.*;
import com.nasnav.service.helpers.UserServicesHelper;
import com.nasnav.service.otp.OtpService;
import com.nasnav.service.otp.OtpType;
import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dao.yeshtery.CommonYeshteryUserRepository;
import com.nasnav.dao.yeshtery.YeshteryUserRepository;
import com.nasnav.dao.yeshtery.YeshteryUserTokenRepository;
import com.nasnav.dto.response.YeshteryUserApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import static com.nasnav.commons.utils.StringUtils.*;
import static com.nasnav.constatnts.EmailConstants.*;
import static com.nasnav.constatnts.EntityConstants.AUTH_TOKEN_VALIDITY;
import static com.nasnav.constatnts.EntityConstants.NASNAV_DOMAIN;
import static com.nasnav.enumerations.Roles.*;
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
    private CommonUserRepository commonNasnavUserRepo;
    @Autowired
    private UserSubscriptionRepository subsRepo;
    @Autowired
    private YeshteryUserTokenRepository userTokenRepo;
    @Autowired
    private AppConfig config;
    @Autowired
    private UserRepository nasNavUserRepository;
    @Autowired
    private EmployeeUserRepository nasNavEmployeeRepository;
    @Autowired
    private UserService nasNavUserService;
    @Autowired
    private UserTokenRepository nasnavUserTokenRepo;
    @Autowired
    private RoleService roleService;
    @Autowired
    private LoyaltyPointsService loyaltyPointsService;
    @Autowired
    private YeshteryOtpService otpService;
    @Autowired
    private OtpService nasnavOtpService;
    @Autowired
    AppConfig appConfig;
    @Autowired
    private InfluencerRepository influencerRepository;


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
    public void sendEmailRecovery(String email, Long orgId, ActivationMethod activationMethod) {
        UserEntity userEntity = getUserEntityByEmailAndOrgId(email, orgId);
        generateResetPasswordToken(userEntity);
        userEntity = nasNavUserRepository.saveAndFlush(userEntity);
        sendRecoveryMail(userEntity, activationMethod);
    }

    private UserEntity getUserEntityByEmailAndOrgId(String email, Long orgId) {
        userServicesHelper.validateEmail(email);
        userServicesHelper.validateOrgId(orgId);

        return ofNullable(nasNavUserRepository.getByEmailAndOrganizationId(email, orgId))
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, UXACTVX0001, email, orgId));
    }



    private void sendRecoveryMail(UserEntity userEntity, ActivationMethod activationMethod) {
        String userName = ofNullable(userEntity.getName()).orElse("User");
        String orgName = orgRepo.getOne(userEntity.getOrganizationId()).getName();
        try {
            // create parameter map to replace parameter by actual UserEntity data.
            Map<String, String> parametersMap = new HashMap<>();
            if (activationMethod == ActivationMethod.OTP) {
                UserOtpEntity userOtp = nasnavOtpService.createUserOtp(userEntity, OtpType.RESET_PASSWORD);
                sendUserOtp(userEntity.getOrganizationId(), userEntity.getEmail(), userOtp.getOtp());
            } else if (activationMethod == ActivationMethod.VERIFICATION_LINK) {
                parametersMap.put(USERNAME_PARAMETER, userName);
                parametersMap.put(CHANGE_PASSWORD_URL_PARAMETER,
                        appConfig.mailRecoveryUrl.concat(userEntity.getResetPasswordToken()));
                // send Recovery mail to user
                this.mailService.send(orgName, userEntity.getEmail(), CHANGE_PASSWORD_EMAIL_SUBJECT,
                        CHANGE_PASSWORD_EMAIL_TEMPLATE, parametersMap);
            }
        } catch (Exception e) {
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, GEN$0003, e.getMessage());
        }
    }

    @Override
    @Transactional
    public YeshteryUserApiResponse recoverUser(UserDTOs.PasswordResetObject resetPasswordData) {
            validateResetPasswordData(resetPasswordData);
            BaseUserEntity userEntity = getUserEntityByResetToken(resetPasswordData);
            userServicesHelper.checkResetPasswordTokenExpiry(userEntity.getResetPasswordSentAt());
            editUserSettings(userEntity, resetPasswordData);
            commonNasnavUserRepo.saveAndFlush(userEntity);

            String orgDomain = domainService.getOrganizationDomainAndSubDir(userEntity.getOrganizationId());
            String token = resetRecoveredUserTokens(userEntity);

        return new YeshteryUserApiResponse(userEntity.getId(), orgDomain, token);
    }

    private void validateResetPasswordData(UserDTOs.PasswordResetObject resetPasswordData){
        userServicesHelper.validateNewPassword(resetPasswordData.password);
        userServicesHelper.validateToken(resetPasswordData.token);
    }

    private void editUserSettings(BaseUserEntity userEntity,
                                  UserDTOs.PasswordResetObject data){
        userEntity.setResetPasswordToken(null);
        userEntity.setResetPasswordSentAt(null);
        userEntity.setEncryptedPassword(passwordEncoder.encode(data.password));
        userEntity.setUserStatus(ACTIVATED.getValue());
    }

    private BaseUserEntity getUserEntityByResetToken(UserDTOs.PasswordResetObject data){
        if(data.employee)
            return nasNavEmployeeRepository.getByResetPasswordToken(data.token)
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, U$LOG$0001));
        else
            return nasNavUserRepository.getByResetPasswordToken(data.token)
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, U$LOG$0001));
    }

    private String resetRecoveredUserTokens(BaseUserEntity user) {
        securityService.logoutAll(user);
        UserTokensEntity tokenEntity = new UserTokensEntity();
        tokenEntity.setToken(generateUUIDToken());
        tokenEntity = nasnavUserTokenRepo.save(tokenEntity);

        if(user.getClass() == UserEntity.class)
            tokenEntity.setUserEntity((UserEntity) user);
        else if (user.getClass() == EmployeeUserEntity.class)
            tokenEntity.setEmployeeUserEntity((EmployeeUserEntity) user);

        return tokenEntity.getToken();
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
    @Transactional(rollbackFor = Throwable.class)
    public YeshteryUserApiResponse registerYeshteryUserV2(Long referrer, UserDTOs.UserRegistrationObjectV2 userJson) {
        if(userJson.getActivationMethod() == null){
            userJson.setActivationMethod(ActivationMethod.VERIFICATION_LINK);
        }
            validateNewUserRegistration(userJson);

            YeshteryUserEntity user = createNewUserEntity(userJson);
            setUserAsDeactivated(user);
            generateYeshteryResetPasswordToken(user);
            user.setReferral(String.valueOf(referrer));
            userRepository.saveAndFlush(user);

            if(referrer != null) givePointsToReferrer(referrer, userJson.getOrgId());

            if (userJson.getActivationMethod() == ActivationMethod.OTP) {
                YeshteryUserOtpEntity userOtp = otpService.createUserOtp(user, OtpType.REGISTER);
                sendUserOtp(user.getOrganizationId(), user.getEmail(), userOtp.getOtp());
            } else {
                sendActivationMail(user, userJson.getRedirectUrl());
            }
            List<OrganizationEntity> yeshteryOrgs = orgService.getYeshteryOrgs();
            yeshteryOrgs.forEach(org -> createNewYeshtryUserForOrg(userJson, org, user.getId()));

            return new YeshteryUserApiResponse(user.getId(), asList(NEED_ACTIVATION, ACTIVATION_SENT));
    }

    private void givePointsToReferrer(Long referrer, Long orgId) {
        UserEntity referrerEntity = nasNavUserRepository.findById(referrer)
                .orElse(null);
        if (referrerEntity != null)
            loyaltyPointsService.givePointsToReferrer(referrerEntity, orgId);
    }

    @Override
    public int linkNonYeshteryUsersToCorrespondingYeshteryUserEntity() {
        List<YeshteryUserEntity> yeshteryUsers = userRepository.findAll();
        return doLinkNonYeshteryUsersToCorrespondingYeshteryUserEntity(yeshteryUsers);
    }

    private int doLinkNonYeshteryUsersToCorrespondingYeshteryUserEntity(List<YeshteryUserEntity> yeshteryUsers) {
        List<OrganizationEntity> orgs = orgRepo.findYeshteryOrganizations();
        int count = 0;
        if (yeshteryUsers.isEmpty())
            return count;
        for (YeshteryUserEntity yeshteryUser : yeshteryUsers) {
            for(OrganizationEntity org : orgs) {
                Optional<UserEntity> optionalUser = nasNavUserRepository.findByEmailAndOrganizationId(yeshteryUser.getEmail(), org.getId());
                UserEntity user;
                if (optionalUser.isEmpty()) {
                    user = new UserEntity();
                    user.setName(yeshteryUser.getName());
                    user.setEmail(yeshteryUser.getEmail());
                    user.setOrganizationId(org.getId());
                    user.setEncryptedPassword(yeshteryUser.getEncryptedPassword());
                    user.setPhoneNumber(yeshteryUser.getPhoneNumber());
                    user.setUserStatus(ACTIVATED.getValue());
                } else {
                    user = optionalUser.get();
                }
                if (user.getYeshteryUserId() != null) {
                    continue;
                }
                user.setYeshteryUserId(yeshteryUser.getId());
                try {
                    nasNavUserRepository.saveAndFlush(user);
                    count++;
                } catch (Exception e) {
                    logger.error("couldn't create/link user with email :"+user.getEmail()+" and org :"+user.getOrganizationId()+", error"+e.getMessage());
                }
            }
        }
        return count;
    }

    @Override
    public YeshteryUserEntity createYeshteryEntity(String name, String email, UserEntity nasnavUser, int yeshteryOrgId, Long orgId) {
        YeshteryUserEntity yeshteryUser = userRepository.getByEmailIgnoreCaseAndOrganizationId(email, orgId);
        if (yeshteryUser == null) {
            yeshteryUser = new YeshteryUserEntity();
            yeshteryUser.setName(name);
            yeshteryUser.setEmail(email);
            yeshteryUser.setOrganizationId((long) yeshteryOrgId);
            yeshteryUser.setEncryptedPassword(EntityConstants.INITIAL_PASSWORD);
            yeshteryUser.setUserStatus(ACTIVATED.getValue());
            yeshteryUser = userRepository.save(yeshteryUser);
        }
        nasnavUser.setYeshteryUserId(yeshteryUser.getId());
        nasNavUserRepository.save(nasnavUser);
        this.linkNonYeshteryUsersToCorrespondingYeshteryUserEntity(yeshteryUser);
        return yeshteryUser;
    }

    @Override
    public int linkNonYeshteryUsersToCorrespondingYeshteryUserEntity(YeshteryUserEntity yeshteryUser) {
        return doLinkNonYeshteryUsersToCorrespondingYeshteryUserEntity(asList(yeshteryUser));
    }


    private void  createNewYeshtryUserForOrg(UserDTOs.UserRegistrationObjectV2 userJson, OrganizationEntity org, Long referencedUserId) {
        Optional<UserEntity> userEntity = nasNavUserRepository.findByEmailAndOrganizationId(userJson.getEmail(), org.getId());
        UserEntity user = new UserEntity();
        if(userEntity.isPresent()){
            user = userEntity.get();
        } else {
            user.setName(userJson.getName());
            user.setEmail(userJson.getEmail());
            user.setEncryptedPassword(passwordEncoder.encode(userJson.password));
            user.setPhoneNumber(userJson.getPhoneNumber());
            user.setOrganizationId(org.getId());
            user.setUserStatus(NOT_ACTIVATED.getValue());
            String generatedToken = generatePasswordToken(user);
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
    public UserRepresentationObject getYeshteryUserData(Long userId, Boolean isEmployee) {
        BaseUserEntity currentUser = securityService.getCurrentUser();
        BaseUserEntity user;
        if (securityService.currentUserIsCustomer() || userId == null || userId.equals(currentUser.getId())) {
            return getUserRepresentationWithUserRoles(currentUser);
        } else {
            Roles userHighestRole = roleService.getEmployeeHighestRole(currentUser.getId());

            if (userHighestRole.equals(NASNAV_ADMIN)) {
                user = commonNasnavUserRepo.findById(userId, isEmployee)
                        .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, U$0001, userId));
            } else {
                Set<String> roles = Roles.getAllPrivileges().get(userHighestRole.name());
                if (!isEmployee) {
                    if (!List.of(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER).contains(userHighestRole))
                        throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$EMP$0014);
                }
                user = commonNasnavUserRepo.getByIdAndOrganizationIdAndRoles(userId, currentUser.getOrganizationId(), isEmployee, roles)
                        .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, U$0001, userId));
            }
        }
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
                generateYeshteryResetPasswordToken(user);
                userRepository.save(user);
            }

            if (accountInfo.getActivationMethod() == ActivationMethod.OTP) {
                YeshteryUserOtpEntity userOtp = otpService.createUserOtp(user, OtpType.REGISTER);
                sendUserOtp(user.getOrganizationId(), user.getEmail(), userOtp.getOtp());
            } else {
                sendActivationMail(user, accountInfo.getRedirectUrl());
            }
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

    @Override
    public AddressDTO updateUserAddress(AddressDTO addressDTO) {
        return nasNavUserService.updateUserAddress(addressDTO);
    }

    @Override
    public void removeUserAddress(Long id) {
        nasNavUserService.removeUserAddress(id);
    }

    @Override
    public UserApiResponse updateUser(UserDTOs.EmployeeUserUpdatingObject userJson) {
        return nasNavUserService.updateUser(userJson);
    }

    private void validateNewUserRegistration(UserDTOs.UserRegistrationObjectV2 userJson) {
        if (!Boolean.TRUE.equals(userJson.confirmationFlag)) {
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, U$EMP$0015, userJson.confirmationFlag);
        }
        userServicesHelper.validateBusinessRules(userJson.getName(), userJson.getEmail(), userJson.getOrgId());
        userServicesHelper.validateNewPassword(userJson.password);

        Long orgId = userJson.getOrgId();

        orgRepo.findById(orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, G$ORG$0001, orgId));

        if (userRepository.existsByEmailIgnoreCaseAndOrganizationId(userJson.email, orgId)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$LOG$0007, userJson.getEmail(), userJson.getOrgId());
        }

        if (userJson.getActivationMethod() == ActivationMethod.VERIFICATION_LINK)
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

    private void generateResetPasswordToken(UserEntity userEntity) {
        String generatedToken = generatePasswordToken(userEntity);
        userEntity.setResetPasswordToken(generatedToken);
        userEntity.setResetPasswordSentAt(now());
    }


    private void generateYeshteryResetPasswordToken(YeshteryUserEntity userEntity) {
        String generatedToken = generateUUIDToken();
        boolean existsByToken = userRepository.existsByResetPasswordToken(generatedToken);
        if (existsByToken) {
            generatedToken = reGenerateResetPasswordToken();
        }
        userEntity.setResetPasswordToken(generatedToken);
        userEntity.setResetPasswordSentAt(now());
    }

    private String generatePasswordToken(UserEntity userEntity) {
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
            throw new RuntimeBusinessException(UNAUTHORIZED, UXACTVX0006, "");

        userServicesHelper.checkResetPasswordTokenExpiry(user.getResetPasswordSentAt());

        if (!isUserDeactivated(user))
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$LOG$0008);
    }

    private void activateUserInDB(YeshteryUserEntity user) {
        user.setResetPasswordToken(null);
        user.setUserStatus(ACTIVATED.getValue());
        userRepository.save(user);
    }

    private UserRepresentationObject getUserRepresentationWithUserRoles(BaseUserEntity user) {
        UserRepresentationObject userRepObj = user.getRepresentation();
        userRepObj.setAddresses(getUserAddresses(userRepObj.getId()));
        userRepObj.setRoles(new HashSet<>(commonUserRepo.getUserRoles(user)));
        userRepObj.setIsInfluencer(influencerRepository.existsByUser_IdOrEmployeeUser_Id(user.getId(),user.getId()));
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
            String domain = domainService.getBackendUrl() + YeshteryConstants.API_PATH;
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

    @Override
    public List<UserRepresentationObject> getUserList(){
        List<YeshteryUserEntity> customers;
        if (securityService.currentUserHasRole(NASNAV_ADMIN)) {
            customers = userRepository.findAll();
        } else {
            customers = userRepository.findByOrganizationId(securityService.getCurrentUserOrganizationId());
        }
        return customers
                .stream()
                .map(YeshteryUserEntity::getRepresentation)
                .collect(toList());
    }

    private void sendUserOtp(Long orgId, String email, String otp) {
        try {
            String orgName = orgRepo.findById(orgId).orElseThrow().getName();
            Map<String, String> parametersMap = new HashMap<>();
            parametersMap.put(OTP_PARAMETER, otp);
            mailService.send(orgName, email, orgName + ACTIVATION_ACCOUNT_EMAIL_SUBJECT, OTP_TEMPLATE, parametersMap);
        } catch (Exception e) {
            logger.error(e, e);
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, GEN$0003, e.getMessage());
        }
    }

    @Override
    @Transactional
    public RecoveryUserResponse activateRecoveryOtp(ActivateOtpDto activateOtp) throws BusinessException {
        UserEntity user = nasNavUserRepository.getByEmailIgnoreCaseAndOrganizationId(activateOtp.getEmail(), activateOtp.getOrgId());
                if (user == null) throw new RuntimeBusinessException(NOT_FOUND, U$EMP$0004, activateOtp.getEmail());
        nasnavOtpService.validateOtp(activateOtp.getOtp(), user, OtpType.RESET_PASSWORD);
        generateResetPasswordToken(user);
        return new RecoveryUserResponse(user.getResetPasswordToken());
    }

    @Override
    public UserApiResponse activateUserAccount(ActivateOtpDto activateOtp) {
        YeshteryUserEntity user = userRepository.getByEmailIgnoreCaseAndOrganizationId(activateOtp.getEmail(), activateOtp.getOrgId());
                if (user == null) throw new RuntimeBusinessException(NOT_FOUND, U$EMP$0004, activateOtp.getEmail());
		otpService.validateOtp(activateOtp.getOtp(), user, OtpType.REGISTER);

        activateUserInDB(user);
        activateOrgUser(user);
        return login(user, false);
    }
}

@Data
@AllArgsConstructor
class UserPostLoginData{
    private BaseYeshteryUserEntity userEntity;
    private String token;
}
