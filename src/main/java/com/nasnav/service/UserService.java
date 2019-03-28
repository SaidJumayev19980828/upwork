package com.nasnav.service;

import com.nasnav.persistence.UserEntity;
import com.nasnav.response.ApiResponse;

public interface UserService {


    /**
     * Register the passed User entity
     * by saving it to DB
     *
     * @param userJson UserJson string
     * @return User entity after saving to DB
     */
    ApiResponse registerUser(String userJson);

    /**
     * Delete user entity by id
     *
     * @param userId To be used to delete user by
     */
    void deleteUser(Long userId);

    UserEntity findUserById(Long userId);

    /**
     * Load user by passed userId
     * @param userId
     * @return
     */
    UserEntity getUserById(Long userId);

    /**
     * update the passed user entity
     * @param userEntity user entity
     * @return user entity after update
     */
    UserEntity update(UserEntity userEntity);

    /**
     * Used to send the user a recovery token to reset his password
     *
     * @param email user email
     * @return ApiResponse object holding the status
     */
    ApiResponse sendEmailRecovery(String email);


    /**
     * change user password
     *
     * @param body json object containing token and new password
     * @return ApiResponse object holding the status
     */
    ApiResponse recoverUser(String body);

    /**
     * login user to system
     *
     * @param body json object containing email and password
     * @return ApiResponse object holding the status
     */
    ApiResponse login(String body);

}
