package com.nasnav.service;

import static com.nasnav.commons.utils.StringUtils.validateNameAndEmail;
import static com.nasnav.constatnts.EmailConstants.ACCOUNT_EMAIL_PARAMETER;
import static com.nasnav.constatnts.EmailConstants.ACTIVATION_ACCOUNT_EMAIL_SUBJECT;
import static com.nasnav.constatnts.EmailConstants.ACTIVATION_ACCOUNT_URL_PARAMETER;
import static com.nasnav.constatnts.EmailConstants.NEW_EMAIL_ACTIVATION_TEMPLATE;
import static com.nasnav.constatnts.EmailConstants.USERNAME_PARAMETER;
import static com.nasnav.constatnts.EntityConstants.PASSWORD_MAX_LENGTH;
import static com.nasnav.constatnts.EntityConstants.PASSWORD_MIN_LENGTH;
import static com.nasnav.constatnts.EntityConstants.TOKEN_VALIDITY;
import static com.nasnav.enumerations.Roles.NASNAV_ADMIN;
import static com.nasnav.enumerations.UserStatus.ACTIVATED;
import static com.nasnav.enumerations.UserStatus.NOT_ACTIVATED;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.response.ResponseStatus.ACTIVATION_SENT;
import static com.nasnav.response.ResponseStatus.EMAIL_EXISTS;
import static com.nasnav.response.ResponseStatus.EXPIRED_TOKEN;
import static com.nasnav.response.ResponseStatus.INVALID_PASSWORD;
import static com.nasnav.response.ResponseStatus.INVALID_REDIRECT_URL;
import static com.nasnav.response.ResponseStatus.INVALID_TOKEN;
import static com.nasnav.response.ResponseStatus.NEED_ACTIVATION;
import static com.nasnav.response.UserApiResponse.createStatusApiResponse;
import static com.nasnav.service.helpers.LoginHelper.isInvalidRedirectUrl;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.nasnav.dao.AddressRepository;
import com.nasnav.dto.AddressDTO;
import com.nasnav.persistence.*;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.web.servlet.view.RedirectView;

import com.google.common.collect.ObjectArrays;
import com.nasnav.AppConfig;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.constatnts.EmailConstants;
import com.nasnav.dao.CommonUserRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.user.ActivationEmailResendDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.EntityValidationException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;

@Service
public class UserServiceImpl implements UserService {

	private Logger logger = LogManager.getLogger();
	private UserRepository userRepository;
	private MailService mailService;
	private PasswordEncoder passwordEncoder;

	
	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private AddressRepository addressRepo;

	@Autowired
	private CommonUserRepository commonUserRepo;
	
	@Autowired
	private OrganizationRepository orgRepo;


	@Autowired
	public UserServiceImpl(UserRepository userRepository, MailService mailService, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.mailService = mailService;
		this.passwordEncoder = passwordEncoder;
	}

	@Autowired
	AppConfig appConfig;

	@Override
	public UserApiResponse registerUserV2(UserDTOs.UserRegistrationObjectV2 userJson) throws BusinessException {
		validateNewUserRegistration(userJson);

		UserEntity user = createNewUserEntity(userJson);		
		setUserAsDeactivated(user);
		generateResetPasswordToken(user);
		user = userRepository.saveAndFlush(user);
		
		sendActivationMail(user, userJson.getRedirectUrl());

		UserApiResponse api = createStatusApiResponse(user.getId(),	asList(NEED_ACTIVATION, ACTIVATION_SENT));
		api.setMessages(new ArrayList<>());
		return api;
	}

	
	
	
	private void validateNewUserRegistration(UserDTOs.UserRegistrationObjectV2 userJson) throws BusinessException {
		if (!userJson.confirmationFlag) {
			throw new EntityValidationException("Registration not confirmed by user!", null, NOT_ACCEPTABLE);
		}

		validateNameAndEmail(userJson.name, userJson.email, userJson.getOrgId());

		validateNewPassword(userJson.password);

		if (userJson.getOrgId() == null) {
			throw new BusinessException("Required org_id is  missing!", "MISSING_PARAM: org_id", NOT_ACCEPTABLE);
		}

		Optional<OrganizationEntity> org = orgRepo.findById(userJson.getOrgId());
		if (!org.isPresent()) {
			throw new BusinessException(
					String.format("Provided org_id %d doesn't match any existing organization", userJson.getOrgId()),
					"INVALID_PARAM: org_id", NOT_ACCEPTABLE);
		}

		if (userRepository.existsByEmailIgnoreCaseAndOrganizationId(userJson.email, userJson.getOrgId())) {
			throw new EntityValidationException(
					"Invalid User Entity: " + EMAIL_EXISTS,
					createStatusApiResponse(singletonList(EMAIL_EXISTS)),
					NOT_ACCEPTABLE);
		}
		
		validateActivationRedirectUrl(userJson.getRedirectUrl());
	}




	private void validateActivationRedirectUrl(String redirectUrl) {
		if(isNull(redirectUrl) || isInvalidRedirectUrl(redirectUrl)) {
			throw new EntityValidationException(
					"Invalid User Entity: " + INVALID_REDIRECT_URL,
					createStatusApiResponse(singletonList(INVALID_REDIRECT_URL)),
					NOT_ACCEPTABLE);
		}
	}
	



	private UserApiResponse sendActivationMail(UserEntity userEntity, String redirectUrl) {
		UserApiResponse userApiResponse = new UserApiResponse();
		try {
			Map<String, String> parametersMap = createActivationEmailParameters(userEntity, redirectUrl);
			mailService.send(userEntity.getEmail(), ACTIVATION_ACCOUNT_EMAIL_SUBJECT,
					NEW_EMAIL_ACTIVATION_TEMPLATE, parametersMap);
		} catch (Exception e) {
			logger.error(e, e);
			userApiResponse.setMessages(singletonList(e.getMessage()));
			throw new EntityValidationException("Could not send Email ", userApiResponse,
					INTERNAL_SERVER_ERROR);
		}
		return userApiResponse;
	}




	private Map<String, String> createActivationEmailParameters(UserEntity userEntity, String redirectUrl) {
		String activationRedirectUrl = buildActivationRedirectUrl(userEntity, redirectUrl);
		
		Map<String, String> parametersMap = new HashMap<>();
		parametersMap.put(USERNAME_PARAMETER, userEntity.getName());
		parametersMap.put(ACCOUNT_EMAIL_PARAMETER, userEntity.getEmail());
		parametersMap.put(ACTIVATION_ACCOUNT_URL_PARAMETER, activationRedirectUrl);				
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
		return user;
	}



	private void setUserAsDeactivated(UserEntity user) {
		user.setUserStatus(NOT_ACTIVATED.getValue());
	}

	
	
	@Override
	public Boolean isUserDeactivated(BaseUserEntity user) {
		if(user instanceof EmployeeUserEntity) {
			return Objects.equals(user.getAuthenticationToken(), DEACTIVATION_CODE);
		}else if(user instanceof UserEntity){
			UserEntity userEntity =  (UserEntity)user;
			return userEntity.getUserStatus().equals(NOT_ACTIVATED.getValue());
		}else {
			return false;
		}
		
	}


	@Override
	public UserApiResponse registerUser(UserDTOs.UserRegistrationObject userJson) {
		this.validateBusinessRules(userJson);

		if(!userRepository.existsByEmailIgnoreCaseAndOrganizationId(userJson.email, userJson.getOrgId())) {
			UserEntity userEntity = createUserEntity(userJson);
			generateResetPasswordToken(userEntity);
			userEntity = userRepository.saveAndFlush(userEntity);
			sendRecoveryMail(userEntity);
			UserApiResponse api = UserApiResponse.createStatusApiResponse(userEntity.getId(),
					Arrays.asList(NEED_ACTIVATION, ACTIVATION_SENT));
			api.setMessages(new ArrayList<>());
			return api;
		}
		throw new EntityValidationException(
				"Invalid User Entity: " + EMAIL_EXISTS,
				UserApiResponse.createStatusApiResponse(singletonList(EMAIL_EXISTS)),
				NOT_ACCEPTABLE);
	}
	
	
	
	

	private UserEntity createUserEntity(UserDTOs.UserRegistrationObject userJson) {
		UserEntity user = UserEntity.registerUser(userJson);
		return userRepository.save(user);
	}

	@Override
	public UserApiResponse updateUser(String userToken, UserDTOs.EmployeeUserUpdatingObject userJson) throws BusinessException {

		UserEntity userEntity = (UserEntity) securityService.getCurrentUser();
		List<ResponseStatus> failResponseStatusList = new ArrayList<>();
		List<ResponseStatus> successResponseStatusList = new ArrayList<>();
		if (StringUtils.isNotBlankOrNull(userJson.getName()))
			if (StringUtils.validateName(userJson.getName())) {
				userEntity.setName(userJson.getName());
			} else {
				failResponseStatusList.add(ResponseStatus.INVALID_NAME);
			}
		if (StringUtils.isNotBlankOrNull(userJson.email)){
			if (StringUtils.validateEmail(userJson.email)) {
				userEntity.setEmail(userJson.email);
				generateResetPasswordToken(userEntity);
				userEntity = userRepository.saveAndFlush(userEntity);
				sendRecoveryMail(userEntity);
				successResponseStatusList.add(ResponseStatus.NEED_ACTIVATION);
				successResponseStatusList.add(ResponseStatus.ACTIVATION_SENT);
			} else {
				failResponseStatusList.add(ResponseStatus.INVALID_EMAIL);
			}
		}
		String [] defaultIgnoredProperties = new String[]{"name", "email", "org_id", "store_id", "role"};
		String [] allIgnoredProperties = new HashSet<String>(
				  Arrays.asList(ObjectArrays.concat(getNullProperties(userJson), defaultIgnoredProperties, String.class))).toArray(new String[0]);
		if (failResponseStatusList.isEmpty()) {
			BeanUtils.copyProperties(userJson, userEntity, allIgnoredProperties);
			if (userJson.getAddress() != null) {
				AddressesEntity address = new AddressesEntity();
				AddressDTO addressDTO = userJson.getAddress();
				Set<AddressesEntity> userAddresses = addressRepo.findByUserId(userEntity.getId());
				if (addressDTO.getId() != null) {
					AddressesEntity oldAddress = ofNullable(addressRepo.findByIdAndUserId(addressDTO.getId(), userEntity.getId()))
							.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0002, addressDTO.getId()));
					userAddresses.remove(oldAddress);
					addressRepo.unlinkAddressFromUser(addressDTO.getId(), userEntity.getId());
				}
				BeanUtils.copyProperties(userJson.getAddress(), address, new String[] {"id"});
				if (!address.equals(new AddressesEntity())) {
					address = addressRepo.save(address);
					userAddresses.add(address);
				}
				userEntity.setAddresses(userAddresses);
			}
			userRepository.saveAndFlush(userEntity);
			if (successResponseStatusList.isEmpty()) {
				successResponseStatusList.add(ResponseStatus.ACTIVATED);
			}
			return UserApiResponse.createMessagesApiResponse(true, successResponseStatusList);
		}
		throw new BusinessException(""+failResponseStatusList, "INVALID_PARAMS", HttpStatus.NOT_ACCEPTABLE);
	}


	/**
	 * validateBusinessRules passed user entity against business rules
	 *
	 * @param userJson User entity to be validated
	 */
	private void validateBusinessRules(UserDTOs.UserRegistrationObject userJson) {
		StringUtils.validateNameAndEmail(userJson.name, userJson.email, userJson.getOrgId());
	}

	@Override
	public void deleteUser(Long userId) {
		userRepository.deleteById(userId);
	}

	@Override
	public UserEntity findUserById(Long userId) {
		Optional<UserEntity> optional = userRepository.findById(userId);
		return optional.isPresent() ? optional.get() : null;

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
	public UserApiResponse sendEmailRecovery(String email, Long orgId) {
		UserEntity userEntity = getUserEntityByEmailAndOrgId(email, orgId);
		generateResetPasswordToken(userEntity);
		userEntity = userRepository.saveAndFlush(userEntity);
		return sendRecoveryMail(userEntity);
	}

	/**
	 * Get user by passed email and organization id
	 *
	 * @param email user entity email
	 * @param orgId user organization id
	 * @return user entity
	 */
	private UserEntity getUserEntityByEmailAndOrgId(String email, Long orgId) {
		// first ensure that email is valid
		if (!StringUtils.validateEmail(email)) {
			UserApiResponse userApiResponse = UserApiResponse.createMessagesApiResponse(false,
					Collections.singletonList(ResponseStatus.INVALID_EMAIL));
			throw new EntityValidationException("INVALID_EMAIL :" + email, userApiResponse, HttpStatus.NOT_ACCEPTABLE);
		}
		// load user entity by email
		UserEntity userEntity = this.userRepository.getByEmailAndOrganizationId(email, orgId);
		if (StringUtils.isBlankOrNull(userEntity)) {
			UserApiResponse userApiResponse = UserApiResponse.createMessagesApiResponse(false,
					Collections.singletonList(ResponseStatus.EMAIL_NOT_EXIST));
			throw new EntityValidationException("EMAIL_NOT_EXIST", userApiResponse, HttpStatus.NOT_ACCEPTABLE);
		}
		return userEntity;
	}


	
	
	
	private void generateResetPasswordToken(UserEntity userEntity) {
		String generatedToken = generateResetPasswordToken();
		userEntity.setResetPasswordToken(generatedToken);
		userEntity.setResetPasswordSentAt(now());
	}

	
	
	
	private UserApiResponse sendRecoveryMail(UserEntity userEntity) {
		UserApiResponse userApiResponse = new UserApiResponse();
		try {
			// create parameter map to replace parameter by actual UserEntity data.
			Map<String, String> parametersMap = new HashMap<>();
			parametersMap.put(EmailConstants.USERNAME_PARAMETER, userEntity.getName());
			parametersMap.put(EmailConstants.CHANGE_PASSWORD_URL_PARAMETER,
					appConfig.mailRecoveryUrl.concat(userEntity.getResetPasswordToken()));
			// send Recovery mail to user
			this.mailService.send(userEntity.getEmail(), EmailConstants.CHANGE_PASSWORD_EMAIL_SUBJECT,
					EmailConstants.CHANGE_PASSWORD_EMAIL_TEMPLATE, parametersMap);
			// set success to true after sending mail.
		} catch (Exception e) {
			userApiResponse.setMessages(Collections.singletonList(e.getMessage()));
			throw new EntityValidationException("Could not send Email ", userApiResponse,
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return userApiResponse;
	}

	/**
	 * generate new ResetPasswordToken and ensure that this ResetPasswordToken is
	 * never used before.
	 *
	 * @return unique generated ResetPasswordToken.
	 */
	private String generateResetPasswordToken() {
		String generatedToken = StringUtils.generateUUIDToken();
		boolean existsByToken = userRepository.existsByResetPasswordToken(generatedToken);
		if (existsByToken) {
			return reGenerateResetPasswordToken();
		}
		return generatedToken;
	}

	/**
	 * regenerate ResetPasswordToken and if token already exists, make recursive
	 * call until generating new ResetPasswordToken.
	 *
	 * @return unique generated ResetPasswordToken.
	 */
	private String reGenerateResetPasswordToken() {
		String generatedToken = StringUtils.generateUUIDToken();
		boolean existsByToken = userRepository.existsByResetPasswordToken(generatedToken);
		if (existsByToken) {
			return reGenerateResetPasswordToken();
		}
		return generatedToken;
	}

	@Override
	public UserApiResponse recoverUser(UserDTOs.PasswordResetObject data) {
		validateNewPassword(data.password);
		UserEntity userEntity = userRepository.getByResetPasswordToken(data.token);
		if (StringUtils.isNotBlankOrNull(userEntity)) {
			// if resetPasswordToken is not active, throw exception for invalid
			// resetPasswordToken
			checkResetPasswordTokenExpiry(userEntity);
			userEntity.setResetPasswordToken(null);
			userEntity.setResetPasswordSentAt(null);
			userEntity.setEncryptedPassword(passwordEncoder.encode(data.password));
			userRepository.saveAndFlush(userEntity);
		} else {
			throw new EntityValidationException("INVALID_TOKEN  ",
					UserApiResponse.createStatusApiResponse(singletonList(INVALID_TOKEN)),
					HttpStatus.NOT_ACCEPTABLE);
		}

		return UserApiResponse.createStatusApiResponse(userEntity.getId(), null);
	}

	private void validateNewPassword(String newPassword) {
		if (StringUtils.isBlankOrNull(newPassword) || newPassword.length() > PASSWORD_MAX_LENGTH
				|| newPassword.length() < PASSWORD_MIN_LENGTH) {
			throw new EntityValidationException("INVALID_PASSWORD  ",
					UserApiResponse.createStatusApiResponse(singletonList(INVALID_PASSWORD)),
					NOT_ACCEPTABLE);
		}
	}

	

	
	private void checkResetPasswordTokenExpiry(UserEntity userEntity) {
		LocalDateTime resetPasswordSentAt = userEntity.getResetPasswordSentAt();
		LocalDateTime tokenExpiryDate = resetPasswordSentAt.plusHours(TOKEN_VALIDITY);
		if (now().isAfter(tokenExpiryDate)) {
			throw new EntityValidationException("EXPIRED_TOKEN  ",
					createStatusApiResponse(singletonList(EXPIRED_TOKEN)),
					NOT_ACCEPTABLE);
		}
	}


	
	
	@Override
	public boolean checkAuthToken(Long userId, String authToken) {
		return userRepository.existsByIdAndAuthenticationToken(userId, authToken);
	}
	
	
	

	@Override
	public UserRepresentationObject getUserData(Long userId, Boolean isEmployee) throws BusinessException {
		BaseUserEntity currentUser = securityService.getCurrentUser();
		
		if(!securityService.currentUserHasRole(NASNAV_ADMIN)) {
			return getUserRepresentationWithUserRoles(currentUser);
		}
		
		Boolean isEmp = ofNullable(isEmployee).orElse(false);
		Long requiredUserId = ofNullable(userId).orElse(currentUser.getId());		
				
		BaseUserEntity user = 
				commonUserRepo.findById(requiredUserId, isEmp)
							.orElseThrow(() -> getNoUserHaveThisIdException(requiredUserId));			
		
		return getUserRepresentationWithUserRoles(user);
	}



	@Override
	public RedirectView activateUserAccount(String token, String redirect) throws BusinessException {
		UserEntity user = userRepository.findByResetPasswordToken(token);

		checkUserActivation(user);
		validateActivationRedirectUrl(redirect);
		
		activateUserInDB(user);
		return redirectUser(securityService.login(user, false).getToken(), redirect);
	}




	private void activateUserInDB(UserEntity user) {
		user.setResetPasswordToken(null);
		user.setUserStatus(ACTIVATED.getValue());
		userRepository.save(user);
	}

	
	
	private RedirectView redirectUser(String authToken, String loginUrl) {
		//String loginUrl = buildOrgLoginPageUrl(orgId);
		
		RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();
		attributes.addAttribute("auth_token", authToken);
		
		RedirectView redirectView = new RedirectView();	
		redirectView.setUrl(loginUrl);
		redirectView.setAttributesMap(attributes);

		return redirectView;
	}





	private void checkUserActivation(UserEntity user) throws BusinessException {
		if (user == null)
			throw new BusinessException("Provided token is invalid", "INVALID_PARAM: token", UNAUTHORIZED);

		checkResetPasswordTokenExpiry(user);

		if (!isUserDeactivated(user))
			throw new BusinessException("Account is already activated!", "INVALID_OPERATION: token", NOT_ACCEPTABLE);
	}
	
	
	

	private UserRepresentationObject getUserRepresentationWithUserRoles(BaseUserEntity user) {
		UserRepresentationObject userRepObj = user.getRepresentation();
		userRepObj.setAddresses(getUserAddresses(userRepObj.getId()));
		userRepObj.setRoles(new HashSet<>(commonUserRepo.getUserRoles(user)));
		return userRepObj;
	}
	
	
	private Set getUserAddresses(Long userId){
		return addressRepo.findByUserId(userId)
						  .stream()
						  .map(AddressesEntity::getRepresentation)
						  .collect(Collectors.toSet());
	}

	
	private BusinessException getNoUserHaveThisIdException(Long id) {
		return new BusinessException("Provided id doesn't match any existing user with id: "+id, "INVALID PARAM: id", HttpStatus.NOT_ACCEPTABLE);
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
	public void resendActivationEmail(ActivationEmailResendDTO accountInfo) throws BusinessException {		
		String email = accountInfo.getEmail();
		Long orgId = accountInfo.getOrgId();
		BaseUserEntity baseUser = commonUserRepo.getByEmailAndOrganizationId(email, orgId);
		validateActivationEmailResend(accountInfo, baseUser);
		
		UserEntity user = (UserEntity)baseUser;
		if(isUserDeactivated(user) && isNull(user.getResetPasswordToken())) {
			generateResetPasswordToken(user);
			userRepository.save(user);
		}
		
		sendActivationMail(user, accountInfo.getRedirectUrl());
	}




	private void validateActivationEmailResend(ActivationEmailResendDTO accountInfo, BaseUserEntity user) throws BusinessException {
		String email = accountInfo.getEmail();
		Long orgId = accountInfo.getOrgId();
		if(user == null || !(user instanceof UserEntity)) {
			throw new BusinessException(
					format(UXACTVX0001.getValue(), email, orgId)
					, UXACTVX0001
					, NOT_ACCEPTABLE);
		}else if(!isUserDeactivated(user)){
			throw new BusinessException(
					format(UXACTVX0002.getValue(), email)
					, UXACTVX0002
					, NOT_ACCEPTABLE);
		}else if(resendRequestedTooSoon(accountInfo)) {
			throw new BusinessException(
					format(UXACTVX0003.getValue(), email)
					, UXACTVX0003
					, NOT_ACCEPTABLE);
		}			 		
	}




	private boolean resendRequestedTooSoon(ActivationEmailResendDTO accountInfo) {
		// TODO Auto-generated method stub
		return false;
	}




	@Override
	public UserApiResponse activateUserAccount(String token) throws BusinessException {
		UserEntity user = userRepository.findByResetPasswordToken(token);

		checkUserActivation(user);
		
		activateUserInDB(user);
		return securityService.login(user, false);
	}
}
