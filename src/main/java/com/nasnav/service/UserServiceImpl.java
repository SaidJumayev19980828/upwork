package com.nasnav.service;

import com.nasnav.AppConfig;
import com.nasnav.constatnts.EmailConstants;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.UserDTOs;
import com.nasnav.enumerations.Roles;
import com.nasnav.exceptions.EntityValidationException;
import com.nasnav.persistence.DefaultBusinessEntity;
import com.nasnav.persistence.EntityUtils;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.UserApiResponse;
import com.nasnav.response.ApiResponseBuilder;
import com.nasnav.response.ResponseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

	private UserRepository userRepository;
	private MailService mailService;
	private PasswordEncoder passwordEncoder;

	@Autowired
	public UserServiceImpl(UserRepository userRepository, MailService mailService, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.mailService = mailService;
		this.passwordEncoder = passwordEncoder;
	}

	@Autowired
	AppConfig appConfig;

	@Override
	public UserApiResponse registerUser(UserDTOs.UserRegistrationObject userJson) {
		// validate user entity against business rules.
		this.validateBusinessRules(userJson);
		// check if a user with the same email and org_Id already exists
		if (userRepository.existsByEmailAndOrgId(userJson.email, userJson.org_id) == null) {
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

	/**
	 * validateBusinessRules passed user entity against business rules
	 *
	 * @param userJson User entity to be validated
	 */
	private void validateBusinessRules(UserDTOs.UserRegistrationObject userJson) {
		EntityUtils.validateNameAndEmail(userJson.name, userJson.email, userJson.org_id);
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
	public DefaultBusinessEntity<?> update(DefaultBusinessEntity<?> userEntity) {
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
		if (!EntityUtils.validateEmail(email)) {
			UserApiResponse userApiResponse = UserApiResponse.createMessagesApiResponse(false,
					Collections.singletonList(ResponseStatus.INVALID_EMAIL));
			throw new EntityValidationException("INVALID_EMAIL :" + email, userApiResponse, HttpStatus.NOT_ACCEPTABLE);
		}
		// load user entity by email
		UserEntity userEntity = this.userRepository.getByEmailAndOrganizationId(email, orgId);
		if (EntityUtils.isBlankOrNull(userEntity)) {
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
		String generatedToken = generateResetPasswordToken(EntityConstants.TOKEN_LENGTH);
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
	 * @param tokenLength length of generated ResetPasswordToken
	 * @return unique generated ResetPasswordToken.
	 */
	private String generateResetPasswordToken(int tokenLength) {
		String generatedToken = EntityUtils.generateToken(tokenLength);
		boolean existsByToken = userRepository.existsByResetPasswordToken(generatedToken);
		if (existsByToken) {
			return reGenerateResetPasswordToken(tokenLength);
		}
		return generatedToken;
	}

	/**
	 * regenerate ResetPasswordToken and if token already exists, make recursive
	 * call until generating new ResetPasswordToken.
	 *
	 * @param tokenLength length of generated ResetPasswordToken
	 * @return unique generated ResetPasswordToken.
	 */
	private String reGenerateResetPasswordToken(int tokenLength) {
		String generatedToken = EntityUtils.generateToken(tokenLength);
		boolean existsByToken = userRepository.existsByResetPasswordToken(generatedToken);
		if (existsByToken) {
			return reGenerateResetPasswordToken(tokenLength);
		}
		return generatedToken;
	}

	@Override
	public UserApiResponse recoverUser(UserDTOs.PasswordResetObject data) {
		validateNewPassword(data.password);
		UserEntity userEntity = userRepository.getByResetPasswordToken(data.token);
		if (EntityUtils.isNotBlankOrNull(userEntity)) {
			// if resetPasswordToken is not active, throw exception for invalid
			// resetPasswordToken
			checkResetPasswordTokenExpiry(userEntity);
			userEntity.setResetPasswordToken(null);
			userEntity.setResetPasswordSentAt(null);
			userEntity.setEncPassword(passwordEncoder.encode(data.password));
			userRepository.saveAndFlush(userEntity);
		} else {
			throw new EntityValidationException("INVALID_TOKEN  ",
					UserApiResponse.createStatusApiResponse(Collections.singletonList(ResponseStatus.INVALID_TOKEN)),
					HttpStatus.NOT_ACCEPTABLE);
		}

		return UserApiResponse.createStatusApiResponse(userEntity.getId(), null);
	}

	private void validateNewPassword(String newPassword) {
		if (EntityUtils.isBlankOrNull(newPassword) || newPassword.length() > EntityConstants.PASSWORD_MAX_LENGTH
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

	@Override
	public UserApiResponse login(UserDTOs.UserLoginObject loginData) {
		UserEntity userEntity = this.userRepository.getByEmailAndOrganizationId(loginData.email, loginData.org_id);

		if (userEntity != null) {
			// check if account needs activation
			boolean accountNeedActivation = isUserNeedActivation(userEntity);
			if (accountNeedActivation) {
				UserApiResponse failedLoginResponse = EntityUtils
						.createFailedLoginResponse(Collections.singletonList(ResponseStatus.NEED_ACTIVATION));
				throw new EntityValidationException("NEED_ACTIVATION ", failedLoginResponse, HttpStatus.LOCKED);
			}
			// ensure that password matched
			boolean passwordMatched = passwordEncoder.matches(loginData.password, userEntity.getEncPassword());

			if (passwordMatched) {
				// check if account is locked
				if (isAccountLocked(userEntity)) { // NOSONAR
					UserApiResponse failedLoginResponse = EntityUtils
							.createFailedLoginResponse(Collections.singletonList(ResponseStatus.ACCOUNT_SUSPENDED));
					throw new EntityValidationException("ACCOUNT_SUSPENDED ", failedLoginResponse, HttpStatus.LOCKED);
				}
				// generate new AuthenticationToken and perform post login updates
				userEntity = updatePostLogin(userEntity);
				return createSuccessLoginResponse(userEntity);
			}
		}
		UserApiResponse failedLoginResponse = EntityUtils
				.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INVALID_CREDENTIALS));
		throw new EntityValidationException("INVALID_CREDENTIALS ", failedLoginResponse, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Generate new AuthenticationToken and perform post login updates.
	 *
	 * @param userEntity to be udpated
	 * @return userEntity
	 */
	private UserEntity updatePostLogin(UserEntity userEntity) {
		LocalDateTime currentSignInDate = userEntity.getCurrentSignInDate();
		userEntity.setLastSignInDate(currentSignInDate);
		userEntity.setCurrentSignInDate(LocalDateTime.now());
		userEntity.setAuthenticationToken(generateAuthenticationToken(EntityConstants.TOKEN_LENGTH));
		return userRepository.saveAndFlush(userEntity);
	}

	/**
	 * generate new AuthenticationToken and ensure that this AuthenticationToken is
	 * never used before.
	 *
	 * @param tokenLength length of generated AuthenticationToken
	 * @return unique generated AuthenticationToken.
	 */
	private String generateAuthenticationToken(int tokenLength) {
		String generatedToken = EntityUtils.generateToken(tokenLength);
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
		String generatedToken = EntityUtils.generateToken(tokenLength);
		boolean existsByToken = userRepository.existsByAuthenticationToken(generatedToken);
		if (existsByToken) {
			return reGenerateAuthenticationToken(tokenLength);
		}
		return generatedToken;
	}

	/**
	 * Check if passed user entity's account is locked.
	 *
	 * @param userEntity User entity to be checked.
	 * @return true if current user entity's account is locked.
	 */
	private boolean isAccountLocked(UserEntity userEntity) {
		// TODO : change implementation later
		return false;
	}

	/**
	 * Check if passed user entity's account needs activation.
	 *
	 * @param userEntity User entity to be checked.
	 * @return true if current user entity's account needs activation.
	 */
	private boolean isUserNeedActivation(UserEntity userEntity) {
		String encPassword = userEntity.getEncPassword();
		return EntityUtils.isBlankOrNull(encPassword) || EntityConstants.INITIAL_PASSWORD.equals(encPassword);
	}

	/**
	 * Create success login Api response
	 *
	 * @param userEntity success user entity
	 * @return UserApiResponse
	 */
	private UserApiResponse createSuccessLoginResponse(UserEntity userEntity) {
		return new ApiResponseBuilder().setSuccess(true).setEntityId(userEntity.getId())
				.setToken(userEntity.getAuthenticationToken()).setRoles(getUserRoles()).setOrganizationId(0L)
				.setStoreId(0L).build();
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

	public boolean checkAuthToken(long userId, String authToken) {
		return userRepository.existsByIdAndAuthenticationToken(userId, authToken);
	}

}
