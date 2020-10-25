package com.nasnav.service;

import static com.nasnav.commons.utils.StringUtils.generateUUIDToken;
import static com.nasnav.commons.utils.StringUtils.isNotBlankOrNull;
import static com.nasnav.constatnts.EmailConstants.ACTIVATION_ACCOUNT_EMAIL_SUBJECT;
import static com.nasnav.constatnts.EmailConstants.ACTIVATION_ACCOUNT_URL_PARAMETER;
import static com.nasnav.constatnts.EmailConstants.NEW_EMAIL_ACTIVATION_TEMPLATE;
import static com.nasnav.constatnts.EmailConstants.USERNAME_PARAMETER;
import static com.nasnav.enumerations.Roles.NASNAV_ADMIN;
import static com.nasnav.enumerations.UserStatus.*;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.response.ResponseStatus.ACTIVATION_SENT;
import static com.nasnav.response.ResponseStatus.NEED_ACTIVATION;
import static com.nasnav.service.helpers.LoginHelper.isInvalidRedirectUrl;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.*;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.enumerations.UserStatus;
import com.nasnav.dao.*;
import com.nasnav.service.helpers.UserServicesHelper;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.web.servlet.view.RedirectView;

import com.google.common.collect.ObjectArrays;
import com.nasnav.AppConfig;
import com.nasnav.constatnts.EmailConstants;
import com.nasnav.dto.AddressDTO;
import com.nasnav.dto.AddressRepObj;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.user.ActivationEmailResendDTO;
import com.nasnav.exceptions.EntityValidationException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.AddressesEntity;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;

@Service
public class UserServiceImpl implements UserService {

	private Logger logger = LogManager.getLogger();

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserServicesHelper userServicesHelper;
	@Autowired
	private SecurityService securityService;
	@Autowired
	private DomainService domainService;
	@Autowired
	private OrganizationService orgService;
	@Autowired
	private MailService mailService;

	@Autowired
	private UserTokenRepository userTokenRepo;
	@Autowired
	private AddressRepository addressRepo;
	@Autowired
	private CommonUserRepository commonUserRepo;
	@Autowired
	private OrganizationRepository orgRepo;
	@Autowired
	private AreaRepository areaRepo;
	@Autowired
	private UserRepository userRepository;

	@Autowired
	AppConfig appConfig;


	@Override
	public UserApiResponse registerUserV2(UserDTOs.UserRegistrationObjectV2 userJson) {
		validateNewUserRegistration(userJson);

		UserEntity user = createNewUserEntity(userJson);		
		setUserAsDeactivated(user);
		generateResetPasswordToken(user);
		user = userRepository.saveAndFlush(user);
		
		sendActivationMail(user, userJson.getRedirectUrl());

		return new UserApiResponse(user.getId(), asList(NEED_ACTIVATION, ACTIVATION_SENT));
	}

	
	
	
	private void validateNewUserRegistration(UserDTOs.UserRegistrationObjectV2 userJson) {
		if (!userJson.confirmationFlag) {
			throw new EntityValidationException("Registration not confirmed by user!", null, NOT_ACCEPTABLE);
		}

		userServicesHelper.validateBusinessRules(userJson.getName(), userJson.getEmail(), userJson.getOrgId());
		userServicesHelper.validateNewPassword(userJson.password);

		Long orgId = userJson.getOrgId();

		OrganizationEntity org = orgRepo.findById(orgId)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$0001));

		if (userRepository.existsByEmailIgnoreCaseAndOrganizationId(userJson.email, orgId)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$LOG$0007, userJson.getEmail(), userJson.getOrgId());
		}
		
		validateActivationRedirectUrl(userJson.getRedirectUrl(), orgId);
	}




	private void validateActivationRedirectUrl(String redirectUrl, Long orgId) {
		List<String> orgDomains = domainService.getOrganizationDomainOnly(orgId);
		if(isNull(redirectUrl) || isInvalidRedirectUrl(redirectUrl, orgDomains)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, UXACTVX0004, redirectUrl);
		}
	}
	



	private void sendActivationMail(UserEntity userEntity, String redirectUrl) {
		try {
			Map<String, String> parametersMap = createActivationEmailParameters(userEntity, redirectUrl);
			mailService.send(userEntity.getEmail(), ACTIVATION_ACCOUNT_EMAIL_SUBJECT,
					NEW_EMAIL_ACTIVATION_TEMPLATE, parametersMap);
		} catch (Exception e) {
			logger.error(e, e);
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR,  GEN$0003, e.getMessage());
		}
	}




	private Map<String, String> createActivationEmailParameters(UserEntity userEntity, String redirectUrl) {
		String domain = domainService.getBackendUrl();
		String orgDomain = domainService.getOrganizationDomainAndSubDir(userEntity.getOrganizationId());

		String activationRedirectUrl = buildActivationRedirectUrl(userEntity, redirectUrl);
		String orgLogo = domain + "/files/"+ orgService.getOrgLogo(userEntity.getOrganizationId());
		String orgName = orgRepo.findById(userEntity.getOrganizationId()).get().getName();

		Map<String, String> parametersMap = new HashMap<>();
		parametersMap.put(USERNAME_PARAMETER, userEntity.getName());
		parametersMap.put(ACTIVATION_ACCOUNT_URL_PARAMETER, activationRedirectUrl);
		parametersMap.put("orgDomain", orgDomain);
		parametersMap.put("orgLogo", orgLogo);
		parametersMap.put("orgName", orgName);
		return parametersMap;
	}



	private String buildActivationRedirectUrl(UserEntity userEntity, String redirectUrl) {
		URIBuilder builder;
		try {
			builder = new URIBuilder(redirectUrl);
			builder.addParameter("activation_token", userEntity.getResetPasswordToken());
			return builder.build().toString();
		} catch (URISyntaxException e) {
			logger.error(e, e);
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, UXACTVX0004, redirectUrl);
		}		
	}




	private UserEntity createNewUserEntity(UserDTOs.UserRegistrationObjectV2 userJson) {
		UserEntity user = new UserEntity();
		user.setName(userJson.getName());
		user.setEmail(userJson.getEmail());
		user.setOrganizationId(userJson.getOrgId());
		user.setEncryptedPassword(passwordEncoder.encode(userJson.password));
		user.setPhoneNumber(userJson.getPhoneNumber());
		return user;
	}



	private void setUserAsDeactivated(UserEntity user) {
		user.setUserStatus(NOT_ACTIVATED.getValue());
	}

	
	
	@Override
	public Boolean isUserDeactivated(BaseUserEntity user) {
		UserEntity userEntity = (UserEntity)user;
		return userEntity.getUserStatus().equals(NOT_ACTIVATED.getValue());
	}


	@Transactional
	@Override
	public UserApiResponse updateUser(UserDTOs.EmployeeUserUpdatingObject userJson) {
		UserEntity userEntity = (UserEntity) securityService.getCurrentUser();
		List<ResponseStatus> successResponseStatusList = new ArrayList<>();
		if (isNotBlankOrNull(userJson.getName())) {
			userServicesHelper.validateName(userJson.getName());
			userEntity.setName(userJson.getName());
		}
		if (isNotBlankOrNull(userJson.email)){
			userServicesHelper.validateEmail(userJson.getEmail());
			userEntity.setEmail(userJson.email);
			generateResetPasswordToken(userEntity);
			userEntity = userRepository.saveAndFlush(userEntity);
			sendRecoveryMail(userEntity);
			successResponseStatusList.addAll(asList(NEED_ACTIVATION, ACTIVATION_SENT));
		}
		String [] defaultIgnoredProperties = new String[]{"name", "email", "org_id", "store_id", "role"};
		String [] allIgnoredProperties = new HashSet<String>(
				  asList(ObjectArrays.concat(getNullProperties(userJson), defaultIgnoredProperties, String.class))).toArray(new String[0]);

		BeanUtils.copyProperties(userJson, userEntity, allIgnoredProperties);
		Long userId = userRepository.saveAndFlush(userEntity).getId();
		if (successResponseStatusList.isEmpty()) {
			successResponseStatusList.add(ResponseStatus.ACTIVATED);
		}
		return new UserApiResponse(userId, successResponseStatusList);
	}


	@Override
	public AddressDTO updateUserAddress(AddressDTO addressDTO) {
		UserEntity user = (UserEntity) securityService.getCurrentUser();
		AddressDTO newAddress = setUserAddresses(addressDTO, user);
		addressRepo.linkAddressToUser(user.getId(), newAddress.getId());
		if (addressDTO.getPrincipal() != null) {
			if (addressDTO.getPrincipal().booleanValue()) {
				addressRepo.makeAddressNotPrincipal(user.getId());
				addressRepo.makeAddressPrincipal(user.getId(), newAddress.getId());
			}
		}
		return newAddress;
	}


	@Override
	public void removeUserAddress(Long id) {
		UserEntity user = (UserEntity) securityService.getCurrentUser();
		if (addressRepo.countByUserIdAndAddressId(id, user.getId()) == 0)
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0002, id);

		addressRepo.unlinkAddressFromUser(id, user.getId());
	}


	private AddressDTO setUserAddresses(AddressDTO addressDTO, UserEntity userEntity) {
		AddressesEntity address = new AddressesEntity();
		if (addressDTO.getId() != null) {
			if (addressRepo.countByUserIdAndAddressId(addressDTO.getId(), userEntity.getId()) == 0)
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0002, addressDTO.getId());
			addressRepo.unlinkAddressFromUser(addressDTO.getId(), userEntity.getId());
		}
		BeanUtils.copyProperties(addressDTO, address, new String[] {"id"});
		if (addressDTO.getAreaId() != null) {
			if (areaRepo.existsById(addressDTO.getAreaId())) {
				address.setAreasEntity(areaRepo.findById(addressDTO.getAreaId()).get());
			}
		}

		addressRepo.save(address);

		BeanUtils.copyProperties(address, addressDTO);

		return addressDTO;
	}

	private void validateName(String name) {
		if (!StringUtils.validateName(name)) {
			throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, U$EMP$0003, name );
		}
	}

	private void validateEmail(String email) {
		if (!StringUtils.validateEmail(email)) {
			throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, U$EMP$0004, email);
		}
	}


	@Override
	public void deleteUser(Long userId) {
		userRepository.deleteById(userId);
	}


	@Override
	public UserEntity getUserById(Long userId) {
		return userRepository.findById(userId).orElse(null);
	}

	@Override
	public BaseUserEntity update(BaseUserEntity userEntity) {
		return userRepository.saveAndFlush((UserEntity) userEntity);
	}

	@Override
	public void sendEmailRecovery(String email, Long orgId) {
		UserEntity userEntity = getUserEntityByEmailAndOrgId(email, orgId);
		generateResetPasswordToken(userEntity);
		userEntity = userRepository.saveAndFlush(userEntity);
		sendRecoveryMail(userEntity);
	}


	private UserEntity getUserEntityByEmailAndOrgId(String email, Long orgId) {
		userServicesHelper.validateEmail(email);
		userServicesHelper.validateOrgId(orgId);

		return ofNullable(userRepository.getByEmailIgnoreCaseAndOrganizationId(email, orgId))
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, UXACTVX0001, email, orgId));
	}


	
	
	
	private void generateResetPasswordToken(UserEntity userEntity) {
		String generatedToken = generateResetPasswordToken();
		userEntity.setResetPasswordToken(generatedToken);
		userEntity.setResetPasswordSentAt(now());
	}

	
	
	
	private void sendRecoveryMail(UserEntity userEntity) {
		String userName = ofNullable(userEntity.getName()).orElse("User");
		try {
			// create parameter map to replace parameter by actual UserEntity data.
			Map<String, String> parametersMap = new HashMap<>();
			parametersMap.put(EmailConstants.USERNAME_PARAMETER, userName);
			parametersMap.put(EmailConstants.CHANGE_PASSWORD_URL_PARAMETER,
					appConfig.mailRecoveryUrl.concat(userEntity.getResetPasswordToken()));
			// send Recovery mail to user
			this.mailService.send(userEntity.getEmail(), EmailConstants.CHANGE_PASSWORD_EMAIL_SUBJECT,
					EmailConstants.CHANGE_PASSWORD_EMAIL_TEMPLATE, parametersMap);
		} catch (Exception e) {
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, GEN$0003, e.getMessage());
		}
	}

	/**
	 * generate new ResetPasswordToken and ensure that this ResetPasswordToken is
	 * never used before.
	 *
	 * @return unique generated ResetPasswordToken.
	 */
	private String generateResetPasswordToken() {
		String generatedToken = generateUUIDToken();
		boolean existsByToken = userRepository.existsByResetPasswordToken(generatedToken);
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
		boolean existsByToken = userRepository.existsByResetPasswordToken(generatedToken);
		if (existsByToken) {
			return reGenerateResetPasswordToken();
		}
		return generatedToken;
	}

	@Override
	public UserApiResponse recoverUser(UserDTOs.PasswordResetObject data) {
		userServicesHelper.validateNewPassword(data.password);
		userServicesHelper.validateToken(data.token);
		UserEntity userEntity = userRepository.getByResetPasswordToken(data.token)
								.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, U$LOG$0001));;

		userServicesHelper.checkResetPasswordTokenExpiry(userEntity.getResetPasswordSentAt());
		userEntity.setResetPasswordToken(null);
		userEntity.setResetPasswordSentAt(null);
		userEntity.setEncryptedPassword(passwordEncoder.encode(data.password));
		userEntity = userRepository.saveAndFlush(userEntity);

		String orgDomain = domainService.getOrganizationDomainAndSubDir(userEntity.getOrganizationId());

		return new UserApiResponse(userEntity.getId(), orgDomain);
	}

	

	@Override
	public UserRepresentationObject getUserData(Long userId, Boolean isEmployee) {
		BaseUserEntity currentUser = securityService.getCurrentUser();
		
		if(!securityService.currentUserHasRole(NASNAV_ADMIN)) {
			return getUserRepresentationWithUserRoles(currentUser);
		}
		
		Boolean isEmp = ofNullable(isEmployee).orElse(false);
		Long requiredUserId = ofNullable(userId).orElse(currentUser.getId());		
				
		BaseUserEntity user = 
				commonUserRepo.findById(requiredUserId, isEmp)
							.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, U$0001, userId));
		
		return getUserRepresentationWithUserRoles(user);
	}



	@Override
	public RedirectView activateUserAccount(String token, String redirect)  {
		UserEntity user = userRepository.findByResetPasswordToken(token);

		checkUserActivation(user);
		validateActivationRedirectUrl(redirect, user.getOrganizationId());
		
		activateUserInDB(user);
		return redirectUser(securityService.login(user, false).getToken(), redirect);
	}




	private void activateUserInDB(UserEntity user) {
		user.setResetPasswordToken(null);
		user.setUserStatus(ACTIVATED.getValue());
		userRepository.save(user);
	}

	
	
	private RedirectView redirectUser(String authToken, String loginUrl) {
		//String loginUrl = buildOrgLoginPageUrl(orgId);
		
		RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();
		attributes.addAttribute("auth_token", authToken);
		
		RedirectView redirectView = new RedirectView();	
		redirectView.setUrl(loginUrl);
		redirectView.setAttributesMap(attributes);

		return redirectView;
	}





	private void checkUserActivation(UserEntity user) {
		if (user == null)
			throw new RuntimeBusinessException(UNAUTHORIZED, UXACTVX0006);

		userServicesHelper.checkResetPasswordTokenExpiry(user.getResetPasswordSentAt());

		if (!isUserDeactivated(user))
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$LOG$0008);
	}
	
	
	

	private UserRepresentationObject getUserRepresentationWithUserRoles(BaseUserEntity user) {
		UserRepresentationObject userRepObj = user.getRepresentation();
		userRepObj.setAddresses(getUserAddresses(userRepObj.getId()));
		userRepObj.setRoles(new HashSet<>(commonUserRepo.getUserRoles(user)));
		return userRepObj;
	}
	
	
	private List<AddressRepObj> getUserAddresses(Long userId){
		return addressRepo.findByUserId(userId);
	}

	
	
	private String[] getNullProperties(UserDTOs.EmployeeUserUpdatingObject userJson) {
		final BeanWrapper src = new BeanWrapperImpl(userJson);
		List<String> nullProperties = new ArrayList<>();
		java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
		for(java.beans.PropertyDescriptor pd : pds) {
			Object srcValue = src.getPropertyValue(pd.getName());
			if (srcValue == null) nullProperties.add(pd.getName());
		}
		String[] result = new String[nullProperties.size()];
		return nullProperties.toArray(result);
	}




	@Override
	public void resendActivationEmail(ActivationEmailResendDTO accountInfo) {
		String email = accountInfo.getEmail();
		Long orgId = accountInfo.getOrgId();
		BaseUserEntity baseUser = commonUserRepo.getByEmailAndOrganizationId(email, orgId);
		validateActivationEmailResend(accountInfo, baseUser);
		
		UserEntity user = (UserEntity)baseUser;
		if(isUserDeactivated(user) && isNull(user.getResetPasswordToken())) {
			generateResetPasswordToken(user);
			userRepository.save(user);
		}
		
		sendActivationMail(user, accountInfo.getRedirectUrl());
	}




	private void validateActivationEmailResend(ActivationEmailResendDTO accountInfo, BaseUserEntity user) {
		String email = accountInfo.getEmail();
		Long orgId = accountInfo.getOrgId();
		if(user == null || !(user instanceof UserEntity)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, UXACTVX0001, email, orgId);
		}else if(!isUserDeactivated(user)){
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, UXACTVX0002, email);
		}else if(resendRequestedTooSoon(accountInfo)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, UXACTVX0003, email);
		}			 		
	}




	private boolean resendRequestedTooSoon(ActivationEmailResendDTO accountInfo) {
		// TODO Auto-generated method stub
		return false;
	}




	@Override
	public UserApiResponse activateUserAccount(String token) {
		UserEntity user = userRepository.findByResetPasswordToken(token);

		checkUserActivation(user);
		
		activateUserInDB(user);
		return securityService.login(user, false);
	}


	@Override
	public void suspendUserAccount(Long id, Boolean suspend) {
		UserEntity user = getUserEntityById(id);
		UserStatus status = UserStatus.getUserStatus(user.getUserStatus());
		if (suspend) {
			if (status.equals(ACCOUNT_SUSPENDED)) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$STATUS$0001);
			}
			user.setUserStatus(ACCOUNT_SUSPENDED.getValue());
			userTokenRepo.deleteByUserEntity(user);
		} else {
			user.setUserStatus(ACTIVATED.getValue());
		}
		userRepository.save(user);
	}

	private UserEntity getUserEntityById(Long id) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		return userRepository.findByIdAndOrganizationId(id, orgId)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, U$0001, id));
	}
}
