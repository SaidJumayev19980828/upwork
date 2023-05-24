package com.nasnav.service.helpers;

import com.nasnav.AppConfig;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.constatnts.EmailConstants;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dao.*;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserDTOs.EmployeeUserCreationObject;
import com.nasnav.enumerations.Roles;
import com.nasnav.enumerations.UserStatus;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.MailService;
import com.nasnav.service.RoleService;


import lombok.RequiredArgsConstructor;


import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.commons.utils.StringUtils.*;
import static com.nasnav.constatnts.EntityConstants.TOKEN_VALIDITY;
import static com.nasnav.enumerations.Roles.NASNAV_ADMIN;
import static com.nasnav.enumerations.Roles.ORGANIZATION_ADMIN;
import static com.nasnav.enumerations.UserStatus.*;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
@RequiredArgsConstructor
public class UserServicesHelper {

	private final EmployeeUserRepository employeeUserRepository;
	private final RoleRepository roleRepository;
	private final RoleEmployeeUserRepository roleEmployeeUserRepository;
	private final RoleService roleService;
	private final MailService mailService;
	private final OrganizationRepository organizationRepo;
	private final AppConfig appConfig;

	private final ShopsRepository shopRepo;


	public void createRoles(List<String> rolesList, EmployeeUserEntity employee, Long orgId) {
		List<String> existingRolesListNames = roleRepository
				.findAll()
				.stream()
				.map(Role::getName)
				.collect(toList());
		Role role;
		Roles roleEnum;
		roleEmployeeUserRepository.deleteByEmployee_Id(employee.getId()); //delete all existing rolesemployeeuser relations
		for (String r : rolesList) {
			// check if role exists in db
			if (!existingRolesListNames.contains(r)) {
				// find the Role enum from the string value
				roleEnum = Roles.valueOf(r);
				role = createRole(roleEnum);
			} else {
				role = roleRepository.findByName(r);
			}
			createRoleEmployeeUser(employee, role);
		}
	}



	private void createRoleEmployeeUser(EmployeeUserEntity employee, Role role) {
		RoleEmployeeUser roleEmployeeUser = new RoleEmployeeUser();
		roleEmployeeUser.setRole(role);
		roleEmployeeUser.setEmployee(employee);
		roleEmployeeUserRepository.save(roleEmployeeUser);
	}


	private Role createRole( Roles roleEnum) {
		Role role = new Role();
		role.setName(roleEnum.name());

		return roleRepository.save(role);
	}



	public void isValidRolesList(List<String> rolesList){
		if(rolesList.isEmpty()){
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$EMP$0001);
		}
		for (String role : rolesList) {
			try {
				Roles.valueOf(role);
			} catch (IllegalArgumentException ex) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$EMP$0007, role);
			}
		}
	}




	public boolean roleCannotManageUsers(Long currentUserId) {
		List<Role> rolesEntity = roleRepository.getRolesOfEmployeeUser(currentUserId);
		return rolesEntity
				.stream()
				.map( role -> Roles.valueOf(role.getName()))
				.noneMatch(Roles::isCanCreateUsers);
	}



	public boolean hasInsufficientLevel(Long currentUserId, List<String> otherUserRolesNames) {
		Set<Roles> currentUserRoles = getUserRoles(currentUserId);
		List<Roles> newUserRoles =
				otherUserRolesNames
				.stream()
				.map(Roles::valueOf)
				.collect(toList());
		return newUserRoles
				.stream()
				.anyMatch(newUserRole -> isHigherThanAllGivenRoles(newUserRole, currentUserRoles));
	}



	public boolean hasMaxRoleLevelOf(Roles role, Long currentUserId){
		Set<Roles> currentUserRoles = getUserRoles(currentUserId);
		return hasMaxRoleLevelOf(role, currentUserRoles);
	}



	public boolean hasMaxRoleLevelOf(Roles role, Set<Roles> userRoles) {
		boolean hasNoRolesWithHigherLevel =
				userRoles
				.stream()
				.noneMatch(currentUserRole -> currentUserRole.getLevel() < role.getLevel());
		boolean hasRole = userRoles.contains(role);
		return hasRole && hasNoRolesWithHigherLevel;
	}


	private Set<Roles> getUserRoles(Long currentUserId) {
		return roleRepository
				.getRolesOfEmployeeUser(currentUserId)
				.stream()
				.map(role -> Roles.valueOf(role.getName()))
				.collect(toSet());
	}


	private boolean isHigherThanAllGivenRoles(Roles role, Collection<Roles> otherRoles){
		//lower level number gets higher privilege, nasnav has max privilege with
		//negative level
		return otherRoles
				.stream()
				.allMatch(otherRole -> role.getLevel() < otherRole.getLevel());
	}



	public EmployeeUserEntity createEmployeeUser(EmployeeUserCreationObject employeeUserJson) {
		EmployeeUserEntity employeeUser = new EmployeeUserEntity();
		employeeUser.setName(employeeUserJson.name);
		employeeUser.setEmail(employeeUserJson.email.toLowerCase());
		employeeUser.setEncryptedPassword(EntityConstants.INITIAL_PASSWORD);
		employeeUser.setOrganizationId(employeeUserJson.orgId);
		employeeUser.setShopId(employeeUserJson.storeId);
		employeeUser.setImage(employeeUserJson.getAvatar());
		employeeUser.setUserStatus(NOT_ACTIVATED.getValue());
		employeeUser = employeeUserRepository.save(employeeUser);

		return employeeUser;
	}


	public UserApiResponse updateEmployeeUser(Long currentUserId, EmployeeUserEntity employeeUserEntity,
											  UserDTOs.EmployeeUserUpdatingObject employeeUserJson) {
		Set<Roles> currentUserRoles = getUserRoles(currentUserId);
		List<ResponseStatus> successResponseStatusList = new ArrayList<>();
		if (isNotBlankOrNull(employeeUserJson.getName())) {
			validateName(employeeUserJson.getName());
			employeeUserEntity.setName(employeeUserJson.getName());
		}
		if (isNotBlankOrNull(employeeUserJson.getOrgId()) && currentUserRoles.contains(NASNAV_ADMIN)) {
			validateOrgId(employeeUserJson.getOrgId());
			employeeUserEntity.setOrganizationId(employeeUserJson.getOrgId());
		}
		if (isStoreChangeApplicable(employeeUserJson, currentUserRoles)) {
			validateStoreIdUpdate(currentUserId, employeeUserJson);
			employeeUserEntity.setShopId(employeeUserJson.getStoreId());
		}
		if (isNotBlankOrNull(employeeUserJson.getEmail())) {
			validateEmail(employeeUserJson.getEmail());
			employeeUserEntity.setEmail(employeeUserJson.getEmail());
			employeeUserEntity = generateResetPasswordToken(employeeUserEntity);

			sendRecoveryMail(employeeUserEntity);
			successResponseStatusList.addAll( asList(ResponseStatus.NEED_ACTIVATION, ResponseStatus.ACTIVATION_SENT) );
		}

		employeeUserEntity = updateRemainingEmployeeUserInfo(employeeUserEntity,employeeUserJson);

		employeeUserEntity = employeeUserRepository.save(employeeUserEntity);


		if (successResponseStatusList.isEmpty())
			successResponseStatusList.add(ResponseStatus.ACTIVATED);

		return new UserApiResponse(employeeUserEntity.getId(), successResponseStatusList);
	}


	private void validateStoreIdUpdate(Long currentUserId, UserDTOs.EmployeeUserUpdatingObject employeeUserJson) {
		Long storeId = ofNullable(employeeUserJson.getStoreId()).orElse(-1L);
		if ( storeId < 0) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$EMP$0012, storeId);
		}
		Long currentUserOrg =
				employeeUserRepository
						.findById(currentUserId)
						.map(EmployeeUserEntity::getOrganizationId)
						.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, U$EMP$0002, currentUserId));
		boolean isValidStore =
				shopRepo.existsByIdAndOrganizationEntity_IdAndRemoved(storeId, currentUserOrg, 0);
		if(!isValidStore){
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$EMP$0012, employeeUserJson.getStoreId());
		}
	}


	private boolean isStoreChangeApplicable(UserDTOs.EmployeeUserUpdatingObject employeeUserJson, Set<Roles> currentUserRoles) {
		return isNotBlankOrNull(employeeUserJson.getStoreId())
				&& (currentUserRoles.contains(ORGANIZATION_ADMIN)
				|| currentUserRoles.contains(NASNAV_ADMIN));
	}


	private EmployeeUserEntity updateRemainingEmployeeUserInfo(EmployeeUserEntity employeeUserEntity, UserDTOs.EmployeeUserUpdatingObject employeeUserJson) {
		if (employeeUserJson.getAvatar() != null)
			employeeUserEntity.setImage(employeeUserJson.getAvatar());

		if (employeeUserJson.getPhoneNumber() != null)
			employeeUserEntity.setPhoneNumber(employeeUserJson.getPhoneNumber());

		return employeeUserEntity;
	}



	/**
	 * Generate new AuthenticationToken and perform post login updates.
	 *
	 * @param employeeUserEntity to be udpated
	 * @return employeeUserEntity
	 */
	public EmployeeUserEntity updatePostLogin(EmployeeUserEntity employeeUserEntity) {
		LocalDateTime currentSignInDate = employeeUserEntity.getCurrentSignInDate();
		employeeUserEntity.setLastSignInDate(currentSignInDate);
		employeeUserEntity.setCurrentSignInDate(LocalDateTime.now());
		employeeUserEntity.setAuthenticationToken(generateAuthenticationToken());
		return employeeUserRepository.saveAndFlush(employeeUserEntity);
	}




	private String generateAuthenticationToken() {
		String generatedToken = generateUUIDToken();
		boolean existsByToken = employeeUserRepository.existsByAuthenticationToken(generatedToken);
		if (existsByToken) {
			return reGenerateAuthenticationToken();
		}
		return generatedToken;
	}




	private String reGenerateAuthenticationToken() {
		String generatedToken = generateUUIDToken();
		boolean existsByToken = employeeUserRepository.existsByAuthenticationToken(generatedToken);
		if (existsByToken) {
			return reGenerateAuthenticationToken();
		}
		return generatedToken;
	}


	public List<String> getEmployeeUserRoles(Long userId) {
		return roleService
				.getRolesOfEmployeeUser(userId)
				.stream()
				.map(Role::getName)
				.collect(toList());
	}

	public void validateBusinessRules(String name, String email, Long orgId) {
		validateName(name);
		validateEmail(email);
		validateOrgId(orgId);
	}


	public EmployeeUserEntity generateResetPasswordToken(EmployeeUserEntity employeeUserEntity) {
		String generatedToken = generateResetPasswordToken();
		employeeUserEntity.setResetPasswordToken(generatedToken);
		employeeUserEntity.setResetPasswordSentAt(LocalDateTime.now());
		return employeeUserRepository.saveAndFlush(employeeUserEntity);
	}

	/**
	 * generate new ResetPasswordToken and ensure that this ResetPasswordToken is
	 * never used before.
	 *
	 * @return unique generated ResetPasswordToken.
	 */
	private String generateResetPasswordToken() {
		String generatedToken = generateUUIDToken();
		boolean existsByToken = employeeUserRepository.existsByResetPasswordToken(generatedToken);
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
		String generatedToken = generateUUIDToken();
		boolean existsByToken = employeeUserRepository.existsByResetPasswordToken(generatedToken);
		if (existsByToken) {
			return reGenerateResetPasswordToken();
		}
		return generatedToken;
	}

	/**
	 * Send An Email to user.
	 *
	 * @param employeeUserEntity user entity
	 * @return UserApiResponse representing the status of sending email.
	 */
	public void sendRecoveryMail(EmployeeUserEntity employeeUserEntity) {
		try {
			// create parameter map to replace parameter by actual UserEntity data.
			String empName = ofNullable(employeeUserEntity.getName()).orElse("New User");
			String orgName = organizationRepo.findById(employeeUserEntity.getOrganizationId())
					.map(OrganizationEntity::getName)
					.orElse("Nasnav");
			Map<String, String> parametersMap = new HashMap<>();
			parametersMap.put(EmailConstants.USERNAME_PARAMETER, empName);
			parametersMap.put(EmailConstants.CHANGE_PASSWORD_URL_PARAMETER,
					appConfig.empMailRecoveryUrl.concat(employeeUserEntity.getResetPasswordToken()));
			// send Recovery mail to user
			this.mailService.send(orgName, employeeUserEntity.getEmail(), EmailConstants.CHANGE_PASSWORD_EMAIL_SUBJECT,
					EmailConstants.CHANGE_PASSWORD_EMAIL_TEMPLATE, parametersMap);
		} catch (Exception e) {
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, GEN$0003, e.getMessage());
		}
	}


	public void validateName(String name) {
		if (!StringUtils.validateName(name)) {
			throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, U$EMP$0003, name );
		}
	}

	public void validateEmail(String email) {
		if (!StringUtils.validateEmail(email)) {
			throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, U$EMP$0004, email);
		}
	}

	public void validateOrgId(Long orgId) {
		if (StringUtils.isBlankOrNull(orgId) || orgId <= 0){
			throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, U$EMP$0005, orgId );
		}
	}

	public void validateNewPassword(String newPassword) {
		if (isBlankOrNull(newPassword) || newPassword.length() > EntityConstants.PASSWORD_MAX_LENGTH
				|| newPassword.length() < EntityConstants.PASSWORD_MIN_LENGTH) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$LOG$0005);
		}
	}

	public void validateToken(String token) {
		if(isBlankOrNull(token)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, UXACTVX0005);
		}
	}


	public void checkResetPasswordTokenExpiry(LocalDateTime resetPasswordSentAt) {
		LocalDateTime tokenExpiryDate = resetPasswordSentAt.plusHours(TOKEN_VALIDITY);
		if (now().isAfter(tokenExpiryDate)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$LOG$0006);
		}
	}

	public UserStatus checkUserStatusForSuspension(BaseUserEntity user) {
		UserStatus status = ofNullable(UserStatus.getUserStatus(user.getUserStatus()))
				.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, U$STATUS$0004));
		if(!setOf(ACTIVATED, ACCOUNT_SUSPENDED).contains(status)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$STATUS$0003);
		}
		return status;
	}
}
