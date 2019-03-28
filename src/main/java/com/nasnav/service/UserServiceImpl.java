package com.nasnav.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.constatnts.EmailConstants;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dao.UserRepository;
import com.nasnav.exceptions.EntityValidationException;
import com.nasnav.persistence.EntityUtils;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.ApiResponse;
import com.nasnav.response.ApiResponseBuilder;
import com.nasnav.response.ResponseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {


    private UserRepository userRepository;
    private MailService mailService;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           MailService mailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ApiResponse registerUser(String userJson) {
        UserEntity userEntity = this.doRegisterUser(userJson);
        return ApiResponse.createStatusApiResponse(userEntity.getId(), Arrays.asList(ResponseStatus.NEED_ACTIVATION, ResponseStatus.ACTIVATION_SENT));
    }


    /**
     * Register new user in DB
     *
     * @param userJson JSON object for the new user to be registered.
     * @return UserEntity object after saving
     */
    private UserEntity doRegisterUser(String userJson) {
        // parse Json to User entity.
        UserEntity user = this.createUserFromJson(userJson);
        // validate user entity against business rules.
        this.validateBusinessRules(user);
        // save to DB.
        return userRepository.save(user);
    }

    /**
     * map Json Request to User Entity
     *
     * @param userJson Json String holding user data
     * @return mapped User entity from Json request
     */
    private UserEntity createUserFromJson(String userJson) {
        // convert JSON request to user object
        // avoid creating new DTO as it is not needed now,
        try {
            UserEntity userEntity = new ObjectMapper().readValue(userJson, UserEntity.class);
            // init required fields to avoid not-null constraint
            userEntity.setEncPassword(EntityConstants.INITIAL_PASSWORD);
            return userEntity;
        } catch (IOException e) {
            throw new EntityValidationException("Invalid Json Request ",
                    ApiResponse.createStatusApiResponse(Arrays.asList(ResponseStatus.INVALID_NAME, ResponseStatus.INVALID_EMAIL)), HttpStatus.OK
            );
        }

    }

    /**
     * validateBusinessRules passed user entity against business rules
     *
     * @param user User entity to be validated
     */
    private void validateBusinessRules(UserEntity user) {
        this.validateNameAndEmail(user);
        this.checkEmailExistence(user);
    }


    /**
     * called pre save user to
     * validateBusinessRules fields of user entity
     *
     * @param user User entity to be processed before registering
     */
    private void validateNameAndEmail(UserEntity user) {
        List<ResponseStatus> responseStatusList = new ArrayList<>();
        if (!EntityUtils.validateName(user.getName())) {
            responseStatusList.add(ResponseStatus.INVALID_NAME);
        }
        if (!EntityUtils.validateEmail(user.getEmail())) {
            responseStatusList.add(ResponseStatus.INVALID_EMAIL);
        }
        if (!responseStatusList.isEmpty()) {
            throw new EntityValidationException("Invalid User Entity, responseStatusList", ApiResponse.createStatusApiResponse(responseStatusList), HttpStatus.OK);
        }
    }

    /**
     * Ensure that the new email is not registered to another user
     *
     * @param user User entity containing email to be checked
     */
    private void checkEmailExistence(UserEntity user) {
        boolean emailAlreadyExists = userRepository.existsByEmail(user.getEmail());
        if (emailAlreadyExists) {
            throw new EntityValidationException("Invalid User Entity " + ResponseStatus.EMAIL_EXISTS.name(),
                    ApiResponse.createStatusApiResponse(Collections.singletonList(ResponseStatus.EMAIL_EXISTS)), HttpStatus.OK);
        }
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

	@Override
	public UserEntity findUserById(Long userId) {
		Optional<UserEntity> optional =  userRepository.findById(userId);
		return optional.isPresent() ? optional.get() : null;
		
	}

    @Override
    public UserEntity getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public UserEntity update(UserEntity userEntity) {
        return userRepository.saveAndFlush(userEntity);
    }

    @Override
    public ApiResponse sendEmailRecovery(String email) {
        UserEntity userEntity = getUserEntityByEmail(email);
        userEntity = generateResetPasswordToken(userEntity);
        return sendRecoveryMail(userEntity);
    }

    /**
     * Get user by passed email
     *
     * @param email user entity email
     * @return user entity
     */
    private UserEntity getUserEntityByEmail(String email) {
        // first ensure that email is valid
        if (!EntityUtils.validateEmail(email)) {
            ApiResponse apiResponse = ApiResponse.createMessagesApiResponse(false, Collections.singletonList(ResponseStatus.INVALID_EMAIL));
            throw new EntityValidationException("INVALID_EMAIL ", apiResponse, HttpStatus.TOO_MANY_REQUESTS);
        }
        // load user entity by email
        UserEntity userEntity = this.userRepository.getByEmail(email);
        if (EntityUtils.isBlankOrNull(userEntity)) {
            ApiResponse apiResponse = ApiResponse.createMessagesApiResponse(false, Collections.singletonList(ResponseStatus.EMAIL_NOT_EXIST));
            throw new EntityValidationException("EMAIL_NOT_EXIST ", apiResponse, HttpStatus.TOO_MANY_REQUESTS);
        }
        return userEntity;
    }

    /**
     * Generate ResetPasswordToken and assign it to passed user entity
     *
     * @param userEntity user entity
     * @return user entity after generating ResetPasswordToken and updating entity.
     */
    private UserEntity generateResetPasswordToken(UserEntity userEntity) {
        String generatedToken = generateResetPasswordToken(EntityConstants.TOKEN_LENGTH);
        userEntity.setResetPasswordToken(generatedToken);
        userEntity.setResetPasswordSentAt(LocalDateTime.now());
        return userRepository.saveAndFlush(userEntity);
    }

    /**
     * Send An Email to user.
     *
     * @param userEntity user entity
     * @return ApiResponse representing the status of sending email.
     */
    private ApiResponse sendRecoveryMail(UserEntity userEntity) {
        ApiResponse apiResponse = new ApiResponse();
        try {
            // create parameter map to replace parameter by actual UserEntity data.
            Map<String, String> parametersMap = new HashMap<>();
            parametersMap.put(EmailConstants.USERNAME_PARAMETER, userEntity.getName());
            parametersMap.put(EmailConstants.CHANGE_PASSWORD_URL_PARAMETER,
                    EmailConstants.CHANGE_PASSWORD_URL.concat(userEntity.getResetPasswordToken()));
            // send Recovery mail to user
            this.mailService.send(userEntity.getEmail(), EmailConstants.CHANGE_PASSWORD_EMAIL_SUBJECT,
                    EmailConstants.CHANGE_PASSWORD_EMAIL_TEMPLATE, parametersMap);
            // set success to true after sending mail.
            apiResponse.setSuccess(true);
        } catch (Exception e) {
            apiResponse.setSuccess(false);
            apiResponse.setMessages(Collections.singletonList(e.getMessage()));
            throw new EntityValidationException("Could not send Email ", apiResponse, HttpStatus.TOO_MANY_REQUESTS);
        }
        return apiResponse;
    }


    /**
     * generate new ResetPasswordToken and ensure that
     * this ResetPasswordToken is never used before.
     *
     * @param tokenLength length of generated ResetPasswordToken
     * @return unique generated ResetPasswordToken.
     */
    private String generateResetPasswordToken(int tokenLength) {
        String generatedToken = EntityUtils.generateToken(tokenLength);
        boolean existsByToken = userRepository.existsByResetPasswordToken(generatedToken);
        if (existsByToken) {
            return reGenerateResetPasswordToken(tokenLength);
        }
        return generatedToken;
    }

    /**
     * regenerate ResetPasswordToken and if token already exists,
     * make recursive call until generating new ResetPasswordToken.
     *
     * @param tokenLength length of generated ResetPasswordToken
     * @return unique generated ResetPasswordToken.
     */
    private String reGenerateResetPasswordToken(int tokenLength) {
        String generatedToken = EntityUtils.generateToken(tokenLength);
        boolean existsByToken = userRepository.existsByResetPasswordToken(generatedToken);
        if (existsByToken) {
            return reGenerateResetPasswordToken(tokenLength);
        }
        return generatedToken;
    }


    @Override
    public ApiResponse recoverUser(String body) {
        String resetPasswordToken;
        String newPassword;
        try {
            JsonNode jsonBody = new ObjectMapper().readTree(body);
            resetPasswordToken = jsonBody.get("token").asText();
            newPassword = jsonBody.get("password").asText();
        } catch (Exception e) {
            ApiResponse failedLoginResponse = EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INVALID_PARAMETERS));
            throw new EntityValidationException("INVALID_CREDENTIALS ", failedLoginResponse, HttpStatus.NOT_ACCEPTABLE);
        }
        UserEntity userEntity = this.changeUserPassword(resetPasswordToken, newPassword);
        return ApiResponse.createStatusApiResponse(userEntity.getId(), null);
    }

    /**
     * update user EncPassword field.
     *
     * @param resetPasswordToken resetPasswordToken sent to user to change his password
     * @param newPassword        the new password to be set.
     * @return user entity after updating his EncPassword
     */
    private UserEntity changeUserPassword(String resetPasswordToken, String newPassword) {
        validateNewPassword(newPassword);
        UserEntity userEntity = userRepository.getByResetPasswordToken(resetPasswordToken);
        if (EntityUtils.isNotBlankOrNull(userEntity)) {
            // if resetPasswordToken is not active, throw exception for invalid resetPasswordToken
            checkResetPasswordTokenExpiry(userEntity);
            userEntity.setResetPasswordToken(null);
            userEntity.setResetPasswordSentAt(null);
            userEntity.setEncPassword(passwordEncoder.encode(newPassword));
            return userRepository.saveAndFlush(userEntity);
        } else {
            throw new EntityValidationException("INVALID_TOKEN  ",
                    ApiResponse.createStatusApiResponse(Collections.singletonList(ResponseStatus.INVALID_TOKEN)),
                    HttpStatus.NOT_ACCEPTABLE);
        }
    }

    private void validateNewPassword(String newPassword) {
        if (EntityUtils.isBlankOrNull(newPassword) ||
                newPassword.length() > EntityConstants.PASSWORD_MAX_LENGTH
                || newPassword.length() < EntityConstants.PASSWORD_MIN_LENGTH) {
            throw new EntityValidationException("INVALID_PASSWORD  ",
                    ApiResponse.createStatusApiResponse(Collections.singletonList(ResponseStatus.INVALID_PASSWORD)),
                    HttpStatus.NOT_ACCEPTABLE);
        }
    }

    /**
     * Ensure that ResetPasswordToken is not expired.
     *
     * @param userEntity user entity
     */
    private void checkResetPasswordTokenExpiry(UserEntity userEntity) {
        LocalDateTime resetPasswordSentAt = userEntity.getResetPasswordSentAt();
        LocalDateTime tokenExpiryDate = resetPasswordSentAt.plusHours(EntityConstants.TOKEN_VALIDITY);
        if (LocalDateTime.now().isAfter(tokenExpiryDate)) {
            throw new EntityValidationException("EXPIRED_TOKEN  ",
                    ApiResponse.createStatusApiResponse(Collections.singletonList(ResponseStatus.EXPIRED_TOKEN)),
                    HttpStatus.NOT_ACCEPTABLE);
        }
    }


    @Override
    public ApiResponse login(String body) {
        String email;
        String password;
        try {
            JsonNode jsonBody = new ObjectMapper().readTree(body);
            email = jsonBody.get("email").asText();
            password = jsonBody.get("password").asText();
        } catch (Exception e) {
            ApiResponse failedLoginResponse = EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INVALID_PARAMETERS));
            throw new EntityValidationException("INVALID_PARAMETERS ", failedLoginResponse, HttpStatus.NOT_ACCEPTABLE);
        }
        UserEntity userEntity = this.userRepository.getByEmail(email);
        if (userEntity != null) {
            // check if account needs activation
            boolean accountNeedActivation = isUserNeedActivation(userEntity);
            if (accountNeedActivation) {
                ApiResponse failedLoginResponse = EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.NEED_ACTIVATION));
                throw new EntityValidationException("NEED_ACTIVATION ", failedLoginResponse, HttpStatus.LOCKED);
            }
            // ensure that password matched
            boolean passwordMatched = passwordEncoder.matches(password, userEntity.getEncPassword());
            if (passwordMatched) {
                // check if account is locked
                boolean accountIsLocked = isAccountLocked(userEntity);
                if (accountIsLocked) {
                    ApiResponse failedLoginResponse = EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.ACCOUNT_SUSPENDED));
                    throw new EntityValidationException("ACCOUNT_SUSPENDED ", failedLoginResponse, HttpStatus.LOCKED);
                }
                // generate new AuthenticationToken and perform post login updates
                userEntity=updatePostLogin(userEntity);
                return createSuccessLoginResponse(userEntity);
            }
        }
        ApiResponse failedLoginResponse = EntityUtils.createFailedLoginResponse(Collections.singletonList(ResponseStatus.INVALID_CREDENTIALS));
        throw new EntityValidationException("INVALID_CREDENTIALS ", failedLoginResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Generate new AuthenticationToken and perform post login updates.
     *
     * @param userEntity to be udpated
     * @return userEntity
     */
    private UserEntity updatePostLogin(UserEntity userEntity){
        LocalDateTime currentSignInDate = userEntity.getCurrentSignInDate();
        userEntity.setLastSignInDate(currentSignInDate);
        userEntity.setCurrentSignInDate(LocalDateTime.now());
        userEntity.setAuthenticationToken(generateAuthenticationToken(EntityConstants.TOKEN_LENGTH));
        return userRepository.saveAndFlush(userEntity);
    }


    /**
     * generate new AuthenticationToken and ensure that
     * this AuthenticationToken is never used before.
     *
     * @param tokenLength length of generated AuthenticationToken
     * @return unique generated AuthenticationToken.
     */
    private String generateAuthenticationToken(int tokenLength) {
        String generatedToken = EntityUtils.generateToken(tokenLength);
        boolean existsByToken = userRepository.existsByAuthenticationToken(generatedToken);
        if (existsByToken) {
            return reGenerateAuthenticationToken(tokenLength);
        }
        return generatedToken;
    }

    /**
     * regenerate AuthenticationToken and if token already exists,
     * make recursive call until generating new AuthenticationToken.
     *
     * @param tokenLength length of generated AuthenticationToken
     * @return unique generated AuthenticationToken.
     */
    private String reGenerateAuthenticationToken(int tokenLength) {
        String generatedToken = EntityUtils.generateToken(tokenLength);
        boolean existsByToken = userRepository.existsByAuthenticationToken(generatedToken);
        if (existsByToken) {
            return reGenerateAuthenticationToken(tokenLength);
        }
        return generatedToken;
    }

    /**
     * Check if passed user entity's account is locked.
     *
     * @param userEntity User entity to be checked.
     * @return true if current user entity's account is locked.
     */
    private boolean isAccountLocked(UserEntity userEntity) {
        //TODO : change implementation later
        return false;
    }

    /**
     * Check if passed user entity's account needs activation.
     *
     * @param userEntity User entity to be checked.
     * @return true if current user entity's account needs activation.
     */
    private boolean isUserNeedActivation(UserEntity userEntity) {
        String encPassword = userEntity.getEncPassword();
        return EntityUtils.isBlankOrNull(encPassword) || EntityConstants.INITIAL_PASSWORD.equals(encPassword);
    }

    /**
     * Create success login Api response
     *
     * @param userEntity success user entity
     * @return ApiResponse
     */
    private ApiResponse createSuccessLoginResponse(UserEntity userEntity) {
        return new ApiResponseBuilder().setSuccess(true).setEntityId(userEntity.getId())
                .setToken(userEntity.getAuthenticationToken()).setRoles(getUserRoles())
                .setOrganizationId(0L).setStoreId(0L).build();
    }

    /**
     * Get list of roles for User entity
     *
     * @return Role list
     */
    private List<String> getUserRoles() {
        // for now, return default role which is Customer
        return Collections.singletonList(EntityConstants.Roles.CUSTOMER.getValue());
    }

}
