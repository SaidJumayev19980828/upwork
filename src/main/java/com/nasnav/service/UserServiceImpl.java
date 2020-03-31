package com.nasnav.service;

import static com.nasnav.enumerations.Roles.NASNAV_ADMIN;
import static java.util.Optional.ofNullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.nasnav.dao.OrganizationRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.common.collect.ObjectArrays;
import com.nasnav.AppConfig;
import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.constatnts.EmailConstants;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dao.CommonUserRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.enumerations.Roles;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.EntityValidationException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.web.servlet.view.RedirectView;

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

		user = generateResetPasswordToken(user);

		sendActivationMail(user);

		UserApiResponse api = UserApiResponse.createStatusApiResponse(user.getId(),
				Arrays.asList(ResponseStatus.NEED_ACTIVATION, ResponseStatus.ACTIVATION_SENT));
		api.setMessages(new ArrayList<>());
		return api;
	}

	private void validateNewUserRegistration(UserDTOs.UserRegistrationObjectV2 userJson) throws BusinessException {
		if (!userJson.confirmationFlag)
			throw new EntityValidationException("Registration not confirmed by user!",
					null, HttpStatus.NOT_ACCEPTABLE);

		StringUtils.validateNameAndEmail(userJson.name, userJson.email, userJson.getOrgId());

		validateNewPassword(userJson.password);

		if (userJson.getOrgId() == null)
			throw new BusinessException("Required org_id is  missing!", "MISSING_PARAM: org_id", HttpStatus.NOT_ACCEPTABLE);

		if (userRepository.existsByEmailAndOrgId(userJson.email, userJson.getOrgId()) != null)
			throw new EntityValidationException("Invalid User Entity: " + ResponseStatus.EMAIL_EXISTS,
					UserApiResponse.createStatusApiResponse(Collections.singletonList(ResponseStatus.EMAIL_EXISTS)),
					HttpStatus.NOT_ACCEPTABLE);
	}



	private UserApiResponse sendActivationMail(UserEntity userEntity) {
		UserApiResponse userApiResponse = new UserApiResponse();
		try {
			// create parameter map to replace parameter by actual UserEntity data.
			Map<String, String> parametersMap = new HashMap<>();
			parametersMap.put(EmailConstants.USERNAME_PARAMETER, userEntity.getName());
			parametersMap.put(EmailConstants.ACCOUNT_EMAIL_PARAMETER, userEntity.getEmail());
			parametersMap.put(EmailConstants.ACTIVATION_ACCOUNT_URL_PARAMETER,
					appConfig.accountActivationUrl.concat(userEntity.getResetPasswordToken()));
			// send Recovery mail to user
			this.mailService.send(userEntity.getEmail(), EmailConstants.ACTIVATION_ACCOUNT_EMAIL_SUBJECT,
					EmailConstants.NEW_EMAIL_ACTIVATION_TEMPLATE, parametersMap);
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



	private UserEntity createNewUserEntity(UserDTOs.UserRegistrationObjectV2 userJson) {
		UserEntity user = new UserEntity();
		user.setName(userJson.getName());
		user.setEmail(userJson.getEmail());
		user.setOrganizationId(userJson.getOrgId());
		user.setEncryptedPassword(passwordEncoder.encode(userJson.password));
		user = setUserTokenDeactivated(user);
		return user;
	}



	private UserEntity setUserTokenDeactivated(UserEntity user) {
		user.setAuthenticationToken("0000-0000-0000-0000");
		return user;
	}

	private Boolean checkUserDeactivated(UserEntity user) {
		return user.getAuthenticationToken().equals("0000-0000-0000-0000");
	}

	@Override
	public UserApiResponse registerUser(UserDTOs.UserRegistrationObject userJson) {
		// validate user entity against business rules.
		this.validateBusinessRules(userJson);
		// check if a user with the same email and org_Id already exists
		if (userRepository.existsByEmailAndOrgId(userJson.email, userJson.getOrgId()) == null) {
			// create and save a user from the json object
			UserEntity userEntity = createUserEntity(userJson);
			// send activation email
			userEntity = generateResetPasswordToken(userEntity);
			sendRecoveryMail(userEntity);
			UserApiResponse api = UserApiResponse.createStatusApiResponse(userEntity.getId(),
					Arrays.asList(ResponseStatus.NEED_ACTIVATION, ResponseStatus.ACTIVATION_SENT));
			api.setMessages(new ArrayList<>());
			return api;
		}
		throw new EntityValidationException("Invalid User Entity: " + ResponseStatus.EMAIL_EXISTS,
				UserApiResponse.createStatusApiResponse(Collections.singletonList(ResponseStatus.EMAIL_EXISTS)),
				HttpStatus.NOT_ACCEPTABLE);
	}

	private UserEntity createUserEntity(UserDTOs.UserRegistrationObject userJson) {
		// parse Json to User entity.
		UserEntity user = UserEntity.registerUser(userJson);

		// save to DB
		UserEntity userEntity = userRepository.save(user);
		return userEntity;
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
				userEntity = generateResetPasswordToken(userEntity);
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
		userEntity = generateResetPasswordToken(userEntity);
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
	private UserEntity generateResetPasswordToken(UserEntity userEntity) {
		String generatedToken = generateResetPasswordToken();
		userEntity.setResetPasswordToken(generatedToken);
		userEntity.setResetPasswordSentAt(LocalDateTime.now());
		return userRepository.saveAndFlush(userEntity);
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
					UserApiResponse.createStatusApiResponse(Collections.singletonList(ResponseStatus.INVALID_TOKEN)),
					HttpStatus.NOT_ACCEPTABLE);
		}

		return UserApiResponse.createStatusApiResponse(userEntity.getId(), null);
	}

	private void validateNewPassword(String newPassword) {
		if (StringUtils.isBlankOrNull(newPassword) || newPassword.length() > EntityConstants.PASSWORD_MAX_LENGTH
				|| newPassword.length() < EntityConstants.PASSWORD_MIN_LENGTH) {
			throw new EntityValidationException("INVALID_PASSWORD  ",
					UserApiResponse.createStatusApiResponse(Collections.singletonList(ResponseStatus.INVALID_PASSWORD)),
					HttpStatus.NOT_ACCEPTABLE);
		}
	}

	/**
	 * Ensure that ResetPasswordToken is not expired.
	 *
	 * @param userEntity user entity
	 */
	private void checkResetPasswordTokenExpiry(UserEntity userEntity) {
		LocalDateTime resetPasswordSentAt = userEntity.getResetPasswordSentAt();
		LocalDateTime tokenExpiryDate = resetPasswordSentAt.plusHours(EntityConstants.TOKEN_VALIDITY);
		if (LocalDateTime.now().isAfter(tokenExpiryDate)) {
			throw new EntityValidationException("EXPIRED_TOKEN  ",
					UserApiResponse.createStatusApiResponse(Collections.singletonList(ResponseStatus.EXPIRED_TOKEN)),
					HttpStatus.NOT_ACCEPTABLE);
		}
	}


	/**
	 * generate new AuthenticationToken and ensure that this AuthenticationToken is
	 * never used before.
	 *
	 * @param tokenLength length of generated AuthenticationToken
	 * @return unique generated AuthenticationToken.
	 */
	private String generateAuthenticationToken(int tokenLength) {
		String generatedToken = StringUtils.generateUUIDToken();
		boolean existsByToken = userRepository.existsByAuthenticationToken(generatedToken);
		if (existsByToken) {
			return reGenerateAuthenticationToken(tokenLength);
		}
		return generatedToken;
	}

	/**
	 * regenerate AuthenticationToken and if token already exists, make recursive
	 * call until generating new AuthenticationToken.
	 *
	 * @param tokenLength length of generated AuthenticationToken
	 * @return unique generated AuthenticationToken.
	 */
	private String reGenerateAuthenticationToken(int tokenLength) {
		String generatedToken = StringUtils.generateUUIDToken();
		boolean existsByToken = userRepository.existsByAuthenticationToken(generatedToken);
		if (existsByToken) {
			return reGenerateAuthenticationToken(tokenLength);
		}
		return generatedToken;
	}

	/**
	 * Get list of roles for User entity
	 *
	 * @return Role list
	 */
	private List<String> getUserRoles() {
		// for now, return default role which is Customer
		return Collections.singletonList(Roles.CUSTOMER.name());
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
		String orgPname = orgRepo.findOneById(user.getOrganizationId()).getPname();
		return redirectUser(securityService.login(user).getToken(), orgPname);
	}

	private RedirectView redirectUser(String token, String orgPname) {
		RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();
		attributes.addAttribute("token", token);

		RedirectView redirectView = new RedirectView();
		redirectView.setUrl(EntityConstants.PROTOCOL+"www."+EntityConstants.NASNAV_DOMAIN+
				"/"+orgPname+"/login");
		redirectView.setAttributesMap(attributes);

		return redirectView;
	}

	private void checkUserActivation(UserEntity user) throws BusinessException {
		if (user == null)
			throw new BusinessException("Provided token is invalid", "INVALID_PARAM: token", HttpStatus.UNAUTHORIZED);

		checkResetPasswordTokenExpiry(user);

		if (!checkUserDeactivated(user))
			throw new BusinessException("Account is already activated!", "INVALID_OPERATION: token", HttpStatus.NOT_ACCEPTABLE);
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
