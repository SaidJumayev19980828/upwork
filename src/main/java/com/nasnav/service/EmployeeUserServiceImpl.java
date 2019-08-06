package com.nasnav.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nasnav.AppConfig;
import com.nasnav.constatnts.EmailConstants;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserDTOs.PasswordResetObject;
import com.nasnav.exceptions.EntityValidationException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.DefaultBusinessEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.EntityUtils;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.helpers.EmployeeUserServiceHelper;

@Service
public class EmployeeUserServiceImpl implements EmployeeUserService {

	private EmployeeUserServiceHelper helper;

	private EmployeeUserRepository employeeUserRepository;
	private MailService mailService;
	
	@Autowired
	public EmployeeUserServiceImpl(EmployeeUserServiceHelper helper, EmployeeUserRepository employeeUserRepository, MailService mailService, PasswordEncoder passwordEncoder) {
		this.helper = helper;
		this.employeeUserRepository = employeeUserRepository;
		this.mailService = mailService;
	}
	
	@Autowired
	AppConfig appConfig;

	@Override
	public UserApiResponse createEmployeeUser(Long userId, String userToken, UserDTOs.EmployeeUserCreationObject employeeUserJson) {
		
		List<String> rolesList = Arrays.asList(employeeUserJson.role.split(","));
		helper.validateBusinessRules(employeeUserJson.name, employeeUserJson.email, employeeUserJson.org_id, rolesList);
		// get current logged in user
		EmployeeUserEntity currentUser = employeeUserRepository.getById(userId);
		// check if email and organization id already exists
		if (employeeUserRepository.getByEmailAndOrganizationId(employeeUserJson.email, employeeUserJson.org_id) == null) {
			int userType = helper.roleCanCreateUser(currentUser.getId());
			if (userType != -1) { // can add employees
				if (userType == 2) { // can add employees within the same organization
					if (!currentUser.getOrganizationId().equals(employeeUserJson.org_id)) { //not the same organization
						throw new EntityValidationException("Error Occurred during user creation:: " + ResponseStatus.INSUFFICIENT_RIGHTS,
								EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS)), HttpStatus.NOT_ACCEPTABLE);
					}
					if (helper.checkOrganizationRolesRights(rolesList)) {
						throw new EntityValidationException("Insufficient Rights ",
								EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS)), HttpStatus.UNAUTHORIZED);
					}
				} else if (userType == 3) {
					if (!currentUser.getShopId().equals(employeeUserJson.store_id)){ //not the same Store
						throw new EntityValidationException("Error Occurred during user creation:: " + ResponseStatus.INSUFFICIENT_RIGHTS,
								EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS)), HttpStatus.NOT_ACCEPTABLE);
					}
					if (helper.checkStoreRolesRights(rolesList)) {
						throw new EntityValidationException("Insufficient Rights ",
								EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS)), HttpStatus.UNAUTHORIZED);
					}
				}
				// parse Json to EmployeeUserEntity
				EmployeeUserEntity employeeUserEntity = helper.createEmployeeUser(employeeUserJson);
				// create Role and RoleEmployeeUser entities from the roles array
				helper.createRoles(rolesList, employeeUserEntity.getId(), employeeUserJson.org_id);
				employeeUserEntity = generateResetPasswordToken(employeeUserEntity);
				sendRecoveryMail(employeeUserEntity);
				return UserApiResponse.createStatusApiResponse(employeeUserEntity.getId(),
						Arrays.asList(ResponseStatus.NEED_ACTIVATION, ResponseStatus.ACTIVATION_SENT));
			}
			throw new EntityValidationException("Insufficient Rights ",
					EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS)), HttpStatus.UNAUTHORIZED);
		}
		throw new EntityValidationException(ResponseStatus.EMAIL_EXISTS.name(),
				EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.EMAIL_EXISTS)),
				HttpStatus.NOT_ACCEPTABLE);
	}

	@Override
	public UserApiResponse updateEmployeeUser(Long userId, String userToken, UserDTOs.EmployeeUserUpdatingObject employeeUserJson) {
		EmployeeUserEntity updateUser,currentUser;
		
		int userType = helper.roleCanCreateUser(userId); //check user privileges
		if (userType == -1) { // can't update employees
			throw new EntityValidationException(""+ResponseStatus.INSUFFICIENT_RIGHTS,
					EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS)), HttpStatus.UNAUTHORIZED);
		}
		currentUser = employeeUserRepository.getById(userId);
		if (EntityUtils.isBlankOrNull(employeeUserJson.updated_user_id)) {// check if same user doing the update
			updateUser = employeeUserRepository.getById(userId);
		} else {
			updateUser = employeeUserRepository.getById(employeeUserJson.updated_user_id.longValue());
		}
		if ((userType == 2) && (!updateUser.getOrganizationId().equals(currentUser.getOrganizationId()))) {
			// can update employees within the same organization and they are not in the same organization
			throw new EntityValidationException("Not in the same Organization " + ResponseStatus.INSUFFICIENT_RIGHTS,
					EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS)), HttpStatus.NOT_ACCEPTABLE);
		} else if ((userType == 3) && (!updateUser.getShopId().equals(currentUser.getShopId()))) {
			// can update employees within the same Store and they are not in the same Store
			throw new EntityValidationException("Not in the same Store " + ResponseStatus.INSUFFICIENT_RIGHTS,
					EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS)), HttpStatus.NOT_ACCEPTABLE);
		}
		helper.updateEmployeeUser(userType, updateUser, employeeUserJson);
		return UserApiResponse.createMessagesApiResponse(true,
				Arrays.asList(ResponseStatus.ACTIVATED));
	}

	

	@Override
	public void deleteUser(Long userId) {
		employeeUserRepository.deleteById(userId);
	}

	@Override
	public BaseUserEntity findUserById(Long userId) {

		return employeeUserRepository.findById(userId).orElse(null);
	}

	@Override
	public BaseUserEntity getUserById(Long userId) {
		return employeeUserRepository.findById(userId).orElse(null);
	}

	@Override
	public BaseUserEntity update(BaseUserEntity employeeUserEntity) {
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
	public boolean checkAuthToken(Long userId, String authToken) {
		return employeeUserRepository.existsByIdAndAuthenticationToken(userId, authToken);
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
		String generatedToken = EntityUtils.generateUUIDToken();
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
		String generatedToken = EntityUtils.generateUUIDToken();
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
