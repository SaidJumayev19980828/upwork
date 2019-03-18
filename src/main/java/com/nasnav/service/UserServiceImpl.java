package com.nasnav.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.persistence.EntityConstants;
import com.nasnav.persistence.User;
import com.nasnav.dao.UserRepositoryI;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.exception.EntityValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@Service
public class UserServiceImpl implements UserServiceI {

    private UserRepositoryI userRepositoryI;

    @Autowired
    public UserServiceImpl(UserRepositoryI userRepositoryI) {
        this.userRepositoryI = userRepositoryI;
    }

    @Override
    public User registerUser(String userJson) {
        // parse Json to User entity.
        User user = this.createUserFromJson(userJson);
        // set required business before register
        this.preRegisterUser(user);
        // validate user entity
        this.validate(user);
        // save to DB
        return userRepositoryI.save(user);
    }

    /**
     * map Json Request to User Entity
     * @param userJson Json String holding user data
     * @return mapped User entity from Json request
     */
    private User createUserFromJson(String userJson) {
        // convert JSON request to user object
        // avoid creating new DTO as it is not needed now,
        try {
            return new ObjectMapper().readValue(userJson, User.class);
        } catch (IOException e) {
            throw new EntityValidationException("Invalid Json Request",
                    Arrays.asList(ResponseStatus.INVALID_NAME, ResponseStatus.INVALID_EMAIL));
        }

    }


    /**
     * called pre register user to
     * set required business before register
     *
     * @param user User entity to be processed before registering
     */
    private void preRegisterUser(User user) {
        // set id to null
        user.setId(null);
        // set status of user to require activation
        user.setStatus(EntityConstants.UserStatus.REQUIRE_ACTIVATION.getValue());

    }

    /**
     * validate passed user entity against business rules
     *
     * @param user User entity to be validated
     */
    private void validate(User user) {
        this.checkEmailExistence(user);
    }

    /**
     * Ensure that the new email is not registered to another user
     *
     * @param user User entity containing email to be checked
     */
    private void checkEmailExistence(User user) {
        boolean emailAlreadyExists = userRepositoryI.existsByEmail(user.getEmail());
        if (emailAlreadyExists) {
            throw new EntityValidationException(ResponseStatus.EMAIL_EXISTS.getValue(),
                    Collections.singletonList(ResponseStatus.EMAIL_EXISTS));
        }
    }

    @Override
    public void deleteUser(Long userId) {
        userRepositoryI.deleteById(userId);
    }
}
