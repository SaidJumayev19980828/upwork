package com.nasnav.service;

import static com.nasnav.commons.utils.StringUtils.validateNameAndEmail;
import static com.nasnav.constatnts.EmailConstants.ACCOUNT_EMAIL_PARAMETER;
import static com.nasnav.constatnts.EmailConstants.ACTIVATION_ACCOUNT_URL_PARAMETER;
import static com.nasnav.constatnts.EmailConstants.NEW_EMAIL_ACTIVATION_TEMPLATE;
import static com.nasnav.constatnts.EmailConstants.USERNAME_PARAMETER;
import static com.nasnav.constatnts.EntityConstants.PASSWORD_MAX_LENGTH;
import static com.nasnav.constatnts.EntityConstants.PASSWORD_MIN_LENGTH;
import static com.nasnav.constatnts.EntityConstants.PROTOCOL;
import static com.nasnav.constatnts.EntityConstants.TOKEN_VALIDITY;
import static com.nasnav.enumerations.Roles.NASNAV_ADMIN;
import static com.nasnav.response.ResponseStatus.ACTIVATION_SENT;
import static com.nasnav.response.ResponseStatus.EMAIL_EXISTS;
import static com.nasnav.response.ResponseStatus.EXPIRED_TOKEN;
import static com.nasnav.response.ResponseStatus.INVALID_PASSWORD;
import static com.nasnav.response.ResponseStatus.INVALID_TOKEN;
import static com.nasnav.response.ResponseStatus.NEED_ACTIVATION;
import static com.nasnav.response.UserApiResponse.createStatusApiResponse;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
import com.nasnav.dao.OrganizationDomainsRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.EntityValidationException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.OrganizationDomainsEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;

@Service
public class UserServiceImpl implements UserService {

	private UserRepository userRepository;
	private MailService mailService;
	private PasswordEncoder passwordEncoder;

	
	@Autowired
	private SecurityService securityService;
	
	
	@Autowired
	private CommonUserRepository commonUserRepo;
	
	
	@Autowired
	private OrganizationDomainsRepository orgDomainRepo;

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
		
		sendActivationMail(user);

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

		if (userRepository.existsByEmailIgnoreCaseAndOrganizationId(userJson.email, userJson.getOrgId())) {
			throw new EntityValidationException(
					"Invalid User Entity: " + EMAIL_EXISTS,
					createStatusApiResponse(singletonList(EMAIL_EXISTS)),
					NOT_ACCEPTABLE);
		}
			
	}



	private UserApiResponse sendActivationMail(UserEntity userEntity) {
		UserApiResponse userApiResponse = new UserApiResponse();
		try {
			// create parameter map to replace parameter by actual UserEntity data.
			Map<String, String> parametersMap = new HashMap<>();
			parametersMap.put(USERNAME_PARAMETER, userEntity.getName());
			parametersMap.put(ACCOUNT_EMAIL_PARAMETER, userEntity.getEmail());
			parametersMap.put(ACTIVATION_ACCOUNT_URL_PARAMETER,
					appConfig.accountActivationUrl.concat(userEntity.getResetPasswordToken()));
			// send Recovery mail to user
			this.mailService.send(userEntity.getEmail(), EmailConstants.ACTIVATION_ACCOUNT_EMAIL_SUBJECT,
					NEW_EMAIL_ACTIVATION_TEMPLATE, parametersMap);
			// set success to true after sending mail.
			userApiResponse.setSuccess(true);
		} catch (Exception e) {
			userApiResponse.setSuccess(false);
			userApiResponse.setMessages(singletonList(e.getMessage()));
			throw new EntityValidationException("Could not send Email ", userApiResponse,
					INTERNAL_SERVER_ERROR);
		}
		return userApiResponse;
	}



	private UserEntity createNewUserEntity(UserDTOs.UserRegistrationObjectV2 userJson) {
		UserEntity user = new UserEntity();
		user.setName(userJson.getName());
		user.setEmail(userJson.getEmail());
		user.setOrganizationId(userJson.getOrgId());
		user.setEncryptedPassword(passwordEncoder.encode(userJson.password));		
		return user;
	}



	private void setUserAsDeactivated(UserEntity user) {
		user.setAuthenticationToken(DEACTIVATION_CODE);
	}

	
	
	@Override
	public Boolean isUserDeactivated(BaseUserEntity user) {
		return Objects.equals(user.getAuthenticationToken(), DEACTIVATION_CODE);
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

	/**
	 * Generate ResetPasswordToken and assign it to passed user entity
	 *
	 * @param userEntity user entity
	 * @return user entity after generating ResetPasswordToken and updating entity.
	 */
	private void generateResetPasswordToken(UserEntity userEntity) {
		String generatedToken = generateResetPasswordToken();
		userEntity.setResetPasswordToken(generatedToken);
		userEntity.setResetPasswordSentAt(now());
	}

	/**
	 * Send An Email to user.
	 *
	 * @param userEntity user entity
	 * @return UserApiResponse representing the status of sending email.
	 */
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
			userApiResponse.setSuccess(true);
		} catch (Exception e) {
			userApiResponse.setSuccess(false);
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
	public RedirectView activateUserAccount(String token) throws BusinessException {
		UserEntity user = userRepository.findByResetPasswordToken(token);

		checkUserActivation(user);
		user.setResetPasswordToken(null);
		
		return redirectUser(securityService.login(user).getToken(), user.getOrganizationId());
	}

	
	
	private RedirectView redirectUser(String authToken, Long orgId) {
		String loginUrl = buildOrgLoginPageUrl(orgId);
		
		RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();
		attributes.addAttribute("token", authToken);
		
		RedirectView redirectView = new RedirectView();	
		redirectView.setUrl(loginUrl);
		redirectView.setAttributesMap(attributes);

		return redirectView;
	}




	private String buildOrgLoginPageUrl(Long orgId) {
		OrganizationDomainsEntity orgDomain = orgDomainRepo.findByOrganizationEntity_Id(orgId);
		String domain = orgDomain.getDomain();
		String subDir = ofNullable("/"+orgDomain.getSubdir()).orElse("");
		String loginUrl = String.format("%s%s%s/login", PROTOCOL, domain, subDir);
		return loginUrl;
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
		userRepObj.setRoles(new HashSet<>(commonUserRepo.getUserRoles(user)));
		return userRepObj;
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
}
