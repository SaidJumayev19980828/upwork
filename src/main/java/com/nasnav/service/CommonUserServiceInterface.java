package com.nasnav.service;

import com.nasnav.dto.UserDTOs;
import com.nasnav.persistence.DefaultBusinessEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.UserApiResponse;

public interface CommonUserServiceInterface {

    /**
     * Delete user entity by id
     *
     * @param userId To be used to delete user by
     */
    void deleteUser(Long userId);

    DefaultBusinessEntity<?> findUserById(Long userId);

    /**
     * Load user by passed userId
     * @param userId
     * @return
     */
    DefaultBusinessEntity<?> getUserById(Long userId);

    /**
     * update the passed user entity
     * @param userEntity user entity
     * @return user entity after update
     */
    DefaultBusinessEntity<?> update(DefaultBusinessEntity<?> userEntity);

    /**
     * Used to send the user a recovery token to reset his password
     *
     * @param email user email
     * @return UserApiResponse object holding the status
     */
    UserApiResponse sendEmailRecovery(String email);


    /**
     * change user password
     *
     * @param body json object containing token and new password
     * @return UserApiResponse object holding the status
     */
    UserApiResponse recoverUser(UserDTOs.PasswordResetObject  body);

    /**
     * login user to system
     *
     * @param body json object containing email and password
     * @return UserApiResponse object holding the status
     */
    UserApiResponse login(UserDTOs.UserLoginObject body);

    /**
     * login user to system
     *
     * @param userId user's ID
     * @param authToken token generated on log-in
     * @return true if user is authenticated (token is valid)
     */
    boolean checkAuthToken(long userId, String authToken);
}
