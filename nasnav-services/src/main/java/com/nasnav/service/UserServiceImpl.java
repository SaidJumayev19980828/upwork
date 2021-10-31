package com.nasnav.service;

import com.google.common.collect.ObjectArrays;
import com.nasnav.AppConfig;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.*;
import com.nasnav.dto.AddressDTO;
import com.nasnav.dto.AddressRepObj;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.user.ActivationEmailResendDTO;
import com.nasnav.enumerations.LoyaltyEvents;
import com.nasnav.enumerations.UserStatus;
import com.nasnav.exceptions.EntityValidationException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.helpers.UserServicesHelper;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;

import static com.nasnav.commons.utils.StringUtils.generateUUIDToken;
import static com.nasnav.commons.utils.StringUtils.isNotBlankOrNull;
import static com.nasnav.constatnts.EmailConstants.*;
import static com.nasnav.enumerations.Roles.NASNAV_ADMIN;
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
public class UserServiceImpl implements UserService {

	private Logger logger = LogManager.getLogger();

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserServicesHelper userServicesHelper;
	@Autowired
	private SecurityService securityService;
	@Autowired
	private DomainService domainService;
	@Autowired
	private OrganizationService orgService;
	@Autowired
	private MailService mailService;

	@Autowired
	private UserTokenRepository userTokenRepo;
	@Autowired
	private AddressRepository addressRepo;
	@Autowired
	private UserAddressRepository userAddressRepo;
	@Autowired
	private CommonUserRepository commonUserRepo;
	@Autowired
	private OrganizationRepository orgRepo;
	@Autowired
	private AreaRepository areaRepo;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserSubscriptionRepository subsRepo;

	@Autowired
	AppConfig appConfig;

	@Autowired
	private SubAreaRepository subAreaRepo;
	@Autowired
	CoinsDropService coinsDropService;
	@Autowired
	MetaOrderRepository metaOrderRepository;
	@Autowired
	TierService tierService;
	@Autowired
	BoosterRepository boosterRepository;

	@Override
	public UserApiResponse registerUserV2(UserDTOs.UserRegistrationObjectV2 userJson) {
		validateNewUserRegistration(userJson);

		UserEntity user = createNewUserEntity(userJson);		
		setUserAsDeactivated(user);
		generateResetPasswordToken(user);
		user = userRepository.saveAndFlush(user);
		
		sendActivationMail(user, userJson.getRedirectUrl());

		return new UserApiResponse(user.getId(), asList(NEED_ACTIVATION, ACTIVATION_SENT));
	}

	
	
	
	private void validateNewUserRegistration(UserDTOs.UserRegistrationObjectV2 userJson) {
		if (!userJson.confirmationFlag) {
			throw new EntityValidationException("Registration not confirmed by user!", null, NOT_ACCEPTABLE);
		}

		userServicesHelper.validateBusinessRules(userJson.getName(), userJson.getEmail(), userJson.getOrgId());
		userServicesHelper.validateNewPassword(userJson.password);

		Long orgId = userJson.getOrgId();

		OrganizationEntity org = orgRepo.findById(orgId)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, G$ORG$0001));

		if (userRepository.existsByEmailIgnoreCaseAndOrganizationId(userJson.email, orgId)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$LOG$0007, userJson.getEmail(), userJson.getOrgId());
		}
		
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
			userEntity.setEmail(userJson.email);
			generateResetPasswordToken(userEntity);
			userEntity = userRepository.saveAndFlush(userEntity);
			sendRecoveryMail(userEntity);
			successResponseStatusList.addAll(asList(NEED_ACTIVATION, ACTIVATION_SENT));
		}
		if (isNotBlankOrNull(userJson.getFamilyId())) {
			coinsDropService.giveUserCoinsNewFamilyMember(userEntity);
			updateUserBoosterByFamilyMember(userEntity.getId());
		}
		if (isNotBlankOrNull(userJson.getTierId())) {
			coinsDropService.giveUserCoinsNewTier(userEntity);
		}
		if (isNotBlankOrNull(userJson.getFamilyId())) {
			coinsDropService.giveUserCoinsNewFamilyMember(userEntity);
		}
		String [] defaultIgnoredProperties = new String[]{"name", "email", "org_id", "shop_id", "role"};
		String [] allIgnoredProperties = new HashSet<String>(
				  asList(ObjectArrays.concat(getNullProperties(userJson), defaultIgnoredProperties, String.class))).toArray(new String[0]);

		BeanUtils.copyProperties(userJson, userEntity, allIgnoredProperties);
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

	@Override
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
	

	@Override
	public UserRepresentationObject getUserData(Long userId, Boolean isEmployee) {
		BaseUserEntity currentUser = securityService.getCurrentUser();
		
		if(!securityService.currentUserHasRole(NASNAV_ADMIN)) {
			return getUserRepresentationWithUserRoles(currentUser);
		}
		
		Boolean isEmp = ofNullable(isEmployee).orElse(false);
		Long requiredUserId = ofNullable(userId).orElse(currentUser.getId());		
				
		BaseUserEntity user = commonUserRepo.findById(requiredUserId, isEmp)
							.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, U$0001, userId));
		
		return getUserRepresentationWithUserRoles(user);
	}



	@Override
	public RedirectView activateUserAccount(String token, String redirect)  {
		UserEntity user = userRepository.findByResetPasswordToken(token);

		checkUserActivation(user);
		validateActivationRedirectUrl(redirect, user.getOrganizationId());
		
		activateUserInDB(user);
		// using securityService.getCurrentUserOrganizationId() causes the api to fail because no current user exists
		Long orgId = user.getOrganizationId();
		Long userId = user.getId();
		if (userId > 0 && coinsDropService.getByOrganizationIdAndTypeId(orgId, LoyaltyEvents.SIGN_UP.getValue().intValue()) != null) {
			coinsDropService.giveUserCoinsSignUp(user);
		}
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
		
		sendActivationMail(user, accountInfo.getRedirectUrl());
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
		if (securityService.currentUserHasRole(NASNAV_ADMIN)) {
			customers = userRepository.findAll();
		} else {
			customers = userRepository.findByOrganizationId(securityService.getCurrentUserOrganizationId());
		}
		return customers
				.stream()
				.map(u -> u.getRepresentation())
				.collect(toList());
	}

	@Override
	public List<UserEntity> getYeshteryUsersByAllowReward(Boolean allowReward) {
		return userRepository.findByYeshteryUserIdNotNullAndAllowReward(allowReward);
	}

	@Override
	public void updateUserByFamilyId(Long familyId, Long userId) {
		if (userId > 0 && familyId > 0) {
			userRepository.updateUserWithFamilyId(familyId, userId);
			UserEntity userEntity = userRepository.findById(userId).get();
			if (userEntity.getFamily().getId() > 0) {
				coinsDropService.giveUserCoinsNewFamilyMember(userEntity);
			}
		}
	}

	@Override
	public void updateUserByTierIdAndOrgId(Long tierId, Long userId, Long orgId) {
		if (tierId < 0) {
			tierId = getTierIdByUserOrders(orgId, userId);
		}
		if (userId > 0 && tierId > 0) {
			userRepository.updateUserWithTierId(tierId, userId);
			UserEntity userEntity = userRepository.findById(userId).get();
			if (userEntity.getTier().getId() > 0) {
				coinsDropService.giveUserCoinsNewTier(userEntity);
			}
		}
	}

	@Override
	public List<UserEntity> getUsersByFamilyId(Long familyId) {
		return userRepository.findByFamily_Id(familyId);
	}

	@Override
	public void updateUserByTierId(Long tierId, Long userId) {
		if (userId > 0 && tierId > 0) {
			userRepository.updateUserWithTierId(tierId, userId);
			UserEntity userEntity = userRepository.findById(userId).get();
			if (userEntity.getTier().getId() > 0) {
				coinsDropService.giveUserCoinsNewTier(userEntity);
			}
		}
	}

	private Long getTierIdByUserOrders(Long orgId, Long userId) {
		if (orgId < 0) {
			orgId = securityService.getCurrentUserOrganizationId();
		}
		Integer orderCount = metaOrderRepository.countByUser_IdAndOrganization_IdAAndFinalizeStatus(userId, orgId);
		Long tierId = tierService.getTierByAmount(orderCount).getId();
		if (tierId > 0) {
			return tierId;
		}
		return -1L;
	}

	private void updateUserBoosterByFamilyMember(Long userId) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		UserEntity userEntity = getUserEntityById(userId);
		Long familyId = userEntity.getFamily().getId();
		if (familyId < 0) {
			return;
		}
		List<UserEntity> familyUsers = userRepository.getByFamily_IdAndOrganizationId(familyId, orgId);
		Integer familyCount = familyUsers.size();
		if (familyCount == 0) {
			return;
		}
		BoosterEntity boosterEntity = null;
		BoosterEntity userBoosterEntity = null;
		List<BoosterEntity> boosterList = new ArrayList<>();
		if (userEntity.getBooster() != null) {
			userBoosterEntity = userEntity.getBooster();
		}
		boosterList = boosterRepository.getAllByLinkedFamilyMember(familyCount+1);
		if (boosterList.isEmpty()) {
			boosterList = boosterRepository.getAllByNumberFamilyChildren(familyCount);
		}
		if (boosterList.size() > 0) {
			boosterEntity = boosterList.get(boosterList.size() - 1);
			if (userBoosterEntity != null && userBoosterEntity != boosterEntity) {
				if (userBoosterEntity.getLevelBooster() > boosterEntity.getLevelBooster()) {
					return;
				}
			}
			userEntity.setBooster(boosterEntity);
		}
		userRepository.save(userEntity);
	}
}
