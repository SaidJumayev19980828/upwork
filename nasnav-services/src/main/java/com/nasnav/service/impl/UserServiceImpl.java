package com.nasnav.service.impl;

import com.google.common.collect.ObjectArrays;
import com.nasnav.AppConfig;
import com.nasnav.commons.utils.CustomPaginationPageRequest;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.dto.request.ActivateOtpDto;
import com.nasnav.dto.request.user.ActivationEmailResendDTO;
import com.nasnav.enumerations.Roles;
import com.nasnav.enumerations.UserStatus;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.request.ImageBase64;
import com.nasnav.response.RecoveryUserResponse;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.*;
import com.nasnav.service.helpers.UserServicesHelper;
import com.nasnav.service.otp.OtpService;
import com.nasnav.service.otp.OtpType;
import com.nasnav.util.MultipartFileUtils;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;

import static com.nasnav.commons.utils.StringUtils.generateUUIDToken;
import static com.nasnav.commons.utils.StringUtils.isNotBlankOrNull;
import static com.nasnav.constatnts.EmailConstants.*;
import static com.nasnav.enumerations.Roles.*;
import static com.nasnav.enumerations.UserStatus.*;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.response.ResponseStatus.ACTIVATION_SENT;
import static com.nasnav.response.ResponseStatus.NEED_ACTIVATION;
import static com.nasnav.service.helpers.LoginHelper.isInvalidRedirectUrl;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private Logger logger = LogManager.getLogger();

	private final PasswordEncoder passwordEncoder;
	private final UserServicesHelper userServicesHelper;
	private final SecurityService securityService;
	private final DomainService domainService;
	private final OrganizationService orgService;
	private final MailService mailService;
	private final RoleService roleService;

	private final UserTokenRepository userTokenRepo;
	private final AddressRepository addressRepo;
	private final UserAddressRepository userAddressRepo;
	private final CommonUserRepository commonUserRepo;
	private final OrganizationRepository orgRepo;
	private final AreaRepository areaRepo;
	private final UserRepository userRepository;
	private final UserSubscriptionRepository subsRepo;
	private final OAuth2ProviderRepository providerRepository;

	private final OAuth2UserRepository oAuthUserRepo;

	private final AppConfig appConfig;

	private final SubAreaRepository subAreaRepo;

	private final MetaOrderRepository metaOrderRepository;

	private final LoyaltyTierService loyaltyTierService;

	private final OtpService otpService;

	private final FileService fileService;

	private final PackageService packageService;

	private final LoyaltyPointsService loyaltyPointsService;

	private UserApiResponse registerUserV2(UserDTOs.UserRegistrationObjectV2 userJson) {
		if(userJson.getActivationMethod() == null){
			userJson.setActivationMethod(ActivationMethod.VERIFICATION_LINK);
		}
		validateNewUserRegistration(userJson);

		UserEntity user = createNewUserEntity(userJson);
		setUserAsDeactivated(user);
		generateResetPasswordToken(user);

		LoyaltyTierEntity loyaltyTier = loyaltyTierService.getActiveDefaultTier(userJson.getOrgId());

		if(loyaltyTier != null) {
			user.setTier(loyaltyTier);
		}

		user = userRepository.saveAndFlush(user);

		if (userJson.getActivationMethod() == ActivationMethod.OTP) {
			UserOtpEntity userOtp = otpService.createUserOtp(user, OtpType.REGISTER);
			sendUserOtp(user, userOtp.getOtp());
		} else {
			sendActivationMail(user, userJson.getRedirectUrl());
		}
		//Get Package Registered In Organization
		Long packageId = packageService.getPackageIdRegisteredInOrg(user);
		return new UserApiResponse(user.getId(),packageId, asList(NEED_ACTIVATION, ACTIVATION_SENT));
	}
	@Override
	public UserApiResponse registerUserReferral(UserDTOs.UserRegistrationObjectV2 userJson, Long referrer) {
		if(referrer != null) givePointsToReferrer(referrer, userJson.getOrgId());
		return  registerUserV2(userJson);
	}
	@Override
	public UserApiResponse googleRegisterUser(UserDTOs.GoogleUserRegistrationObject json) {
		UserDTOs.UserRegistrationObjectV2 userJson = json.getUser();
		validateNewUserRegistration(userJson);
		UserEntity user = createNewUserEntity(userJson);
		user.setUserStatus(ACTIVATED.getValue());
		LoyaltyTierEntity loyaltyTier = loyaltyTierService.getActiveDefaultTier(userJson.getOrgId());
		if(loyaltyTier != null) {
			user.setTier(loyaltyTier);
		}
		user = userRepository.save(user);
		String loginToken = generateUserToken(user);
		saveNewOAuthUserToDB(json.getIdToken(),json.getServerAuthCode(),user);
		return new UserApiResponse(user.getId(),loginToken);
	}
	private void saveNewOAuthUserToDB(String token, String serverCode,UserEntity nasnavUser) {
		OAuth2UserEntity oAuthUser = new OAuth2UserEntity();
		oAuthUser.setUser(nasnavUser);
		oAuthUser.setEmail(nasnavUser.getEmail());
		oAuthUser.setLoginToken(token);
		oAuthUser.setOAuth2Id(serverCode);
		oAuthUser.setOrganizationId(nasnavUser.getOrganizationId());
		OAuth2ProviderEntity provider = providerRepository.findByProviderNameIgnoreCase("google").get();
		if (provider == null) {
			provider = new OAuth2ProviderEntity();
			provider.setProviderName("google");
			providerRepository.save(provider);
		}
		oAuthUser.setProvider(provider);
		oAuthUserRepo.save(oAuthUser);
	}

	private String generateUserToken(BaseUserEntity user) {
		UserTokensEntity token = new UserTokensEntity();
		token.setToken(StringUtils.generateUUIDToken());
		if (user instanceof EmployeeUserEntity) {
			token.setEmployeeUserEntity((EmployeeUserEntity) user);
		} else {
			token.setUserEntity((UserEntity) user);
		}
		userTokenRepo.save(token);
		UserTokensEntity userTokensEntity = userTokenRepo.getUserEntityByToken(token.getToken());
		userTokenRepo.save(userTokensEntity);
		return token.getToken();
	}

	private void givePointsToReferrer(Long referrer, Long orgId) {
		UserEntity referrerEntity = userRepository.findById(referrer)
				.orElse(null);
		if (referrerEntity != null)
			loyaltyPointsService.givePointsToReferrer(referrerEntity, orgId);
	}

	private void validateNewUserRegistration(UserDTOs.UserRegistrationObjectV2 userJson) {
		if (!Boolean.TRUE.equals(userJson.confirmationFlag)) {
			throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, U$EMP$0015, userJson.confirmationFlag);
		}

		userServicesHelper.validateBusinessRules(userJson.getName(), userJson.getEmail(), userJson.getOrgId());
		userServicesHelper.validateNewPassword(userJson.password);

		Long orgId = userJson.getOrgId();

		OrganizationEntity org = orgRepo.findById(orgId)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, G$ORG$0001));

		if (userRepository.existsByEmailIgnoreCaseAndOrganizationId(userJson.email, orgId)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$LOG$0007, userJson.getEmail(), userJson.getOrgId());
		}

		if (userJson.getActivationMethod() == ActivationMethod.VERIFICATION_LINK)
			validateActivationRedirectUrl(userJson.getRedirectUrl(), orgId);
	}




	private void validateActivationRedirectUrl(String redirectUrl, Long orgId) {
		List<String> orgDomains = domainService.getOrganizationDomainOnly(orgId);
		if(isNull(redirectUrl) || isInvalidRedirectUrl(redirectUrl, orgDomains)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, UXACTVX0004, redirectUrl);
		}
	}




	private void sendActivationMail(UserEntity userEntity, String redirectUrl) {
		try {
			String orgName = orgRepo.findById(userEntity.getOrganizationId()).get().getName();
			Map<String, String> parametersMap = createActivationEmailParameters(userEntity, redirectUrl, orgName);
			mailService.send(orgName, userEntity.getEmail(), orgName + ACTIVATION_ACCOUNT_EMAIL_SUBJECT,
					NEW_EMAIL_ACTIVATION_TEMPLATE, parametersMap);
		} catch (Exception e) {
			logger.error(e, e);
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR,  GEN$0003, e.getMessage());
		}
	}




	private Map<String, String> createActivationEmailParameters(UserEntity userEntity, String redirectUrl, String orgName) {
		String domain = domainService.getBackendUrl();
		String orgDomain = domainService.getOrganizationDomainAndSubDir(userEntity.getOrganizationId());

		String activationRedirectUrl = buildActivationRedirectUrl(userEntity, redirectUrl);
		String orgLogo = domain + "/files/"+ orgService.getOrgLogo(userEntity.getOrganizationId());
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



	private String buildActivationRedirectUrl(UserEntity userEntity, String redirectUrl) {
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




	private UserEntity createNewUserEntity(UserDTOs.UserRegistrationObjectV2 userJson) {
		UserEntity user = new UserEntity();
		user.setName(userJson.getName());
		user.setEmail(userJson.getEmail());
		user.setOrganizationId(userJson.getOrgId());
		user.setEncryptedPassword(passwordEncoder.encode(userJson.password));
		user.setPhoneNumber(userJson.getPhoneNumber());
		user.setImage(userJson.getAvatar());
		return user;
	}



	private void setUserAsDeactivated(UserEntity user) {
		user.setUserStatus(NOT_ACTIVATED.getValue());
	}



	@Override
	public Boolean isUserDeactivated(BaseUserEntity user) {
		UserEntity userEntity = (UserEntity)user;
		return userEntity.getUserStatus().equals(NOT_ACTIVATED.getValue());
	}


	@Transactional
	@Override
	public UserApiResponse updateUser(UserDTOs.EmployeeUserUpdatingObject userJson) {
		UserEntity userEntity = (UserEntity) securityService.getCurrentUser();
		List<ResponseStatus> successResponseStatusList = new ArrayList<>();
		if (isNotBlankOrNull(userJson.getName())) {
			userServicesHelper.validateName(userJson.getName());
			userEntity.setName(userJson.getName());
		}
		if (isNotBlankOrNull(userJson.email)){
			userServicesHelper.validateEmail(userJson.getEmail());
			if(!userJson.getEmail().equals(userEntity.getEmail())) {
				userEntity.setEmail(userJson.email);
				generateResetPasswordToken(userEntity);
				userEntity = userRepository.saveAndFlush(userEntity);
				sendRecoveryMail(userEntity);
				successResponseStatusList.addAll(asList(NEED_ACTIVATION, ACTIVATION_SENT));
			}
		}
		String [] defaultIgnoredProperties = new String[]{"name", "email", "org_id", "shop_id", "role"};
		String [] allIgnoredProperties = new HashSet<String>(
				asList(ObjectArrays.concat(getNullProperties(userJson), defaultIgnoredProperties, String.class))).toArray(new String[0]);

		BeanUtils.copyProperties(userJson, userEntity, allIgnoredProperties);

		if(!StringUtils.validDateTime(userJson.getDateOfBirth()))
			userEntity.setDateOfBirth(LocalDateTime.parse(userJson.getDateOfBirth()));

		Long userId = userRepository.saveAndFlush(userEntity).getId();
		if (successResponseStatusList.isEmpty()) {
			successResponseStatusList.add(ResponseStatus.ACTIVATED);
		}
		return new UserApiResponse(userId, successResponseStatusList);
	}


	@Override
	@Transactional
	public AddressDTO updateUserAddress(AddressDTO addressDTO) {
		validateAddressDTO(addressDTO);
		AddressDTO newAddressEntity = doUpdateUserAddressesImmutably(addressDTO);
		setAsPrincipleAddressIfNeeded(addressDTO, newAddressEntity);
		return newAddressEntity;
	}

	private void validateAddressDTO(AddressDTO addressDTO) {
		if (addressDTO.getAddressLine1() == null || addressDTO.getAddressLine1().isEmpty()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$PRAM$0001, "address_line_1");
		}
		if (addressDTO.getAreaId() == null || addressDTO.getAreaId() < 0) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$PRAM$0001, "area_id");
		}
		if (addressDTO.getPhoneNumber() == null || addressDTO.getPhoneNumber().isEmpty()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$PRAM$0001, "phone_number");
		}
	}

	private void setAsPrincipleAddressIfNeeded(AddressDTO addressDTO, AddressDTO newAddress) {
		UserEntity user = (UserEntity) securityService.getCurrentUser();
		if (addressDTO.getPrincipal() != null) {
			if (addressDTO.getPrincipal()) {
				addressRepo.makeAddressNotPrincipal(user.getId());
				addressRepo.makeAddressPrincipal(user.getId(), newAddress.getId());
				newAddress.setPrincipal(true);
			}
		}
	}


	@Override
	public void removeUserAddress(Long id) {
		UserEntity user = (UserEntity) securityService.getCurrentUser();
		if (addressRepo.countByUserIdAndAddressId(id, user.getId()) == 0){
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0002, id);
		}
		addressRepo.unlinkAddressFromUser(id, user.getId());
	}



	/**
	 * we use a immutable approach for addresses, as they are referenced by orders.
	 * and orders will need the address original data when the order was made.
	 * so, updating an existing address actually creates a totally new address entity and
	 * the existing entity is just being unlinked from the user instead of modifying it.
	 * */
	private AddressDTO doUpdateUserAddressesImmutably(AddressDTO addressDTO) {
		unlinkExistingAddressEntityFromUser(addressDTO);
		persistNewUserAddressEntity(addressDTO);
		linkNewAddressEntityToUser(addressDTO);
		return addressDTO;
	}



	private void linkNewAddressEntityToUser(AddressDTO addressDTO) {
		UserEntity userEntity = (UserEntity) securityService.getCurrentUser();
		addressRepo.linkAddressToUser(userEntity.getId(), addressDTO.getId());
	}



	private void unlinkExistingAddressEntityFromUser(AddressDTO addressDTO) {
		UserEntity userEntity = (UserEntity) securityService.getCurrentUser();
		if (addressDTO.getId() != null) {
			if (addressRepo.countByUserIdAndAddressId(addressDTO.getId(), userEntity.getId()) == 0){
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0002, addressDTO.getId());
			}
			addressRepo.unlinkAddressFromUser(addressDTO.getId(), userEntity.getId());
		}
	}



	private void persistNewUserAddressEntity(AddressDTO addressDTO) {
		AddressesEntity address = new AddressesEntity();
		BeanUtils.copyProperties(addressDTO, address, new String[] {"id"});

		setArea(addressDTO, address);
		setSubArea(addressDTO, address);

		addressRepo.save(address);

		BeanUtils.copyProperties(address, addressDTO);
	}



	private void setSubArea(AddressDTO addressDTO, AddressesEntity address) {
		Long subAreaId = addressDTO.getSubAreaId();
		if(isNull(subAreaId)){
			return;
		}
		Long orgId = securityService.getCurrentUserOrganizationId();
		SubAreasEntity subArea =
				subAreaRepo
						.findByIdAndOrganization_Id(subAreaId, orgId)
						.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, SUBAREA$001, subAreaId, orgId));
		address.setSubAreasEntity(subArea);
		if(isNull(addressDTO.getAreaId())){
			address.setAreasEntity(subArea.getArea());
		}else{
			validateSubAreaMatchesGivenArea(addressDTO, address, subAreaId, subArea);
		}
	}




	private void validateSubAreaMatchesGivenArea(AddressDTO addressDTO, AddressesEntity address, Long subAreaId, SubAreasEntity subArea) {
		Long givenAreaId =
				ofNullable(address.getAreasEntity())
						.map(AreasEntity::getId)
						.orElse(addressDTO.getAreaId());
		Long subAreaParentId =
				ofNullable(subArea.getArea())
						.map(AreasEntity::getId)
						.orElse(null);
		if(!Objects.equals(givenAreaId, subAreaParentId)){
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, SUBAREA$002, subAreaId, givenAreaId);
		}
	}



	private void setArea(AddressDTO addressDTO, AddressesEntity address) {
		if (nonNull(addressDTO.getAreaId())) {
			if (areaRepo.existsById(addressDTO.getAreaId())) {
				address.setAreasEntity(areaRepo.findById(addressDTO.getAreaId()).get());
			}
		}
	}

	@Override
	public void deleteUser(Long userId) {
		userRepository.deleteById(userId);
	}


	@Override
	public UserEntity getUserById(Long userId) {
		return userRepository.findById(userId).orElse(null);
	}

	@Override
	public BaseUserEntity update(BaseUserEntity userEntity) {
		return userRepository.saveAndFlush((UserEntity) userEntity);
	}

	public void sendEmailRecovery(String email, Long orgId) {
		UserEntity userEntity = getUserEntityByEmailAndOrgId(email, orgId);
		generateResetPasswordToken(userEntity);
		userEntity = userRepository.saveAndFlush(userEntity);
		sendRecoveryMail(userEntity);
	}


	private UserEntity getUserEntityByEmailAndOrgId(String email, Long orgId) {
		userServicesHelper.validateEmail(email);
		userServicesHelper.validateOrgId(orgId);

		return ofNullable(userRepository.getByEmailIgnoreCaseAndOrganizationId(email, orgId))
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, UXACTVX0001, email, orgId));
	}





	private void generateResetPasswordToken(UserEntity userEntity) {
		String generatedToken = generateResetPasswordToken();
		userEntity.setResetPasswordToken(generatedToken);
		userEntity.setResetPasswordSentAt(now());
	}




	private void sendRecoveryMail(UserEntity userEntity) {
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

	@Override
	@Transactional
	public UserApiResponse recoverUser(UserDTOs.PasswordResetObject data) {
		userServicesHelper.validateNewPassword(data.password);
		userServicesHelper.validateToken(data.token);
		UserEntity userEntity = userRepository.getByResetPasswordToken(data.token)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, U$LOG$0001));;

		userServicesHelper.checkResetPasswordTokenExpiry(userEntity.getResetPasswordSentAt());
		userEntity.setResetPasswordToken(null);
		userEntity.setResetPasswordSentAt(null);
		userEntity.setEncryptedPassword(passwordEncoder.encode(data.password));
		userEntity = userRepository.saveAndFlush(userEntity);

		String orgDomain = domainService.getOrganizationDomainAndSubDir(userEntity.getOrganizationId());
		String token = resetRecoveredUserTokens(userEntity);

		return new UserApiResponse(userEntity.getId(), orgDomain, token);
	}


	private String resetRecoveredUserTokens(UserEntity user) {
		securityService.logoutAll(user);
		UserTokensEntity tokenEntity = new UserTokensEntity();
		tokenEntity.setUserEntity(user);
		tokenEntity.setToken(generateUUIDToken());
		return userTokenRepo.save(tokenEntity).getToken();
	}

	/* get user own info or other user info
	for customer, get his own info only
	for nasnav admin, get requested user info regardless of its roles or organization
	for employees, only organization admins and managers can get employee or customer info withing the same organization
	 */
	@Override
	public UserRepresentationObject getUserData(Long userId, Boolean isEmployee) {
		BaseUserEntity currentUser = securityService.getCurrentUser();
		BaseUserEntity user;

		if (Boolean.TRUE.equals(securityService.currentUserIsCustomer()) || userId == null) {
			return getUserRepresentationWithUserRoles(currentUser);
		}
		Roles userHighestRole = roleService.getEmployeeHighestRole(currentUser.getId());
		if (userHighestRole.equals(NASNAV_ADMIN)) {
			user = commonUserRepo.findById(userId, isEmployee)
					.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, U$0001, userId));
		} else {
			if (isEmployee != null && !isEmployee && !List.of(ORGANIZATION_ADMIN, ORGANIZATION_MANAGER).contains(userHighestRole))
					throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$EMP$0014);
			user = commonUserRepo.findByIdAndOrganizationId(userId, currentUser.getOrganizationId(), isEmployee)
					.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, U$0001, userId));
		}

		return getUserRepresentationWithUserRoles(user);
	}



	@Override
	public RedirectView activateUserAccount(String token, String redirect)  {
		UserEntity user = userRepository.findByResetPasswordToken(token);

		checkUserActivation(user);
		validateActivationRedirectUrl(redirect, user.getOrganizationId());

		activateUserInDB(user);
		// using securityService.getCurrentUserOrganizationId() causes the api to fail because no current user exists
		return redirectUser(securityService.login(user, false).getToken(), redirect);
	}




	private void activateUserInDB(UserEntity user) {
		user.setResetPasswordToken(null);
		user.setUserStatus(ACTIVATED.getValue());
		userRepository.save(user);
	}



	private RedirectView redirectUser(String authToken, String loginUrl) {
		RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();
		attributes.addAttribute("auth_token", authToken);

		RedirectView redirectView = new RedirectView();
		redirectView.setUrl(loginUrl);
		redirectView.setAttributesMap(attributes);

		return redirectView;
	}





	private void checkUserActivation(UserEntity user) {
		if (user == null)
			throw new RuntimeBusinessException(UNAUTHORIZED, UXACTVX0006, "");

		userServicesHelper.checkResetPasswordTokenExpiry(user.getResetPasswordSentAt());

		if (!isUserDeactivated(user))
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$LOG$0008);
	}




	private UserRepresentationObject getUserRepresentationWithUserRoles(BaseUserEntity user) {
		UserRepresentationObject userRepObj = user.getRepresentation();
		userRepObj.setAddresses(getUserAddresses(userRepObj.getId()));
		userRepObj.setRoles(new HashSet<>(roleService.getUserRoles(user)));
		userRepObj.setLastLogin(securityService.getLastLoginForUser(user));
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



	private String[] getNullProperties(UserDTOs.EmployeeUserUpdatingObject userJson) {
		final BeanWrapper src = new BeanWrapperImpl(userJson);
		List<String> nullProperties = new ArrayList<>();
		java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
		for(java.beans.PropertyDescriptor pd : pds) {
			Object srcValue = src.getPropertyValue(pd.getName());
			if (srcValue == null) nullProperties.add(pd.getName());
		}
		String[] result = new String[nullProperties.size()];
		return nullProperties.toArray(result);
	}




	@Override
	public void resendActivationEmail(ActivationEmailResendDTO accountInfo) {
		String email = accountInfo.getEmail();
		Long orgId = accountInfo.getOrgId();
		BaseUserEntity baseUser = commonUserRepo.getByEmailAndOrganizationId(email, orgId);
		validateActivationEmailResend(accountInfo, baseUser);

		UserEntity user = (UserEntity)baseUser;
		if(isUserDeactivated(user)) {
			generateResetPasswordToken(user);
			userRepository.save(user);
		}
		if (accountInfo.getActivationMethod() == ActivationMethod.OTP) {
			UserOtpEntity userOtp = otpService.createUserOtp(user, OtpType.REGISTER);
			sendUserOtp(user, userOtp.getOtp());
		} else {
			sendActivationMail(user, accountInfo.getRedirectUrl());
		}
	}


	private void validateActivationEmailResend(ActivationEmailResendDTO accountInfo, BaseUserEntity user) {
		String email = accountInfo.getEmail();
		Long orgId = accountInfo.getOrgId();
		if(user == null) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, UXACTVX0001, email, orgId);
		}else if (! (user instanceof UserEntity)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, E$USR$0001);
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




	@Override
	public UserApiResponse activateUserAccount(String token) {
		UserEntity user = userRepository.findByResetPasswordToken(token);

		checkUserActivation(user);

		activateUserInDB(user);
		return securityService.login(user, false);
	}


	@Override
	public void suspendUserAccount(Long id, Boolean suspend) {
		UserEntity user = getUserEntityById(id);
		UserStatus status = userServicesHelper.checkUserStatusForSuspension(user);
		if (suspend) {
			if (status.equals(ACCOUNT_SUSPENDED)) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$STATUS$0001);
			}
			user.setUserStatus(ACCOUNT_SUSPENDED.getValue());
			userTokenRepo.deleteByUserEntity(user);
		} else {
			user.setUserStatus(ACTIVATED.getValue());
		}
		userRepository.save(user);
	}

	private UserEntity getUserEntityById(Long id) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		return userRepository.findByIdAndOrganizationId(id, orgId)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, U$0001, id));
	}


	@Override
	@Transactional
	public void subscribeEmail(String email, Long orgId) {
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

	public RedirectView activateSubscribedEmail(String token, Long orgId) {
		String url = domainService.getOrganizationDomainAndSubDir(orgId);
		if (!subsRepo.existsByToken(token)) {
			return new RedirectView(url);
		}
		UserSubscriptionEntity sub = subsRepo.findByToken(token);
		sub.setToken(null);
		subsRepo.save(sub);
		return new RedirectView(url);
	}

	public List<UserRepresentationObject> getUserList(){
		List<UserEntity> customers;
		if (Boolean.TRUE.equals(securityService.currentUserHasRole(NASNAV_ADMIN))) {
			customers = userRepository.findAll();
		} else {
			customers = userRepository.findByOrganizationId(securityService.getCurrentUserOrganizationId());
		}
		return customers
				.stream()
				.map(UserEntity::getRepresentation)
				.collect(toList());
	}

	public PaginatedResponse<UserRepresentationObject> getUserListByStatusPaging(Integer start, Integer count, Integer userStatus) {
		Page<UserEntity> customersPage ;
		Pageable pageable = Objects.nonNull(start) && Objects.nonNull(count)?
				new CustomPaginationPageRequest(start, count)
				:Pageable.unpaged();

		if (Boolean.TRUE.equals(securityService.currentUserHasRole(NASNAV_ADMIN))) {
			if(userStatus!=null)
				customersPage = userRepository.findAllUsersByUserStatus(userStatus, pageable);
			else
				customersPage = userRepository.findAll(pageable.first());
		} else {
			Long orgID = securityService.getCurrentUserOrganizationId();
			if(userStatus!=null)
				customersPage = userRepository.findByOrganizationIdAndUserStatus(orgID, userStatus, pageable);
			else
				customersPage = userRepository.findByOrganizationId(orgID, pageable);
		}

		return PaginatedResponse.<UserRepresentationObject>builder()
				.content(customersPage.getContent().stream()
						.map(UserEntity::getRepresentation)
						.toList())
				.totalPages(customersPage.getTotalPages())
				.totalRecords(customersPage.getTotalElements())
				.build();
	}

	@Override
	public List<UserEntity> getYeshteryUsersByAllowReward(Boolean allowReward) {
		return userRepository.findByYeshteryUserIdNotNullAndAllowReward(allowReward);
	}

	@Override
	public void updateUserByTierIdAndOrgId(Long userId, Long orgId) {
		Long tierId = getTierIdByUserOrders(orgId, userId);
		if(tierId != null && tierId > 0) {
			userRepository.updateUserTier(tierId, userId);
		}
	}

	public Long getTierIdByUserOrders(Long orgId, Long userId) {
		if (orgId < 0) {
			orgId = securityService.getCurrentUserOrganizationId();
		}
		Integer orderCount = metaOrderRepository.countByUser_IdAndOrganization_IdAAndFinalizeStatus(userId, orgId);
		return ofNullable(loyaltyTierService.getTierByAmountAndOrganizationId(orderCount, orgId))
				.map(LoyaltyTierEntity::getId)
				.orElse(-1L);
	}

	private void sendUserOtp(UserEntity userEntity, String otp) {
		try {
			String orgName = orgRepo.findById(userEntity.getOrganizationId()).orElseThrow().getName();
			Map<String, String> parametersMap = new HashMap<>();
			parametersMap.put(OTP_PARAMETER, otp);
			mailService.send(orgName, userEntity.getEmail(), orgName + ACTIVATION_ACCOUNT_EMAIL_SUBJECT, OTP_TEMPLATE, parametersMap);
		} catch (Exception e) {
			logger.error(e, e);
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, GEN$0003, e.getMessage());
		}
	}

	public void sendEmailRecovery(String email, Long orgId, ActivationMethod activationMethod) {
		UserEntity userEntity = getUserEntityByEmailAndOrgId(email, orgId);
		generateResetPasswordToken(userEntity);
		userEntity = userRepository.saveAndFlush(userEntity);
		sendRecoveryMail(userEntity, activationMethod);
	}

	@Override
	@Transactional
	public RecoveryUserResponse activateRecoveryOtp(ActivateOtpDto activateOtp) throws BusinessException {
		UserEntity user = userRepository.findByEmailAndOrganizationId(activateOtp.getEmail(), activateOtp.getOrgId())
				.orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, U$EMP$0004, activateOtp.getEmail()));
		otpService.validateOtp(activateOtp.getOtp(), user, OtpType.RESET_PASSWORD);
		generateResetPasswordToken(user);
		return new RecoveryUserResponse(user.getResetPasswordToken());
	}

	private void sendRecoveryMail(UserEntity userEntity, ActivationMethod activationMethod) {
		String userName = ofNullable(userEntity.getName()).orElse("User");
		String orgName = orgRepo.getOne(userEntity.getOrganizationId()).getName();
		try {
			// create parameter map to replace parameter by actual UserEntity data.
			Map<String, String> parametersMap = new HashMap<>();
			if (activationMethod == ActivationMethod.OTP) {
				UserOtpEntity userOtp = otpService.createUserOtp(userEntity, OtpType.RESET_PASSWORD);
				sendUserOtp(userEntity, userOtp.getOtp());
			} else if (activationMethod == ActivationMethod.VERIFICATION_LINK) {
				parametersMap.put(USERNAME_PARAMETER, userName);
				parametersMap.put(CHANGE_PASSWORD_URL_PARAMETER, appConfig.mailRecoveryUrl.concat(userEntity.getResetPasswordToken()));
				this.mailService.send(orgName, userEntity.getEmail(), CHANGE_PASSWORD_EMAIL_SUBJECT, CHANGE_PASSWORD_EMAIL_TEMPLATE, parametersMap);
			}
		} catch (Exception e) {
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, GEN$0003, e.getMessage());
		}
	}

	@Override
	public UserApiResponse activateUserAccount(ActivateOtpDto activateOtpDto) {
		UserEntity user = userRepository.findByEmailAndOrganizationId(activateOtpDto.getEmail(), activateOtpDto.getOrgId())
				.orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, U$EMP$0004, activateOtpDto.getEmail()));
		otpService.validateOtp(activateOtpDto.getOtp(), user, OtpType.REGISTER);
		activateUserInDB(user);
		//Get Package Registered In Organization
		Long packageId = packageService.getPackageIdRegisteredInOrg(user);
		UserApiResponse userApiResponse = securityService.login(user, false);
		userApiResponse.setPackageId(packageId);
		return userApiResponse;
	}

	@Transactional
	@Override
	public UserApiResponse updateUserAvatar(MultipartFile file) {

		UserEntity userEntity = (UserEntity) securityService.getCurrentUser();

		List<ResponseStatus> successResponseStatusList = new ArrayList<>();

		String imageUrl = fileService.saveFileForUser(file, userEntity.getId());

		if (isNotBlankOrNull(imageUrl)) {

			String oldImageUrl = userEntity.getImage();

			//First, set the new image url
			userEntity.setImage(imageUrl);

			userEntity = userRepository.save(userEntity);

			fileService.deleteFileByUrl(oldImageUrl);

		}

		if (successResponseStatusList.isEmpty()) {
			successResponseStatusList.add(ResponseStatus.ACTIVATED);
		}
		//display  user Id, url of image
		return new UserApiResponse(userEntity.getId(), imageUrl, successResponseStatusList);
	}

	@Override
	public UserApiResponse processUserAvatar(ImageBase64 image) throws IOException {
		MultipartFile userAvatar = MultipartFileUtils.convert(image.getBase64(), image.getFileName(), image.getFileType());
		return updateUserAvatar(userAvatar);
	}

	@Override
	public void updateUserPhone(Long userId, Long organizationId ,String phone){
		UserEntity user = userRepository.findByIdAndOrganizationId(userId, organizationId)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, U$0001, userId));
		if(StringUtils.isBlankOrNull(user.getPhoneNumber()) || !user.getPhoneNumber().equals(phone)){
			user.setPhoneNumber(phone);
			userRepository.save(user);
		}
	}

	@Override
	public List<UserRepresentationObject> getUserData(String anonymous) {
		return userRepository.findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(anonymous,anonymous)
				.stream().map(UserEntity::getRepresentation).toList();
	}

	@Override
	public String getUsernameById(Long id) {
		return userRepository.findById(id).
				orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND,  U$0001, id)).getName();
	}


}
