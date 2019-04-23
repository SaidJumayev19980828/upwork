package com.nasnav.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.RoleRepository;
import com.nasnav.dto.UserDTOs;
import com.nasnav.enumerations.Roles;
import com.nasnav.exceptions.EntityValidationException;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.EntityUtils;
import com.nasnav.persistence.Role;
import com.nasnav.persistence.RoleEmployeeUser;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.UserApiResponse;
import com.nasnav.response.ApiResponseBuilder;
import com.nasnav.response.ResponseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.security.RolesAllowed;

@Service
public class EmployeeUserServiceImpl implements EmployeeUserService {

	private EmployeeUserRepository employeeUserRepository;
	private RoleRepository roleRepository;
	private PasswordEncoder passwordEncoder;
	private UserService userService;
	private RoleService roleService;

	@Autowired
	public EmployeeUserServiceImpl(EmployeeUserRepository userRepository, RoleRepository roleRepository,
			PasswordEncoder passwordEncoder, UserService userService, RoleService roleService) {
		this.employeeUserRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.userService = userService;
		this.roleService = roleService;
	}

	@Override
	public UserApiResponse createEmployeeUser(UserDTOs.EmployeeUserCreationObject employeeUserJson) {
		EntityUtils.validateNameAndEmail(employeeUserJson.name, employeeUserJson.email);
		// check if email already exists
		if (employeeUserRepository.getByEmail(employeeUserJson.email) == null) {
			String[] rolesList = employeeUserJson.role.split(",");
			// check if at least one of the roles can create a user
			if (roleCanCreateUser(rolesList)) {
				// parse Json to EmployeeUserEntity
				EmployeeUserEntity employeeUserEntity = employeeUserRepository
						.save(EmployeeUserEntity.createEmployeeUser(employeeUserJson));

				createRolesListFromRoleString(rolesList, employeeUserEntity.getId(), employeeUserJson.org_id);

				return UserApiResponse.createStatusApiResponse(Integer.toUnsignedLong(employeeUserEntity.getId()),
						Arrays.asList(ResponseStatus.NEED_ACTIVATION));
			}

			return EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS));
		}
		
		throw new EntityValidationException(ResponseStatus.EMAIL_EXISTS.name(),
				EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.EMAIL_EXISTS)),
				HttpStatus.NOT_ACCEPTABLE);
	}

	private void createRolesListFromRoleString(String[] rolesList, Integer employeeUserId, Integer org_id) {
		for (String role : rolesList) {
			try {
				Roles roleEnum = Roles.valueOf(role);

				Role roleEntity = new Role();
				roleEntity.setOrganizationId(org_id);
				roleEntity.setName(roleEnum.name());
				Role roleEntityRepo = roleRepository.save(roleEntity);
				Integer roleId = roleEntityRepo.getId();

				RoleEmployeeUser roleEmployeeUser = new RoleEmployeeUser();
				roleEmployeeUser.setRoleId(roleId);
				roleEmployeeUser.setEmployeeUserId(employeeUserId);
				// TODO : save roleEmployeeUser

			} catch (IllegalArgumentException ex) {
				throw new EntityValidationException(ResponseStatus.INVALID_ROLE.name(),
						EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INVALID_ROLE)),
						HttpStatus.NOT_ACCEPTABLE);
			}
		}
	}

	private boolean roleCanCreateUser(String[] rolesList) {
		for (String role : rolesList) {
			if (role.equals(Roles.NASNAV_ADMIN.name()) || role.equals(Roles.ORGANIZATION_ADMIN.name())
					|| role.equals(Roles.STORE_ADMIN.name())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public UserApiResponse login(UserDTOs.UserLoginObject body) {
		if (body.employee) {
			EmployeeUserEntity employeeUserEntity = this.employeeUserRepository.getByEmail(body.email);
			if(employeeUserEntity != null) {
				// check if account needs activation
				boolean accountNeedActivation = isEmployeeUserNeedActivation(employeeUserEntity);
				if (accountNeedActivation) {
					UserApiResponse failedLoginResponse = EntityUtils
							.createFailedLoginResponse(Collections.singletonList(ResponseStatus.NEED_ACTIVATION));
					throw new EntityValidationException("NEED_ACTIVATION ", failedLoginResponse, HttpStatus.LOCKED);
				}
				// ensure that password matched
				boolean passwordMatched = passwordEncoder.matches(body.password, employeeUserEntity.getEncryptedPassword());
				if (passwordMatched) {
					// check if account is locked
					if (isAccountLocked(employeeUserEntity)) { // TODO: so far there is no lockdown, so always false
																// //NOSONAR
						UserApiResponse failedLoginResponse = EntityUtils
								.createFailedLoginResponse(Collections.singletonList(ResponseStatus.ACCOUNT_SUSPENDED));
						throw new EntityValidationException("ACCOUNT_SUSPENDED ", failedLoginResponse, HttpStatus.LOCKED);
					}
					// generate new AuthenticationToken and perform post login updates
					updatePostLogin(employeeUserEntity);
					return createSuccessLoginResponse(employeeUserEntity);
				}
			}
			UserApiResponse failedLoginResponse = EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INVALID_CREDENTIALS));
	        throw new EntityValidationException("INVALID_CREDENTIALS ", failedLoginResponse, HttpStatus.UNAUTHORIZED);
		}
		// try to login using users table if employee_users does not contain current
		// login.
		return this.userService.login(body);
	}

	/**
	 * Generate new AuthenticationToken and perform post login updates.
	 *
	 * @param employeeUserEntity to be udpated
	 * @return employeeUserEntity
	 */
	private EmployeeUserEntity updatePostLogin(EmployeeUserEntity employeeUserEntity) {
		LocalDateTime currentSignInDate = employeeUserEntity.getCurrentSignInAt();
		employeeUserEntity.setLastSignInAt(currentSignInDate);
		employeeUserEntity.setCurrentSignInAt(LocalDateTime.now());
		employeeUserEntity.setAuthenticationToken(generateAuthenticationToken(EntityConstants.TOKEN_LENGTH));
		return employeeUserRepository.saveAndFlush(employeeUserEntity);
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
		boolean existsByToken = employeeUserRepository.existsByAuthenticationToken(generatedToken);
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
		boolean existsByToken = employeeUserRepository.existsByAuthenticationToken(generatedToken);
		if (existsByToken) {
			return reGenerateAuthenticationToken(tokenLength);
		}
		return generatedToken;
	}

	/**
	 * Check if passed employeeUser entity's account is locked.
	 *
	 * @param employeeUserEntity EemployeeUser entity to be checked.
	 * @return true if current EmployeeUser entity's account is locked.
	 */
	private boolean isAccountLocked(EmployeeUserEntity employeeUserEntity) {
		// TODO : change implementation later
		return false;
	}

	/**
	 * Check if passed EmployeeUser entity's account needs activation.
	 *
	 * @param employeeUserEntity EmployeeUser entity to be checked.
	 * @return true if current EmployeeUser entity's account needs activation.
	 */
	private boolean isEmployeeUserNeedActivation(EmployeeUserEntity employeeUserEntity) {
		String encryptedPassword = employeeUserEntity.getEncryptedPassword();
		return EntityUtils.isBlankOrNull(encryptedPassword)
				|| EntityConstants.INITIAL_PASSWORD.equals(encryptedPassword);
	}

	/**
	 * Create success login Api response
	 *
	 * @param employeeUserEntity success EmployeeUser entity
	 * @return UserApiResponse
	 */
	private UserApiResponse createSuccessLoginResponse(EmployeeUserEntity employeeUserEntity) {
		Integer organizationId = employeeUserEntity.getOrganizationId();
		Long shopId = employeeUserEntity.getShopId();
		return new ApiResponseBuilder().setSuccess(true).setEntityId(employeeUserEntity.getId().longValue())
				.setToken(employeeUserEntity.getAuthenticationToken())
				.setRoles(getEmployeeUserRoles(employeeUserEntity.getId()))
				.setOrganizationId(organizationId != null ? organizationId.longValue() : 0L)
				.setStoreId(shopId != null ? shopId : 0L).build();
	}

	/**
	 * Get list of roles for EmployeeUser entity
	 *
	 * @return Role list
	 */
	private List<String> getEmployeeUserRoles(Integer integer) {
		List<String> employessUserRoles = new ArrayList<>();
		List<Role> rolesOfEmployeeUser = this.roleService.getRolesOfEmployeeUser(integer);
		if (EntityUtils.isNotBlankOrNull(rolesOfEmployeeUser)) {
			rolesOfEmployeeUser.forEach(role -> {
				employessUserRoles.add(role.getName());
			});
		}
		return employessUserRoles;
	}
	
}
