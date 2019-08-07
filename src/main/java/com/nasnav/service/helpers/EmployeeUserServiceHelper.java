package com.nasnav.service.helpers;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.nasnav.AppConfig;
import com.nasnav.constatnts.EmailConstants;
import com.nasnav.dto.UserDTOs;
import com.nasnav.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.RoleEmployeeUserRepository;
import com.nasnav.dao.RoleRepository;
import com.nasnav.dto.UserDTOs.EmployeeUserCreationObject;
import com.nasnav.enumerations.Roles;
import com.nasnav.exceptions.EntityValidationException;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.EntityUtils;
import com.nasnav.persistence.Role;
import com.nasnav.persistence.RoleEmployeeUser;
import com.nasnav.response.ApiResponseBuilder;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.RoleService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeUserServiceHelper {

	private EmployeeUserRepository employeeUserRepository;
	private RoleRepository roleRepository;
	private RoleEmployeeUserRepository roleEmployeeUserRepository;
	private RoleService roleService;
	private MailService mailService;
	private List<String> nonStoreRolesList = Arrays.asList("NASNAV_ADMIN", "ORGANIZATION_ADMIN", "ORGANIZATION_MANAGER", "ORGANIZATION_EMPLOYEE");
	private List<String> nonOrgRolesList = Arrays.asList("NASNAV_ADMIN", "STORE_ADMIN", "STORE_MANAGER", "STORE_EMPLOYEE");

	@Autowired
	public EmployeeUserServiceHelper(EmployeeUserRepository userRepository, RoleRepository roleRepository,
			RoleEmployeeUserRepository roleEmployeeUserRepository, RoleService roleService, MailService mailService) {
		this.employeeUserRepository = userRepository;
		this.roleRepository = roleRepository;
		this.roleEmployeeUserRepository = roleEmployeeUserRepository;
		this.roleService = roleService;
		this.mailService = mailService;
	}

	@Autowired
	AppConfig appConfig;

	public void createRoles(List<String> rolesList, Integer employeeUserId, Long org_id) {
		List<Role> existingRoles = roleRepository.findAll();
		List<String> existingRolesListNames = existingRoles.stream().map( role -> role.getName()).collect(Collectors.toList());
		Integer roleId;
		Roles roleEnum;
		roleEmployeeUserRepository.deleteByEmployeeUserId(employeeUserId); //delete all existing rolesemployeeuser relations
		for (String role : rolesList) {
			// check if role exists in db
			if (!existingRolesListNames.contains(role)) {
				// find the Role enum from the string value
				roleEnum = Roles.valueOf(role);
				roleId = createRole(org_id, roleEnum);
			} else {
				roleId = roleRepository.findByName(role).getId();
			}
			createRoleEmployeeUser(employeeUserId, roleId);
		}
	}

	private void createRoleEmployeeUser(Integer employeeUserId, Integer roleId) {
		RoleEmployeeUser roleEmployeeUser = new RoleEmployeeUser();
		roleEmployeeUser.setRoleId(roleId);
		roleEmployeeUser.setEmployeeUserId(employeeUserId);
		//TODO: check if these fields will be removed from the DB or not
		roleEmployeeUser.setCreatedAt(LocalDateTime.now());
		roleEmployeeUser.setUpdatedAt(LocalDateTime.now());
		roleEmployeeUserRepository.save(roleEmployeeUser);
	}

	// create a Role entity and return its id
	private Integer createRole(Long org_id, Roles roleEnum) {
		// create a role entity
		Role role = new Role();
		role.setOrganizationId(org_id);
		role.setName(roleEnum.name());
		//TODO: check if these fields will be removed from the DB or not
		role.setCreatedAt(LocalDateTime.now());
		role.setUpdatedAt(LocalDateTime.now());
		Role roleEntity = roleRepository.save(role);
		// get the create role id to create a RoleEmployeeUser entity using it
		Integer roleId = roleEntity.getId();
		return roleId;
	}

	public boolean isValidRolesList(List<String> rolesList){
		for (String role : rolesList) {
			Roles roleEnum;
			try {
				roleEnum = Roles.valueOf(role);
			} catch (IllegalArgumentException ex) {
				throw new EntityValidationException(ResponseStatus.INVALID_ROLE.name(),
						EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INVALID_ROLE)),
						HttpStatus.NOT_ACCEPTABLE);
			}
		}
		return true;
	}

	// check if the current list of roles has a role authorized to create users or
	// not
	public Integer roleCanCreateUser(Integer id) {
		// get list of roles belong to current user
		List<Role> rolesList = roleRepository.getRolesOfEmployeeUser(id);
		List<Roles> rolesListNames = rolesList.stream().map( role -> Roles.valueOf(role.getName())).collect(Collectors.toList());
		if (rolesListNames.contains(Roles.NASNAV_ADMIN)) {
			return 1;
		} else if (rolesListNames.contains(Roles.ORGANIZATION_ADMIN)) {
			return 2;
		} else if (rolesListNames.contains(Roles.STORE_ADMIN)) {
			return 3;
		}
		return -1;
	}

	// check if the current roles is an organization admin role
	public boolean hasOrganizationRole(String[] rolesList) {
		return Arrays.stream(rolesList).anyMatch(Roles.ORGANIZATION_ADMIN.getValue()::equals);
	}

	// check if the current roles is a store admin role
	public boolean hasStoreRole(String[] rolesList) {
		return Arrays.stream(rolesList).anyMatch(Roles.STORE_ADMIN.getValue()::equals);
	}

	public EmployeeUserEntity createEmployeeUser(EmployeeUserCreationObject employeeUserJson) {
		return employeeUserRepository.save(EmployeeUserEntity.createEmployeeUser(employeeUserJson));
	}

	public boolean checkOrganizationRolesRights(List<String> roles) {
		return !Collections.disjoint(roles, nonOrgRolesList);
	}

	public boolean checkStoreRolesRights(List<String> roles) {
		return !Collections.disjoint(roles, nonStoreRolesList);
	}

	public UserApiResponse updateEmployeeUser(Integer userType, EmployeeUserEntity employeeUserEntity, UserDTOs.EmployeeUserUpdatingObject employeeUserJson) {
		List<ResponseStatus> failResponseStatusList = new ArrayList<>();
		List<ResponseStatus> successResponseStatusList = new ArrayList<>();
		List<String> rolesList;
		if (EntityUtils.isNotBlankOrNull(employeeUserJson.getName())) {
			if (EntityUtils.validateName(employeeUserJson.getName())) {
				employeeUserEntity.setName(employeeUserJson.getName());
			} else {
				failResponseStatusList.add(ResponseStatus.INVALID_NAME);
			}
		}
		if (EntityUtils.isNotBlankOrNull(employeeUserJson.getOrgId()) && userType == 1) {
			if (employeeUserJson.getOrgId() >= 0) {
				employeeUserEntity.setOrganizationId(employeeUserJson.getOrgId());
			} else {
				failResponseStatusList.add(ResponseStatus.INVALID_ORGANIZATION);
			}
		}
		if (EntityUtils.isNotBlankOrNull(employeeUserJson.getStoreId()) && (userType == 1 || userType == 2)) {
			if (employeeUserJson.getStoreId() >= 0) {
				employeeUserEntity.setShopId(employeeUserJson.getStoreId());
			} else {
				failResponseStatusList.add(ResponseStatus.INVALID_STORE);
			}
		}
		if (EntityUtils.isNotBlankOrNull(employeeUserJson.getRole())){
			rolesList = Arrays.asList(employeeUserJson.getRole().split(","));
			// check if can update employees roles
			if (userType != -1) { // can update employees roles
				if (userType == 2) { // can update employees roles within the same organization
					if (checkOrganizationRolesRights(rolesList)) { // check roles list to update
						failResponseStatusList.add(ResponseStatus.INSUFFICIENT_RIGHTS);
					}
				} else if (userType == 3) { // can update employees roles within the same store
					if (checkStoreRolesRights(rolesList)) { // check roles list to update
						failResponseStatusList.add(ResponseStatus.INSUFFICIENT_RIGHTS);
					}
				}
				if (!failResponseStatusList.contains(ResponseStatus.INSUFFICIENT_RIGHTS)) {
					createRoles(rolesList, employeeUserEntity.getId(), employeeUserJson.getOrgId());
				}
			}
		}
		if (EntityUtils.isNotBlankOrNull(employeeUserJson.getEmail())) {
			if (EntityUtils.validateEmail(employeeUserJson.getEmail())) {
				employeeUserEntity.setEmail(employeeUserJson.getEmail());
				if ((employeeUserJson.getUpdatedUserId() == null) || employeeUserJson.getUpdatedUserId().intValue() == employeeUserEntity.getId()) {
					employeeUserEntity = generateResetPasswordToken(employeeUserEntity);
					sendRecoveryMail(employeeUserEntity);
					successResponseStatusList.add(ResponseStatus.NEED_ACTIVATION);
					successResponseStatusList.add(ResponseStatus.ACTIVATION_SENT);
				}
			} else {
				failResponseStatusList.add(ResponseStatus.INVALID_EMAIL);
			}
		}
		if (!failResponseStatusList.isEmpty()) {
			throw new EntityValidationException("Invalid User Entity: " + failResponseStatusList,
					UserApiResponse.createStatusApiResponse(failResponseStatusList), HttpStatus.NOT_ACCEPTABLE);
		}
		employeeUserRepository.save(employeeUserEntity);
		if (successResponseStatusList.isEmpty()) {
			successResponseStatusList.add(ResponseStatus.ACTIVATED);
		}
		return  UserApiResponse.createMessagesApiResponse(true, successResponseStatusList);
	}

	/**
	 * Generate new AuthenticationToken and perform post login updates.
	 *
	 * @param employeeUserEntity to be udpated
	 * @return employeeUserEntity
	 */
	public EmployeeUserEntity updatePostLogin(EmployeeUserEntity employeeUserEntity) {
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
	public boolean isAccountLocked(EmployeeUserEntity employeeUserEntity) {
		// TODO : change implementation later
		return false;
	}

	/**
	 * Check if passed EmployeeUser entity's account needs activation.
	 *
	 * @param employeeUserEntity EmployeeUser entity to be checked.
	 * @return true if current EmployeeUser entity's account needs activation.
	 */
	public boolean isEmployeeUserNeedActivation(EmployeeUserEntity employeeUserEntity) {
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
	public UserApiResponse createSuccessLoginResponse(EmployeeUserEntity employeeUserEntity) {
		Long organizationId = employeeUserEntity.getOrganizationId();
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
	public List<String> getEmployeeUserRoles(Integer integer) {
		List<String> employeeUserRoles = new ArrayList<>();
		List<Role> rolesOfEmployeeUser = this.roleService.getRolesOfEmployeeUser(integer);
		if (EntityUtils.isNotBlankOrNull(rolesOfEmployeeUser)) {
			rolesOfEmployeeUser.forEach(role -> {
				employeeUserRoles.add(role.getName());
			});
		}
		return employeeUserRoles;
	}

	public void validateBusinessRules(String name, String email, Long orgId, List<String> rolesList) {
		EntityUtils.validateNameAndEmail(name, email, orgId);
		isValidRolesList(rolesList);
	}

	/**
	 * Generate ResetPasswordToken and assign it to passed user entity
	 *
	 * @param employeeUserEntity
	 * @return user entity after generating ResetPasswordToken and updating entity.
	 */
	public EmployeeUserEntity generateResetPasswordToken(EmployeeUserEntity employeeUserEntity) {
		String generatedToken = generateResetPasswordToken(EntityConstants.TOKEN_LENGTH);
		//employeeUserEntity.setEncryptedPassword("");
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
	public UserApiResponse sendRecoveryMail(EmployeeUserEntity employeeUserEntity) {
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
