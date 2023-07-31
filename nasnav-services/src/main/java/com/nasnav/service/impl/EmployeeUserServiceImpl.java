package com.nasnav.service.impl;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Enums;
import com.nasnav.commons.criteria.AbstractCriteriaQueryBuilder;
import com.nasnav.commons.utils.CollectionUtils;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.UserTokenRepository;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserDTOs.PasswordResetObject;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.ActivateOtpDto;
import com.nasnav.enumerations.Roles;
import com.nasnav.enumerations.UserStatus;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.EmployeeUserOtpEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.UserTokensEntity;
import com.nasnav.request.UsersSearchParam;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.EmployeeUserService;
import com.nasnav.service.MailService;
import com.nasnav.service.RoleService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.helpers.UserServicesHelper;
import com.nasnav.service.otp.EmployeeOtpService;
import com.nasnav.service.otp.OtpType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

import static com.nasnav.commons.utils.StringUtils.generateUUIDToken;
import static com.nasnav.commons.utils.StringUtils.isNotBlankOrNull;
import static com.nasnav.constatnts.EmailConstants.ACTIVATION_ACCOUNT_EMAIL_SUBJECT;
import static com.nasnav.constatnts.EmailConstants.OTP_PARAMETER;
import static com.nasnav.constatnts.EmailConstants.OTP_TEMPLATE;
import static com.nasnav.enumerations.Roles.*;
import static com.nasnav.enumerations.UserStatus.*;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.response.ResponseStatus.ACTIVATION_SENT;
import static com.nasnav.response.ResponseStatus.NEED_ACTIVATION;
import static com.nasnav.service.otp.OtpType.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@Slf4j
public class EmployeeUserServiceImpl implements EmployeeUserService {

	private Logger logger = LogManager.getLogger();
	@Autowired
	private EmployeeUserRepository employeeUserRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private RoleService roleService;
	@Autowired
	private SecurityService securityService;
	@Autowired
	private UserServicesHelper empUserSvcHelper;
	@Autowired
	private ShopsRepository shopRepo;
	@Autowired
	private UserTokenRepository userTokenRepo;
	@Autowired
	private EmployeeOtpService employeeOtpService;
	@Autowired
	private OrganizationRepository organizationRepository;
	@Autowired
	private MailService mailService;

	@Autowired
	@Qualifier("userListQueryBuilder")
	private AbstractCriteriaQueryBuilder<EmployeeUserEntity> criteriaQueryBuilder;

	@Override
	@Transactional
	public UserApiResponse createEmployeeUser(UserDTOs.EmployeeUserCreationObject employeeUserJson) {
		List<String> rolesList = extractRoles(employeeUserJson);

		empUserSvcHelper.validateBusinessRules(employeeUserJson.name, employeeUserJson.email, employeeUserJson.orgId);
		empUserSvcHelper.isValidRolesList(rolesList);
		validateEmpEmailAlreadyExists(employeeUserJson);
		List<EmployeeUserEntity> orgEmployees = employeeUserRepository.findByOrganizationId(employeeUserJson.orgId);
		if(!orgEmployees.isEmpty()) {
			validateCurrentUserCanManageEmpAccount(employeeUserJson.orgId, employeeUserJson.storeId, rolesList);
		}
		validateStoreForEmployeeCreation(employeeUserJson, rolesList);

		EmployeeUserEntity employeeUserEntity = doCreateNewEmpAccount(employeeUserJson, rolesList);
		return new UserApiResponse(employeeUserEntity.getId(), asList(NEED_ACTIVATION, ACTIVATION_SENT));
	}


	@Override
	@Transactional
	public UserApiResponse createEmployeeUserWithPassword(UserDTOs.EmployeeUserWithPassword employeeUserWithPassword) {
		List<String> rolesList = extractRoles(employeeUserWithPassword);

		empUserSvcHelper.validateBusinessRules(employeeUserWithPassword.name, employeeUserWithPassword.email, employeeUserWithPassword.orgId);
		empUserSvcHelper.isValidRolesList(rolesList);
		validateEmpEmailAlreadyExists(employeeUserWithPassword);
		List<EmployeeUserEntity> orgEmployees = employeeUserRepository.findByOrganizationId(employeeUserWithPassword.orgId);
		if (!orgEmployees.isEmpty()) {
			validateCurrentUserCanManageEmpAccount(employeeUserWithPassword.orgId, employeeUserWithPassword.storeId, rolesList);
		}
		validateStoreForEmployeeCreation(employeeUserWithPassword, rolesList);

		EmployeeUserEntity employeeUserEntity = doCreateNewEmpAccountWithPassword(employeeUserWithPassword, rolesList);

		updateEmployeeUserWithPassword(employeeUserEntity, employeeUserWithPassword);

		return new UserApiResponse(employeeUserEntity.getId(), asList(NEED_ACTIVATION, ACTIVATION_SENT));
	}

	private void updateEmployeeUserWithPassword(EmployeeUserEntity employeeUserEntity,
												UserDTOs.EmployeeUserWithPassword employeeUserWithPassword) {

		empUserSvcHelper.validateNewPassword(employeeUserWithPassword.getPassword());
		if (employeeUserEntity.getEncryptedPassword() == null
				|| employeeUserEntity.getEncryptedPassword().isEmpty()) {
			employeeUserEntity.setEncryptedPassword(passwordEncoder.encode(employeeUserWithPassword.getPassword()));

			employeeUserEntity = employeeUserRepository.save(employeeUserEntity);

			if (employeeUserEntity.getEncryptedPassword() != null
				&& !employeeUserEntity.getEncryptedPassword().isEmpty()) {
				EmployeeUserOtpEntity employeeUserOtpEntity = employeeOtpService
						.createUserOtp(employeeUserEntity, REGISTER);
				sendEmployeeOtp(employeeUserEntity, employeeUserOtpEntity.getOtp());
			}
		}

	}

	private void sendEmployeeOtp(EmployeeUserEntity employeeUserEntity, String otp) {
		try {
			String orgName = organizationRepository.findById(employeeUserEntity.getOrganizationId())
					.orElseThrow()
					.getName();
			Map<String, String> parameterMap = new HashMap<>();
			parameterMap.put(OTP_PARAMETER, otp);
			if (employeeUserEntity.getEmail() != null && !employeeUserEntity.getEmail().isEmpty())
				mailService.send(orgName, employeeUserEntity.getEmail(),
						orgName + ACTIVATION_ACCOUNT_EMAIL_SUBJECT, OTP_TEMPLATE,
						parameterMap);
		} catch (Exception e) {
			logger.error(e, e);
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, GEN$0003, e.getMessage());
		}
	}

	private void validateStoreForEmployeeCreation(UserDTOs.EmployeeUserCreationObject employeeUserJson, List<String> roles) {
		Long orgId = employeeUserJson.orgId;
		Long storeId = employeeUserJson.storeId;
		boolean shopExists = shopRepo.existsByIdAndOrganizationEntity_IdAndRemoved(storeId, orgId, 0);
		boolean newUserHasStoreRoles =
				Stream.of(STORE_MANAGER.name(), STORE_EMPLOYEE.name())
				.anyMatch(roles::contains);
		if( newUserHasStoreRoles && !shopExists){
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$EMP$0012, storeId);
		}
	}




	private void validateEmpEmailAlreadyExists(UserDTOs.EmployeeUserCreationObject employeeUserJson) {
		if (employeeUserRepository.existsByEmailIgnoreCase(employeeUserJson.email)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$EMP$0006, employeeUserJson.getEmail());
		}
	}


	private EmployeeUserEntity doCreateNewEmpAccount(UserDTOs.EmployeeUserCreationObject employeeUserJson, List<String> rolesList) {
		EmployeeUserEntity employeeUserEntity = empUserSvcHelper.createEmployeeUser(employeeUserJson);
		empUserSvcHelper.createRoles(rolesList, employeeUserEntity, employeeUserJson.orgId);
		employeeUserEntity = empUserSvcHelper.generateResetPasswordToken(employeeUserEntity);
		empUserSvcHelper.sendRecoveryMail(employeeUserEntity);
		return employeeUserEntity;
	}

	private EmployeeUserEntity doCreateNewEmpAccountWithPassword(UserDTOs.EmployeeUserWithPassword employeeUserWithPassword,
																 List<String> rolesList) {
		EmployeeUserEntity employeeUserEntity = empUserSvcHelper.createEmployeeUser(employeeUserWithPassword);
		empUserSvcHelper.createRoles(rolesList, employeeUserEntity, employeeUserWithPassword.orgId);
//		employeeUserEntity = empUserSvcHelper.generateResetPasswordToken(employeeUserEntity);
//		empUserSvcHelper.sendRecoveryMail(employeeUserEntity);
		return employeeUserEntity;
	}



	private void validateCurrentUserCanManageEmpAccount(Long otherEmpOrgId, Long otherEmpStoreId, List<String> rolesList) {
		EmployeeUserEntity currentUser = getCurrentUser();
		Long userId = currentUser.getId();
		if (empUserSvcHelper.roleCannotManageUsers(userId)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$EMP$0008);
		}
		if ( empUserSvcHelper.hasInsufficientLevel(userId, rolesList)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$EMP$0009);
		}

		if (!securityService.currentUserHasRole(NASNAV_ADMIN)) {
			if (!currentUser.getOrganizationId().equals(otherEmpOrgId)) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$EMP$0010 );
			}
		}
		if (empUserSvcHelper.hasMaxRoleLevelOf(STORE_MANAGER, userId)) {
			if (!currentUser.getShopId().equals(otherEmpStoreId)){
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$EMP$0011 );
			}
		}
	}



	private List<String> extractRoles(UserDTOs.EmployeeUserCreationObject employeeUserJson) {
		return ofNullable(employeeUserJson)
				.map(json -> json.getRole())
				.map(this::extractRoles)
				.orElse(emptyList());
	}



	private List<String> extractRoles(UserDTOs.EmployeeUserUpdatingObject employeeUserJson) {
		return ofNullable(employeeUserJson)
				.map(json -> json.getRole())
				.map(this::extractRoles)
				.orElse(emptyList());
	}

	private List<String> extractRoles(String rolesStr) {
		return ofNullable(rolesStr)
				.map(str -> str.split(","))
				.map(Arrays::asList)
				.orElse(emptyList());
	}

	private EmployeeUserEntity getCurrentUser() {
		BaseUserEntity baseCurrentUser = securityService.getCurrentUser();
		
		if(!(baseCurrentUser instanceof EmployeeUserEntity)) {
			throw new RuntimeBusinessException(UNAUTHORIZED, G$USR$0001);
		}
		EmployeeUserEntity currentUser = (EmployeeUserEntity)baseCurrentUser;
		return currentUser;
	}




	@Override
	public UserApiResponse updateEmployeeUser(UserDTOs.EmployeeUserUpdatingObject employeeUserJson) {
		EmployeeUserEntity currentUser = getCurrentUser();

		Long updatedUserId = employeeUserJson.getUpdatedUserId();
		EmployeeUserEntity updateUser =
				ofNullable(updatedUserId)
				.flatMap(employeeUserRepository::findById)
				.orElse(currentUser);

		List<String> updatedUserNewRoles = extractRoles(employeeUserJson);
		if (!updatedUserNewRoles.isEmpty()) {
			empUserSvcHelper.isValidRolesList(updatedUserNewRoles);
		}
		List<String> updatedUserOldRoles = empUserSvcHelper.getEmployeeUserRoles(updatedUserId);
		List<String> allRolesToCheck = CollectionUtils.concat(updatedUserNewRoles, updatedUserOldRoles);

		validateCurrentUserCanManageEmpAccount(updateUser.getOrganizationId(), updateUser.getShopId(), allRolesToCheck);

		empUserSvcHelper.createRoles(updatedUserNewRoles, updateUser, updateUser.getOrganizationId());

		return empUserSvcHelper.updateEmployeeUser(currentUser.getId(), updateUser, employeeUserJson);
	}


	@Override
	public void deleteUser(Long userId) {
		employeeUserRepository.deleteById(userId);
	}



	@Override
	public BaseUserEntity getUserById(Long userId) {
		return employeeUserRepository.findById(userId).orElse(null);
	}


	@Override
	public BaseUserEntity update(BaseUserEntity employeeUserEntity) {
		return employeeUserRepository.saveAndFlush((EmployeeUserEntity) employeeUserEntity);
	}


	public void sendEmailRecovery(String email) {
		EmployeeUserEntity employeeUserEntity = getEmployeeUserByEmail(email);
		employeeUserEntity = empUserSvcHelper.generateResetPasswordToken(employeeUserEntity);
		empUserSvcHelper.sendRecoveryMail(employeeUserEntity);
	}


	@Override
	@Transactional
	public UserApiResponse recoverUser(PasswordResetObject data) {
		empUserSvcHelper.validateNewPassword(data.password);
		empUserSvcHelper.validateToken(data.token);
		EmployeeUserEntity employeeUserEntity = employeeUserRepository.getByResetPasswordToken(data.token)
												.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, U$LOG$0001));

		empUserSvcHelper.checkResetPasswordTokenExpiry(employeeUserEntity.getResetPasswordSentAt());
		employeeUserEntity.setResetPasswordToken(null);
		employeeUserEntity.setResetPasswordSentAt(null);
		employeeUserEntity.setEncryptedPassword(passwordEncoder.encode(data.password));
		employeeUserEntity.setUserStatus(ACTIVATED.getValue());
		employeeUserEntity = employeeUserRepository.saveAndFlush(employeeUserEntity);

		String token = resetRecoveredUserTokens(employeeUserEntity);

		return new UserApiResponse(employeeUserEntity.getId(), token);
	}

	private String resetRecoveredUserTokens(EmployeeUserEntity user) {
		securityService.logoutAll(user);
		UserTokensEntity tokenEntity = new UserTokensEntity();
		tokenEntity.setEmployeeUserEntity(user);
		tokenEntity.setToken(generateUUIDToken());
		return userTokenRepo.save(tokenEntity).getToken();
	}


	private EmployeeUserEntity getEmployeeUserByEmail(String email) {
		return employeeUserRepository
						.findByEmailIgnoreCase(email)
						.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, UXACTVX0007, email));
	}


	public List<UserRepresentationObject> getUserList(String token, Long orgId, Long shopId, String role) {
		EmployeeUserEntity user = (EmployeeUserEntity) securityService.getCurrentUser();
		Roles userHighestRole = roleService.getEmployeeHighestRole(user.getId());
		Set<String> roles = new HashSet<>();

		if (isNotBlankOrNull(role)) {
			if (!Enums.getIfPresent(Roles.class, role).isPresent())
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$EMP$0007,  role);
			if (!roleService.checkRoleOrder(userHighestRole.getValue(), role)) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$EMP$0013);
			}
			roles = Set.of(role);
		} else {
			if (!userHighestRole.equals(NASNAV_ADMIN))
				roles = Roles.getAllPrivileges().get(userHighestRole.name());
		}
		orgId = getUserOrgId(orgId, user, userHighestRole);
		shopId = getUserShopId(shopId, user, userHighestRole);

		UsersSearchParam params = new UsersSearchParam(roles, orgId, shopId);
		return criteriaQueryBuilder
				.getResultList(params, false)
				.stream()
				.map(EmployeeUserEntity::getRepresentation)
				.collect(toList());
	}

	private Long getUserOrgId(Long orgId, EmployeeUserEntity user, Roles userHighestRole) {
		if (userHighestRole.name().startsWith("O") || userHighestRole.name().startsWith("S"))
			return user.getOrganizationId();
		return orgId;
	}

	private Long getUserShopId(Long shopId, EmployeeUserEntity user, Roles userHighestRole) {
		if (userHighestRole.name().startsWith("S"))
			return user.getShopId();
		return shopId;
	}

	@Override
	public Boolean isUserDeactivated(BaseUserEntity user) {
		return user.getUserStatus().equals(NOT_ACTIVATED.getValue());
	}


	@Override
	public void suspendEmployeeAccount(Long id, Boolean suspend) {
		EmployeeUserEntity user = getAndValidateEmployeeToSuspend(id);
		UserStatus status = empUserSvcHelper.checkUserStatusForSuspension(user);
		if (suspend) {
			if (status.equals(ACCOUNT_SUSPENDED)) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$STATUS$0001);
			}
			user.setUserStatus(ACCOUNT_SUSPENDED.getValue());
			userTokenRepo.deleteByEmployeeUserEntity(user);
		} else {
			user.setUserStatus(ACTIVATED.getValue());
		}
		employeeUserRepository.save(user);
	}

	@Override
	public List<UserRepresentationObject> getAvailableEmployeesByOrgId(Long orgId) {
		return employeeUserRepository.findByOrganizationId(orgId)
				.stream()
				.map(user -> user.getRepresentation())
				.collect(toList());
	}


	private EmployeeUserEntity getAndValidateEmployeeToSuspend(Long id) {
		EmployeeUserEntity user = getEmployeeToSuspend(id);
		List<String> userRoles = empUserSvcHelper.getEmployeeUserRoles(user.getId());
		validateCurrentUserCanManageEmpAccount(user.getOrganizationId(), user.getShopId(), userRoles);
		validateUserNotSuspendingHimself(user);
		return user;
	}



	private EmployeeUserEntity getEmployeeToSuspend(Long id) {
		Optional<EmployeeUserEntity> optionalEmployeeUser;
		if (securityService.currentUserHasRole(NASNAV_ADMIN)) {
			optionalEmployeeUser = employeeUserRepository.findById(id);
		} else {
			Long orgId = securityService.getCurrentUserOrganizationId();
			optionalEmployeeUser = employeeUserRepository.findByIdAndOrganizationId(id, orgId);
		}
		return optionalEmployeeUser
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, U$EMP$0002, id));
	}



	private void validateUserNotSuspendingHimself(EmployeeUserEntity user) {
		EmployeeUserEntity currentUser = (EmployeeUserEntity) securityService.getCurrentUser();
		if (Objects.equals(user.getId(), currentUser.getId())) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$STATUS$0002);
		}
	}

	@Override
	public UserApiResponse activateUserAccount(ActivateOtpDto activateOtpDto) {
		EmployeeUserEntity user = employeeUserRepository.findByEmailIgnoreCaseAndOrganizationId(activateOtpDto.getEmail(), activateOtpDto.getOrgId())
				.orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, U$EMP$0004, activateOtpDto.getEmail()));
		employeeOtpService.validateOtp(activateOtpDto.getOtp(), user, OtpType.REGISTER);
		activateUserInDB(user);
		return securityService.login(user, false);
	}

	private void activateUserInDB(EmployeeUserEntity user) {
		user.setResetPasswordToken(null);
		user.setUserStatus(ACTIVATED.getValue());
		employeeUserRepository.save(user);
	}

}
