package com.nasnav.service;

import java.util.*;
import java.util.stream.Stream;

import com.nasnav.commons.utils.CollectionUtils;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.UserTokenRepository;
import com.nasnav.enumerations.UserStatus;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.common.base.Enums;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserDTOs.PasswordResetObject;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.enumerations.Roles;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.helpers.UserServicesHelper;
import org.springframework.transaction.annotation.Transactional;

import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.commons.utils.EntityUtils.collectionContainsAnyOf;
import static com.nasnav.commons.utils.StringUtils.generateUUIDToken;
import static com.nasnav.enumerations.Roles.*;
import static com.nasnav.enumerations.UserStatus.ACCOUNT_SUSPENDED;
import static com.nasnav.enumerations.UserStatus.ACTIVATED;
import static com.nasnav.enumerations.UserStatus.NOT_ACTIVATED;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.response.ResponseStatus.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.*;

@Service
public class EmployeeUserServiceImpl implements EmployeeUserService {

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

	@Override
	@Transactional
	public UserApiResponse createEmployeeUser(UserDTOs.EmployeeUserCreationObject employeeUserJson) {
		List<String> rolesList = extractRoles(employeeUserJson);

		empUserSvcHelper.validateBusinessRules(employeeUserJson.name, employeeUserJson.email, employeeUserJson.orgId);
		empUserSvcHelper.isValidRolesList(rolesList);
		validateEmpEmailAlreadyExists(employeeUserJson);
		validateCurrentUserCanManageEmpAccount(employeeUserJson.orgId, employeeUserJson.storeId, rolesList);
		validateStoreForEmployeeCreation(employeeUserJson, rolesList);

		EmployeeUserEntity employeeUserEntity = doCreateNewEmpAccount(employeeUserJson, rolesList);
		return new UserApiResponse(employeeUserEntity.getId(), asList(NEED_ACTIVATION, ACTIVATION_SENT));
	}




	private void validateStoreForEmployeeCreation(UserDTOs.EmployeeUserCreationObject employeeUserJson, List<String> roles) {
		Long orgId = employeeUserJson.orgId;
		Long storeId = employeeUserJson.storeId;
		boolean shopExists = shopRepo.existsByIdAndOrganizationEntity_Id(storeId, orgId);
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
		empUserSvcHelper.createRoles(rolesList, employeeUserEntity.getId(), employeeUserJson.orgId);
		employeeUserEntity = empUserSvcHelper.generateResetPasswordToken(employeeUserEntity);
		empUserSvcHelper.sendRecoveryMail(employeeUserEntity);
		return employeeUserEntity;
	}



	private void validateCurrentUserCanManageEmpAccount(Long otherEmpOrgId, Long otherEmpStoreId, List<String> rolesList) {
		EmployeeUserEntity currentUser = getCurrentUser();
		Long userId = currentUser.getId();
		if (empUserSvcHelper.roleCannotManageUsers(userId)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$EMP$0008);
		}
		if ( empUserSvcHelper.hasInsuffiecentLevel(userId, rolesList)) {
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

		empUserSvcHelper.createRoles(updatedUserNewRoles, updatedUserId, updateUser.getOrganizationId());

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


	@Override
	public void sendEmailRecovery(String email, Long orgId) {
		EmployeeUserEntity employeeUserEntity = getEmployeeUserByEmail(email, orgId);
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


	private EmployeeUserEntity getEmployeeUserByEmail(String email, Long orgId) {
		empUserSvcHelper.validateEmail(email);
		empUserSvcHelper.validateOrgId(orgId);
		return employeeUserRepository
						.findByEmailIgnoreCaseAndOrganizationId(email, orgId)
						.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, UXACTVX0001, email, orgId));
	}


	public List<UserRepresentationObject> getUserList(String token, Long orgId, Long storeId, String role) {
		EmployeeUserEntity user = (EmployeeUserEntity)securityService.getCurrentUser();
		List<String> userRoles = empUserSvcHelper.getEmployeeUserRoles(user.getId());
		Set<String> roles = new HashSet<>();
		List<EmployeeUserEntity> usersEntites = new ArrayList<>();
		List<Long> employeesIds = new ArrayList<>();
		List<UserRepresentationObject> userRepObjs = new ArrayList<>();
		if (role != null) {
			if (!Enums.getIfPresent(Roles.class, role).isPresent())
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$EMP$0007,  role);
			for (String userRole : userRoles)
				if (roleService.checkRoleOrder(userRole, role)) {
					roles.add(role);
					if (userRole.startsWith("O"))
						orgId = user.getOrganizationId();
					else if (userRole.startsWith("S")){
						orgId = user.getOrganizationId();
						storeId = user.getShopId();
					}
					break;
				}
			if (roles.isEmpty())
				return userRepObjs;
		} else {
			if (!userRoles.contains("NASNAV_ADMIN")) {
				if ( collectionContainsAnyOf(userRoles, "ORGANIZATION_ADMIN", "ORGANIZATION_MANAGER", "ORGANIZATION_EMPLOYEE")) {
					orgId = user.getOrganizationId();
					if (userRoles.contains("ORGANIZATION_ADMIN")) {
                        roles = Roles.getOrganizationAdminPrelivedge();
					} else if (userRoles.contains("ORGANIZATION_MANAGER")) {
                        roles = Roles.getOrganizationManagerPrelivedge();
					} else {
                        roles = Roles.getOrganizationEmployeePrelivedge();
					}
				}
                else if (collectionContainsAnyOf(userRoles, "STORE_MANAGER", "STORE_EMPLOYEE")) {
                    orgId = user.getOrganizationId();
                    storeId = user.getShopId();
                    if (userRoles.contains("STORE_MANAGER")) {
                        roles = Roles.getStoreManagerPrelivedge();
                    } else
                        roles.add("STORE_EMPLOYEE");
                }
			}
		}
		if (roles.isEmpty()) {
			if (storeId == null && orgId == null)
				usersEntites = employeeUserRepository.findAll();
			else if (storeId != null && orgId == null)
				usersEntites = employeeUserRepository.findByShopId(storeId);
			else if (storeId == null && orgId != null)
				usersEntites = employeeUserRepository.findByOrganizationId(orgId);
			else
				usersEntites = employeeUserRepository.findByOrganizationIdAndShopId(orgId, storeId);
		} else {
			employeesIds = empUserSvcHelper.getEmployeesIds(new ArrayList<>(roles));
			if (storeId == null && orgId == null)
				usersEntites = employeeUserRepository.findByIdIn(employeesIds);
			else if (storeId != null && orgId == null)
				usersEntites = employeeUserRepository.findByShopIdAndIdIn(storeId, employeesIds);
			else if (storeId == null && orgId != null)
				usersEntites = employeeUserRepository.findByOrganizationIdAndIdIn(orgId, employeesIds);
			else
				usersEntites = employeeUserRepository.findByOrganizationIdAndShopIdAndIdIn(orgId, storeId, employeesIds);
		}

		userRepObjs = usersEntites
				.stream()
				.map(entity -> entity.getRepresentation())
				.collect(toList());

		userRepObjs
				.stream()
				.forEach(obj -> obj.setRoles(new HashSet<>(empUserSvcHelper.getEmployeeUserRoles(obj.getId()))));

		return userRepObjs;
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

}
