package com.nasnav.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import com.nasnav.commons.utils.CollectionUtils;
import com.nasnav.exceptions.RuntimeBusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.common.base.Enums;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserDTOs.PasswordResetObject;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.enumerations.Roles;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.EntityValidationException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.helpers.EmployeeUserServiceHelper;

import static com.nasnav.commons.utils.EntityUtils.createFailedLoginResponse;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.enumerations.Roles.*;
import static com.nasnav.exceptions.ErrorCodes.UXACTVX0005;
import static com.nasnav.response.ResponseStatus.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class EmployeeUserServiceImpl implements EmployeeUserService {

	

	private EmployeeUserRepository employeeUserRepository;
	private PasswordEncoder passwordEncoder;
	private RoleServiceImpl roleServiceImpl;
	
	@Autowired
	private SecurityService securityService;

	@Autowired
	private EmployeeUserServiceHelper empUserSvcHelper;

	
	
	@Autowired
	public EmployeeUserServiceImpl(EmployeeUserServiceHelper helper, EmployeeUserRepository employeeUserRepository,
								   PasswordEncoder passwordEncoder, RoleServiceImpl roleServiceImpl) {
		this.empUserSvcHelper = helper;
		this.employeeUserRepository = employeeUserRepository;
		this.passwordEncoder = passwordEncoder;
		this.roleServiceImpl = roleServiceImpl;
	}




	@Override
	public UserApiResponse createEmployeeUser(String userToken, UserDTOs.EmployeeUserCreationObject employeeUserJson) {
		List<String> rolesList = extractRoles(employeeUserJson);

		empUserSvcHelper.validateBusinessRules(employeeUserJson.name, employeeUserJson.email, employeeUserJson.orgId, rolesList);
		validateEmpEmailAlreadyExists(employeeUserJson);
		validateCurrentUserCanManageEmpAccount(employeeUserJson.orgId, employeeUserJson.storeId, rolesList);

		EmployeeUserEntity employeeUserEntity = doCreateNewEmpAccount(employeeUserJson, rolesList);
		return UserApiResponse
				.createStatusApiResponse(
						employeeUserEntity.getId(),
						asList(NEED_ACTIVATION, ACTIVATION_SENT));
	}




	private void validateEmpEmailAlreadyExists(UserDTOs.EmployeeUserCreationObject employeeUserJson) {
		if (employeeUserRepository.findByEmailIgnoreCaseAndOrganizationId(employeeUserJson.email, employeeUserJson.orgId).isPresent()) {
			throw new EntityValidationException(
							EMAIL_EXISTS.name(),
							createFailedLoginResponse(singletonList(EMAIL_EXISTS)),
							NOT_ACCEPTABLE);
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

		if (empUserSvcHelper.roleCannotManageUsers(currentUser.getId())
				|| empUserSvcHelper.hasInsuffiecentLevel(currentUser.getId(), rolesList)) {
			throw new EntityValidationException("Insufficient Rights ",
					createFailedLoginResponse(singletonList(INSUFFICIENT_RIGHTS)), NOT_ACCEPTABLE);
		}

		if (!securityService.currentUserHasRole(NASNAV_ADMIN)) {
			if (!currentUser.getOrganizationId().equals(otherEmpOrgId)) {
				throw new EntityValidationException("Error Occurred during user creation:: " + INSUFFICIENT_RIGHTS,
						createFailedLoginResponse(singletonList(INSUFFICIENT_RIGHTS)), NOT_ACCEPTABLE);
			}
		}

		if (securityService.currentUserHasRole(STORE_MANAGER)) {
			if (!currentUser.getShopId().equals(otherEmpStoreId)){
				throw new EntityValidationException("Error Occurred during user creation:: " + INSUFFICIENT_RIGHTS,
						createFailedLoginResponse(singletonList(INSUFFICIENT_RIGHTS)), NOT_ACCEPTABLE);
			}
		}
	}



	private List<String> extractRoles(UserDTOs.EmployeeUserCreationObject employeeUserJson) {
		return ofNullable(employeeUserJson)
				.map(json -> json.role)
				.map(roleStr -> roleStr.split(","))
				.map(Arrays::asList)
				.orElse(emptyList());
	}



	private List<String> extractRoles(UserDTOs.EmployeeUserUpdatingObject employeeUserJson) {
		return ofNullable(employeeUserJson)
				.map(json -> json.getRole())
				.map(roleStr -> roleStr.split(","))
				.map(Arrays::asList)
				.orElse(emptyList());
	}



	private EmployeeUserEntity getCurrentUser() {
		BaseUserEntity baseCurrentUser = securityService.getCurrentUser();
		
		if(!(baseCurrentUser instanceof EmployeeUserEntity)) {
			throw new EntityValidationException("Insufficient Rights ",
					createFailedLoginResponse(singletonList(INSUFFICIENT_RIGHTS)), UNAUTHORIZED);
		}
		EmployeeUserEntity currentUser = (EmployeeUserEntity)baseCurrentUser;
		return currentUser;
	}




	@Override
	public UserApiResponse updateEmployeeUser(String userToken, UserDTOs.EmployeeUserUpdatingObject employeeUserJson) throws BusinessException {
		EmployeeUserEntity currentUser = getCurrentUser();

		Long updatedUserId = employeeUserJson.getUpdatedUserId();
		EmployeeUserEntity updateUser =
				ofNullable(updatedUserId)
				.flatMap(employeeUserRepository::findById)
				.orElse(currentUser);

		List<String> updatedUserNewRoles = extractRoles(employeeUserJson);
		List<String> updatedUserOldRoles = empUserSvcHelper.getEmployeeUserRoles(updatedUserId);
		List<String> allRolesToCheck = CollectionUtils.concat(updatedUserNewRoles, updatedUserOldRoles);
		validateCurrentUserCanManageEmpAccount(updateUser.getOrganizationId(), updateUser.getShopId(), allRolesToCheck);

		return empUserSvcHelper.updateEmployeeUser(currentUser.getId(), updateUser, employeeUserJson);
	}




	public UserApiResponse login(UserDTOs.UserLoginObject body) {
		EmployeeUserEntity employeeUserEntity =
				employeeUserRepository
					.findByEmailIgnoreCaseAndOrganizationId(body.email, body.getOrgId())
					.orElseThrow(this::createInvalidCredentialsException);

		boolean accountNeedActivation = empUserSvcHelper.isEmployeeUserNeedActivation(employeeUserEntity);
		if (accountNeedActivation) {
			UserApiResponse failedLoginResponse = createFailedLoginResponse(singletonList(NEED_ACTIVATION));
			throw new EntityValidationException("NEED_ACTIVATION ", failedLoginResponse, HttpStatus.LOCKED);
		}

		boolean passwordMatched = passwordEncoder.matches(body.password, employeeUserEntity.getEncryptedPassword());
		if (!passwordMatched) {
			throw createInvalidCredentialsException();
		}

		if (empUserSvcHelper.isAccountLocked(employeeUserEntity)) { // TODO: so far there is no lockdown, so always
			UserApiResponse failedLoginResponse = createFailedLoginResponse(singletonList(ResponseStatus.ACCOUNT_SUSPENDED));
			throw new EntityValidationException("ACCOUNT_SUSPENDED ", failedLoginResponse, HttpStatus.LOCKED);
		}

		empUserSvcHelper.updatePostLogin(employeeUserEntity);
		return empUserSvcHelper.createSuccessLoginResponse(employeeUserEntity);
	}




	private RuntimeException  createInvalidCredentialsException() {
		UserApiResponse failedLoginResponse =
				createFailedLoginResponse(singletonList(INVALID_CREDENTIALS));
		return new EntityValidationException("INVALID_CREDENTIALS ", failedLoginResponse, UNAUTHORIZED);
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
	public UserApiResponse sendEmailRecovery(String email, Long orgId) {
		EmployeeUserEntity employeeUserEntity = getEmployeeUserByEmail(email, orgId);
		employeeUserEntity = empUserSvcHelper.generateResetPasswordToken(employeeUserEntity);
		return empUserSvcHelper.sendRecoveryMail(employeeUserEntity);
	}

	@Override
	public UserApiResponse recoverUser(PasswordResetObject data) {
		validateNewPassword(data.password);
		if(isBlankOrNull(data.token)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, UXACTVX0005);
		}
		EmployeeUserEntity employeeUserEntity = employeeUserRepository.getByResetPasswordToken(data.token);
		if (StringUtils.isNotBlankOrNull(employeeUserEntity)) {
			// if resetPasswordToken is not active, throw exception for invalid
			// resetPasswordToken
			checkResetPasswordTokenExpiry(employeeUserEntity);
			employeeUserEntity.setResetPasswordToken(null);
			employeeUserEntity.setResetPasswordSentAt(null);
			employeeUserEntity.setEncryptedPassword(passwordEncoder.encode(data.password));
			employeeUserRepository.saveAndFlush(employeeUserEntity);
		} else {
			throw new EntityValidationException("INVALID_TOKEN  ",
					UserApiResponse.createStatusApiResponse(singletonList(ResponseStatus.INVALID_TOKEN)),
					NOT_ACCEPTABLE);
		}
		return UserApiResponse.createStatusApiResponse((long)employeeUserEntity.getId(), null);
	}

	private void validateNewPassword(String newPassword) {
		if (isBlankOrNull(newPassword) || newPassword.length() > EntityConstants.PASSWORD_MAX_LENGTH
				|| newPassword.length() < EntityConstants.PASSWORD_MIN_LENGTH) {
			throw new EntityValidationException("INVALID_PASSWORD  ",
					UserApiResponse.createStatusApiResponse(singletonList(ResponseStatus.INVALID_PASSWORD)),
					NOT_ACCEPTABLE);
		}
	}

	private void checkResetPasswordTokenExpiry(EmployeeUserEntity employeeUserEntity) {
		LocalDateTime resetPasswordSentAt = employeeUserEntity.getResetPasswordSentAt();
		LocalDateTime tokenExpiryDate = resetPasswordSentAt.plusHours(EntityConstants.TOKEN_VALIDITY);
		if (LocalDateTime.now().isAfter(tokenExpiryDate)) {
			throw new EntityValidationException("EXPIRED_TOKEN  ",
					UserApiResponse.createStatusApiResponse(singletonList(ResponseStatus.EXPIRED_TOKEN)),
					NOT_ACCEPTABLE);
		}
	}

	@Override
	public boolean checkAuthToken(Long userId, String authToken) {
		return employeeUserRepository.existsByIdAndAuthenticationToken(userId, authToken);
	}




	private EmployeeUserEntity getEmployeeUserByEmail(String email, Long orgId) {
		if (!StringUtils.validateEmail(email)) {
			UserApiResponse userApiResponse =
					UserApiResponse
							.createMessagesApiResponse(false, singletonList(INVALID_EMAIL));
			throw new EntityValidationException("INVALID_EMAIL :" + email, userApiResponse, NOT_ACCEPTABLE);
		}

		return	employeeUserRepository
						.findByEmailIgnoreCaseAndOrganizationId(email, orgId)
						.orElseThrow(this::createEmailNotExistException);
	}




	private RuntimeException  createEmailNotExistException() {
		UserApiResponse userApiResponse =
				UserApiResponse
				.createMessagesApiResponse(false,singletonList(EMAIL_NOT_EXIST));
		return new EntityValidationException("EMAIL_NOT_EXIST", userApiResponse, NOT_ACCEPTABLE);
	}




	public List<UserRepresentationObject> getUserList(String token, Long orgId, Long storeId, String role) throws BusinessException {
		EmployeeUserEntity user = (EmployeeUserEntity)securityService.getCurrentUser();
		List<String> userRoles = empUserSvcHelper.getEmployeeUserRoles(user.getId());
		Set<String> roles = new HashSet<>();
		List<EmployeeUserEntity> usersEntites = new ArrayList<>();
		List<Long> employeesIds = new ArrayList<>();
		List<UserRepresentationObject> userRepObjs = new ArrayList<>();
		if (role != null) {
			if (!Enums.getIfPresent(Roles.class, role).isPresent())
				throw new BusinessException("INVALID_PARAM: role","No roles matching the provided role", NOT_ACCEPTABLE);
			for (String userRole : userRoles)
				if (roleServiceImpl.checkRoleOrder(userRole, role)) {
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
				if (userRoles.contains("ORGANIZATION_ADMIN") || userRoles.contains("ORGANIZATION_MANAGER") || userRoles.contains("ORGANIZATION_EMPLOYEE")) {
					orgId = user.getOrganizationId();
					if (userRoles.contains("ORGANIZATION_ADMIN")) {
                        roles = Roles.getOrganizationAdminPrelivedge();
					} else if (userRoles.contains("ORGANIZATION_MANAGER")) {
                        roles = Roles.getOrganizationManagerPrelivedge();
					} else {
                        roles = Roles.getOrganizationEmployeePrelivedge();
					}
				}
                else if (userRoles.contains("STORE_ADMIN") || userRoles.contains("STORE_MANAGER") || userRoles.contains("STORE_EMPLOYEE")) {
                    orgId = user.getOrganizationId();
                    storeId = user.getShopId();
                    if (userRoles.contains("STORE_ADMIN")) {
                        roles = Roles.getStoreAdminPrelivedge();
                    } else if (userRoles.contains("STORE_MANAGER")) {
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

		userRepObjs = usersEntites.stream().map(entity -> entity.getRepresentation()).collect(toList());
		for(UserRepresentationObject obj : userRepObjs)
			obj.setRoles(new HashSet<>(empUserSvcHelper.getEmployeeUserRoles(obj.getId())));

		return userRepObjs;
	}
	
	
	
	
	
	@Override
	public Boolean isUserDeactivated(BaseUserEntity user) {
		return Objects.equals(user.getAuthenticationToken(), DEACTIVATION_CODE);
	}
	

}
