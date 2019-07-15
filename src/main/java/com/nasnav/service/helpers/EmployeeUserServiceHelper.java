package com.nasnav.service.helpers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

	@Autowired
	public EmployeeUserServiceHelper(EmployeeUserRepository userRepository, RoleRepository roleRepository,
			RoleEmployeeUserRepository roleEmployeeUserRepository, RoleService roleService) {
		this.employeeUserRepository = userRepository;
		this.roleRepository = roleRepository;
		this.roleEmployeeUserRepository = roleEmployeeUserRepository;
		this.roleService = roleService;
	}

	public void createRoles(String[] rolesList, Integer employeeUserId, Long org_id) {
		for (String role : rolesList) {
			// find the Role enum from the string value
			Roles roleEnum = Roles.valueOf(role);
			Integer roleId = createRole(org_id, roleEnum);
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

	public boolean isValidRolesList(String[] rolesList){
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
	public boolean roleCanCreateUser(String[] rolesList) {
		for (String role : rolesList) {
			Roles roleEnum = Roles.valueOf(role);
			if (roleEnum == Roles.NASNAV_ADMIN || roleEnum == Roles.ORGANIZATION_ADMIN
					|| roleEnum == Roles.STORE_ADMIN) {
				return true;
			}
		}
		return false;
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
	private List<String> getEmployeeUserRoles(Integer integer) {
		List<String> employeeUserRoles = new ArrayList<>();
		List<Role> rolesOfEmployeeUser = this.roleService.getRolesOfEmployeeUser(integer);
		if (EntityUtils.isNotBlankOrNull(rolesOfEmployeeUser)) {
			rolesOfEmployeeUser.forEach(role -> {
				employeeUserRoles.add(role.getName());
			});
		}
		return employeeUserRoles;
	}

	public void validateBusinessRules(String name, String email, Long orgId, String[] rolesList) {
		EntityUtils.validateNameAndEmail(name, email, orgId);
		isValidRolesList(rolesList);
	}
}
