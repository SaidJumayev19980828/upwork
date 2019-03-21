package com.nasnav.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.UserRepository;
import com.nasnav.persistence.EntityConstants;
import com.nasnav.persistence.EntityUtils;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.ApiResponse;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.exception.EntityValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {


    private UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public ApiResponse registerUser(String userJson) {
        UserEntity userEntity = this.doRegisterUser(userJson);
        return new ApiResponse(userEntity.getId(), Arrays.asList(ResponseStatus.NEED_ACTIVATION, ResponseStatus.ACTIVATION_SENT));
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
            throw new EntityValidationException("Invalid Json Request",
                    Arrays.asList(ResponseStatus.INVALID_NAME, ResponseStatus.INVALID_EMAIL));
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
            throw new EntityValidationException("Invalid User Entity, responseStatusList", responseStatusList);
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
            throw new EntityValidationException("Invalid User Entity" + ResponseStatus.EMAIL_EXISTS.name(),
                    Collections.singletonList(ResponseStatus.EMAIL_EXISTS));
        }
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
