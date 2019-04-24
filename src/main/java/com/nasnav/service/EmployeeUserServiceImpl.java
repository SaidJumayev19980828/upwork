package com.nasnav.service;

import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dto.UserDTOs;
import com.nasnav.exceptions.EntityValidationException;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.EntityUtils;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.helpers.EmployeeUserServiceHelper;
import com.nasnav.response.ResponseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;

@Service
public class EmployeeUserServiceImpl implements EmployeeUserService {
	
	private EmployeeUserServiceHelper helper;

	private EmployeeUserRepository employeeUserRepository;
	private PasswordEncoder passwordEncoder;

	@Autowired
	public EmployeeUserServiceImpl(EmployeeUserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.employeeUserRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public UserApiResponse createEmployeeUser(UserDTOs.EmployeeUserCreationObject employeeUserJson) {
		EntityUtils.validateNameAndEmail(employeeUserJson.name, employeeUserJson.email);
		// check if email already exists
		if (employeeUserRepository.getByEmail(employeeUserJson.email) == null) {
			String[] rolesList = employeeUserJson.role.split(",");
			// check if at least one of the roles can create a user
			if (helper.roleCanCreateUser(rolesList)) {
				// parse Json to EmployeeUserEntity
				EmployeeUserEntity employeeUserEntity = helper.createEmployeeUser(employeeUserJson);

				// create Role and RoleEmployeeUser entities from the roles array
				helper.createRoles(rolesList, employeeUserEntity.getId(), employeeUserJson.org_id);

				return UserApiResponse.createStatusApiResponse(Integer.toUnsignedLong(employeeUserEntity.getId()),
						Arrays.asList(ResponseStatus.NEED_ACTIVATION));
			}
			return EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS));
		}
		throw new EntityValidationException(ResponseStatus.EMAIL_EXISTS.name(),
				EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.EMAIL_EXISTS)),
				HttpStatus.NOT_ACCEPTABLE);
	}

	@Override
	public UserApiResponse login(UserDTOs.UserLoginObject body) {
		EmployeeUserEntity employeeUserEntity = this.employeeUserRepository.getByEmail(body.email);
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
}
