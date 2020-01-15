package com.nasnav.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.common.base.Enums;
import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dao.CommonUserRepository;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.UserRepository;
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
	public UserApiResponse createEmployeeUser(Long userId, String userToken, UserDTOs.EmployeeUserCreationObject employeeUserJson) {
		List<String> rolesList = Arrays.asList(employeeUserJson.role.split(","));
		empUserSvcHelper.validateBusinessRules(employeeUserJson.name, employeeUserJson.email, employeeUserJson.orgId, rolesList);
		// get current logged in user
		EmployeeUserEntity currentUser = getCurrentUser();
		
		// check if email and organization id already exists
		if (employeeUserRepository.getByEmailAndOrganizationId(employeeUserJson.email, employeeUserJson.orgId) == null) {
			int userType = empUserSvcHelper.roleCanCreateUser(currentUser.getId());
			if (userType != -1) { // can add employees
				if (userType == 2) { // can add employees within the same organization
					if (!currentUser.getOrganizationId().equals(employeeUserJson.orgId)) { //not the same organization
						throw new EntityValidationException("Error Occurred during user creation:: " + ResponseStatus.INSUFFICIENT_RIGHTS,
								EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS)), HttpStatus.NOT_ACCEPTABLE);
					}
					if (empUserSvcHelper.checkOrganizationRolesRights(rolesList)) {
						throw new EntityValidationException("Insufficient Rights ",
								EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS)), HttpStatus.UNAUTHORIZED);
					}
				} else if (userType == 3) {
					if (!currentUser.getShopId().equals(employeeUserJson.storeId)){ //not the same Store
						throw new EntityValidationException("Error Occurred during user creation:: " + ResponseStatus.INSUFFICIENT_RIGHTS,
								EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS)), HttpStatus.NOT_ACCEPTABLE);
					}
					if (empUserSvcHelper.checkStoreRolesRights(rolesList)) {
						throw new EntityValidationException("Insufficient Rights ",
								EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS)), HttpStatus.UNAUTHORIZED);
					}
				}
				// parse Json to EmployeeUserEntity
				EmployeeUserEntity employeeUserEntity = empUserSvcHelper.createEmployeeUser(employeeUserJson);
				// create Role and RoleEmployeeUser entities from the roles array
				empUserSvcHelper.createRoles(rolesList, employeeUserEntity.getId(), employeeUserJson.orgId);
				employeeUserEntity = empUserSvcHelper.generateResetPasswordToken(employeeUserEntity);
				empUserSvcHelper.sendRecoveryMail(employeeUserEntity);
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

	private EmployeeUserEntity getCurrentUser() {
		BaseUserEntity baseCurrentUser = securityService.getCurrentUser();
		
		if(!(baseCurrentUser instanceof EmployeeUserEntity)) {
			throw new EntityValidationException("Insufficient Rights ",
					EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS)), HttpStatus.UNAUTHORIZED);
		}
		
		EmployeeUserEntity currentUser = (EmployeeUserEntity)baseCurrentUser;
		return currentUser;
	}

	@Override
	public UserApiResponse updateEmployeeUser(Long userId, String userToken, UserDTOs.EmployeeUserUpdatingObject employeeUserJson) {
		EmployeeUserEntity updateUser,currentUser;
		
		int userType = empUserSvcHelper.roleCanCreateUser(userId); //check user privileges
		if (userType == -1) { // can't update employees
			throw new EntityValidationException(""+ResponseStatus.INSUFFICIENT_RIGHTS,
					EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INSUFFICIENT_RIGHTS)), HttpStatus.UNAUTHORIZED);
		}
		currentUser = getCurrentUser();
		if (StringUtils.isBlankOrNull(employeeUserJson.getUpdatedUserId())) {// check if same user doing the update
			updateUser = employeeUserRepository.getById(userId);
		} else {
			updateUser = employeeUserRepository.getById(employeeUserJson.getUpdatedUserId());
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
		return empUserSvcHelper.updateEmployeeUser(userType, updateUser, employeeUserJson);
	}

	public UserApiResponse login(UserDTOs.UserLoginObject body) {
		EmployeeUserEntity employeeUserEntity = this.employeeUserRepository.getByEmailAndOrganizationId(body.email, body.getOrgId());
		if (employeeUserEntity != null) {
			// check if account needs activation
			boolean accountNeedActivation = empUserSvcHelper.isEmployeeUserNeedActivation(employeeUserEntity);
			if (accountNeedActivation) {
				UserApiResponse failedLoginResponse = EntityUtils
						.createFailedLoginResponse(Collections.singletonList(ResponseStatus.NEED_ACTIVATION));
				throw new EntityValidationException("NEED_ACTIVATION ", failedLoginResponse, HttpStatus.LOCKED);
			}
			// ensure that password matched
			boolean passwordMatched = passwordEncoder.matches(body.password, employeeUserEntity.getEncryptedPassword());
			if (passwordMatched) {
				// check if account is locked
				if (empUserSvcHelper.isAccountLocked(employeeUserEntity)) { // TODO: so far there is no lockdown, so always
																	// false
					// //NOSONAR
					UserApiResponse failedLoginResponse = EntityUtils
							.createFailedLoginResponse(Collections.singletonList(ResponseStatus.ACCOUNT_SUSPENDED));
					throw new EntityValidationException("ACCOUNT_SUSPENDED ", failedLoginResponse, HttpStatus.LOCKED);
				}
				// generate new AuthenticationToken and perform post login updates
				empUserSvcHelper.updatePostLogin(employeeUserEntity);
				return empUserSvcHelper.createSuccessLoginResponse(employeeUserEntity);
			}
		}
		UserApiResponse failedLoginResponse = EntityUtils
				.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INVALID_CREDENTIALS));
		throw new EntityValidationException("INVALID_CREDENTIALS ", failedLoginResponse, HttpStatus.UNAUTHORIZED);
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
		employeeUserEntity = empUserSvcHelper.generateResetPasswordToken(employeeUserEntity);
		return empUserSvcHelper.sendRecoveryMail(employeeUserEntity);
	}

	@Override
	public UserApiResponse recoverUser(PasswordResetObject data) {
		validateNewPassword(data.password);
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
					UserApiResponse.createStatusApiResponse(Collections.singletonList(ResponseStatus.INVALID_TOKEN)),
					HttpStatus.NOT_ACCEPTABLE);
		}
		return UserApiResponse.createStatusApiResponse((long)employeeUserEntity.getId(), null);
	}

	private void validateNewPassword(String newPassword) {
		if (StringUtils.isBlankOrNull(newPassword) || newPassword.length() > EntityConstants.PASSWORD_MAX_LENGTH
				|| newPassword.length() < EntityConstants.PASSWORD_MIN_LENGTH) {
			throw new EntityValidationException("INVALID_PASSWORD  ",
					UserApiResponse.createStatusApiResponse(Collections.singletonList(ResponseStatus.INVALID_PASSWORD)),
					HttpStatus.NOT_ACCEPTABLE);
		}
	}

	private void checkResetPasswordTokenExpiry(EmployeeUserEntity employeeUserEntity) {
		LocalDateTime resetPasswordSentAt = employeeUserEntity.getResetPasswordSentAt();
		LocalDateTime tokenExpiryDate = resetPasswordSentAt.plusHours(EntityConstants.TOKEN_VALIDITY);
		if (LocalDateTime.now().isAfter(tokenExpiryDate)) {
			throw new EntityValidationException("EXPIRED_TOKEN  ",
					UserApiResponse.createStatusApiResponse(Collections.singletonList(ResponseStatus.EXPIRED_TOKEN)),
					HttpStatus.NOT_ACCEPTABLE);
		}
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
		if (!StringUtils.validateEmail(email)) {
			UserApiResponse userApiResponse = UserApiResponse.createMessagesApiResponse(false,
					Collections.singletonList(ResponseStatus.INVALID_EMAIL));
			throw new EntityValidationException("INVALID_EMAIL :" + email, userApiResponse, HttpStatus.NOT_ACCEPTABLE);
		}
		// load user entity by email
		EmployeeUserEntity employeeUserEntity = this.employeeUserRepository.getByEmailAndOrganizationId(email, orgId);
		if (StringUtils.isBlankOrNull(employeeUserEntity)) {
			UserApiResponse userApiResponse = UserApiResponse.createMessagesApiResponse(false,
					Collections.singletonList(ResponseStatus.EMAIL_NOT_EXIST));
			throw new EntityValidationException("EMAIL_NOT_EXIST", userApiResponse, HttpStatus.NOT_ACCEPTABLE);
		}
		return employeeUserEntity;
	}

	public List<UserRepresentationObject> getUserList(String token, Long orgId, Long storeId, String role) throws BusinessException {
		EmployeeUserEntity user = employeeUserRepository.findByAuthenticationToken(token).get();
		List<String> userRoles = empUserSvcHelper.getEmployeeUserRoles(user.getId());
		Set<String> roles = new HashSet<>();
		List<EmployeeUserEntity> usersEntites = new ArrayList<>();
		List<Long> employeesIds = new ArrayList<>();
		List<UserRepresentationObject> userRepObjs = new ArrayList<>();
		if (role != null) {
			if (!Enums.getIfPresent(Roles.class, role).isPresent())
				throw new BusinessException("INVALID_PARAM: role","No roles matching the provided role",HttpStatus.NOT_ACCEPTABLE);
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

		userRepObjs = usersEntites.stream().map(entity -> entity.getRepresentation()).collect(Collectors.toList());
		for(UserRepresentationObject obj : userRepObjs)
			obj.setRoles(new HashSet<>(empUserSvcHelper.getEmployeeUserRoles(obj.getId())));

		return userRepObjs;
	}
	

}
