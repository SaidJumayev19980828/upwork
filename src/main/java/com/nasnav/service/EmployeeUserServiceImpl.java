package com.nasnav.service;

import com.nasnav.AppConfig;
import com.nasnav.constatnts.EmailConstants;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserDTOs.PasswordResetObject;
import com.nasnav.exceptions.EntityValidationException;
import com.nasnav.persistence.DefaultBusinessEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.EntityUtils;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.helpers.EmployeeUserServiceHelper;
import com.nasnav.response.ResponseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class EmployeeUserServiceImpl implements EmployeeUserService {

	private EmployeeUserServiceHelper helper;

	private EmployeeUserRepository employeeUserRepository;
	private MailService mailService;
	private PasswordEncoder passwordEncoder;

	@Autowired
	public EmployeeUserServiceImpl(EmployeeUserServiceHelper helper, EmployeeUserRepository employeeUserRepository, MailService mailService, PasswordEncoder passwordEncoder) {
		this.helper = helper;
		this.employeeUserRepository = employeeUserRepository;
		this.mailService = mailService;
		this.passwordEncoder = passwordEncoder;
	}
	
	@Autowired
	AppConfig appConfig;

	@Override
	public UserApiResponse createEmployeeUser(Integer userId, String userToken, UserDTOs.EmployeeUserCreationObject employeeUserJson) {
		// check if user is authenticated
		if (!employeeUserRepository.existsByAuthenticationToken(userToken)) {
			throw new EntityValidationException("Insufficient Rights ",
					EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS)), HttpStatus.UNAUTHORIZED);
		}
		List<String> rolesList = Arrays.asList(employeeUserJson.role.split(","));
		helper.validateBusinessRules(employeeUserJson.name, employeeUserJson.email, employeeUserJson.org_id, rolesList);
		// get current logged in user
		EmployeeUserEntity currentUser = employeeUserRepository.getById(userId);
		List<String> nonOrgRolesList = Arrays.asList("NASNAV_ADMIN", "STORE_ADMIN", "STORE_MANAGER", "STORE_EMPLOYEE");
		List<String> nonStoreRolesList = Arrays.asList("NASNAV_ADMIN", "STORE_ADMIN", "STORE_MANAGER", "STORE_EMPLOYEE");
		// check if email and organization id already exists
		if (employeeUserRepository.getByEmailAndOrganizationId(employeeUserJson.email, employeeUserJson.org_id) == null) {
			int userType = helper.roleCanCreateUser(currentUser.getId());
			if (userType != -1) { // can add employees
				if (userType == 2) { // can add employees within the same organization
					if (!currentUser.getOrganizationId().equals(employeeUserJson.org_id)) { //not the same organization
						throw new EntityValidationException("Error Occurred during user creation:: " + ResponseStatus.INVALID_ORGANIZATION,
								EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INVALID_ORGANIZATION)), HttpStatus.NOT_ACCEPTABLE);
					}
					if (!Collections.disjoint(rolesList, nonOrgRolesList)) {
						throw new EntityValidationException("Insufficient Rights ",
								EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS)), HttpStatus.UNAUTHORIZED);
					}
				} else if (userType == 3) {
					if (!currentUser.getShopId().equals(employeeUserJson.store_id)){ //not the same Store
						throw new EntityValidationException("Error Occurred during user creation:: " + ResponseStatus.INVALID_STORE,
								EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INVALID_STORE)), HttpStatus.NOT_ACCEPTABLE);
					}
					if (!Collections.disjoint(rolesList, nonStoreRolesList)) {
						throw new EntityValidationException("Insufficient Rights ",
								EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS)), HttpStatus.UNAUTHORIZED);
					}
				}
				// parse Json to EmployeeUserEntity
				EmployeeUserEntity employeeUserEntity = helper.createEmployeeUser(employeeUserJson);
				// create Role and RoleEmployeeUser entities from the roles array
				helper.createRoles(rolesList, employeeUserEntity.getId(), employeeUserJson.org_id);
				return UserApiResponse.createStatusApiResponse(Integer.toUnsignedLong(employeeUserEntity.getId()),
						Arrays.asList(ResponseStatus.NEED_ACTIVATION));

			}
			throw new EntityValidationException("Insufficient Rights ",
					EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS)), HttpStatus.UNAUTHORIZED);
		}
		throw new EntityValidationException(ResponseStatus.EMAIL_EXISTS.name(),
				EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.EMAIL_EXISTS)),
				HttpStatus.NOT_ACCEPTABLE);
	}

	@Override
	public UserApiResponse login(UserDTOs.UserLoginObject body) {
		EmployeeUserEntity employeeUserEntity = this.employeeUserRepository.getByEmailAndOrganizationId(body.email, body.org_id);
		if (employeeUserEntity != null) {
			// check if account needs activation
			boolean accountNeedActivation = helper.isEmployeeUserNeedActivation(employeeUserEntity);
			if (accountNeedActivation) {
				UserApiResponse failedLoginResponse = EntityUtils
						.createFailedLoginResponse(Collections.singletonList(ResponseStatus.NEED_ACTIVATION));
				throw new EntityValidationException("NEED_ACTIVATION ", failedLoginResponse, HttpStatus.LOCKED);
			}
			// ensure that password matched
			boolean passwordMatched = passwordEncoder.matches(body.password, employeeUserEntity.getEncryptedPassword());
			if (passwordMatched) {
				// check if account is locked
				if (helper.isAccountLocked(employeeUserEntity)) { // TODO: so far there is no lockdown, so always
																	// false
					// //NOSONAR
					UserApiResponse failedLoginResponse = EntityUtils
							.createFailedLoginResponse(Collections.singletonList(ResponseStatus.ACCOUNT_SUSPENDED));
					throw new EntityValidationException("ACCOUNT_SUSPENDED ", failedLoginResponse, HttpStatus.LOCKED);
				}
				// generate new AuthenticationToken and perform post login updates
				helper.updatePostLogin(employeeUserEntity);
				return helper.createSuccessLoginResponse(employeeUserEntity);
			}
		}
		UserApiResponse failedLoginResponse = EntityUtils
				.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INVALID_CREDENTIALS));
		throw new EntityValidationException("INVALID_CREDENTIALS ", failedLoginResponse, HttpStatus.UNAUTHORIZED);
	}

	@Override
	public void deleteUser(Long userId) {
		employeeUserRepository.deleteById(userId.intValue());
	}

	@Override
	public DefaultBusinessEntity<?> findUserById(Long userId) {

		return employeeUserRepository.findById(userId.intValue()).orElse(null);
	}

	@Override
	public DefaultBusinessEntity<?> getUserById(Long userId) {
		return employeeUserRepository.findById(userId.intValue()).orElse(null);
	}

	@Override
	public DefaultBusinessEntity<?> update(DefaultBusinessEntity<?> employeeUserEntity) {
		return employeeUserRepository.saveAndFlush((EmployeeUserEntity) employeeUserEntity);
	}

	@Override
	public UserApiResponse sendEmailRecovery(String email, Long orgId) {
		EmployeeUserEntity employeeUserEntity = getEmployeeUserByEmail(email, orgId);
		employeeUserEntity = generateResetPasswordToken(employeeUserEntity);
		return sendRecoveryMail(employeeUserEntity);
	}

	@Override
	public UserApiResponse recoverUser(PasswordResetObject body) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkAuthToken(long userId, String authToken) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Get employee user by passed email
	 *
	 * @param email user entity email
	 * @return employee user entity
	 */
	private EmployeeUserEntity getEmployeeUserByEmail(String email, Long orgId) {
		// first ensure that email is valid
		if (!EntityUtils.validateEmail(email)) {
			UserApiResponse userApiResponse = UserApiResponse.createMessagesApiResponse(false,
					Collections.singletonList(ResponseStatus.INVALID_EMAIL));
			throw new EntityValidationException("INVALID_EMAIL :" + email, userApiResponse, HttpStatus.NOT_ACCEPTABLE);
		}
		// load user entity by email
		EmployeeUserEntity employeeUserEntity = this.employeeUserRepository.getByEmailAndOrganizationId(email, orgId);
		if (EntityUtils.isBlankOrNull(employeeUserEntity)) {
			UserApiResponse userApiResponse = UserApiResponse.createMessagesApiResponse(false,
					Collections.singletonList(ResponseStatus.EMAIL_NOT_EXIST));
			throw new EntityValidationException("EMAIL_NOT_EXIST", userApiResponse, HttpStatus.NOT_ACCEPTABLE);
		}
		return employeeUserEntity;
	}
	
	/**
	 * Generate ResetPasswordToken and assign it to passed user entity
	 *
	 * @param employeeUserEntity
	 * @return user entity after generating ResetPasswordToken and updating entity.
	 */
	private EmployeeUserEntity generateResetPasswordToken(EmployeeUserEntity employeeUserEntity) {
		String generatedToken = generateResetPasswordToken(EntityConstants.TOKEN_LENGTH);
		employeeUserEntity.setResetPasswordToken(generatedToken);
		employeeUserEntity.setResetPasswordSentAt(LocalDateTime.now());
		return employeeUserRepository.saveAndFlush(employeeUserEntity);
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
		boolean existsByToken = employeeUserRepository.existsByResetPasswordToken(generatedToken);
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
		boolean existsByToken = employeeUserRepository.existsByResetPasswordToken(generatedToken);
		if (existsByToken) {
			return reGenerateResetPasswordToken(tokenLength);
		}
		return generatedToken;
	}
	
	/**
	 * Send An Email to user.
	 *
	 * @param employeeUserEntity user entity
	 * @return UserApiResponse representing the status of sending email.
	 */
	private UserApiResponse sendRecoveryMail(EmployeeUserEntity employeeUserEntity) {
		UserApiResponse userApiResponse = new UserApiResponse();
		try {
			// create parameter map to replace parameter by actual UserEntity data.
			Map<String, String> parametersMap = new HashMap<>();
			parametersMap.put(EmailConstants.USERNAME_PARAMETER, employeeUserEntity.getName());
			parametersMap.put(EmailConstants.CHANGE_PASSWORD_URL_PARAMETER,
					appConfig.mailRecoveryUrl.concat(employeeUserEntity.getResetPasswordToken()));
			// send Recovery mail to user
			this.mailService.send(employeeUserEntity.getEmail(), EmailConstants.CHANGE_PASSWORD_EMAIL_SUBJECT,
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

}
