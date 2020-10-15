package com.nasnav.service;

import com.nasnav.dto.UserDTOs;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.response.UserApiResponse;

public interface CommonUserServiceInterface {

    String DEACTIVATION_CODE = "0000-0000-0000-0000";

	/**
     * Delete user entity by id
     *
     * @param userId To be used to delete user by
     */
    void deleteUser(Long userId);

    /**
     * Load user by passed userId
     * @param userId
     * @return
     */
    BaseUserEntity getUserById(Long userId);

    /**
     * update the passed user entity
     * @param userEntity user entity
     * @return user entity after update
     */
    BaseUserEntity update(BaseUserEntity userEntity);

    /**
     * Used to send the user a recovery token to reset his password
     *
     * @param email user email
     * @param orgId user organization id
     * @return UserApiResponse object holding the status
     */
    UserApiResponse sendEmailRecovery(String email, Long orgId);


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
     * @param userId user's ID
     * @param authToken token generated on log-in
     * @return true if user is authenticated (token is valid)
     */
    boolean checkAuthToken(Long userId, String authToken);

	Boolean isUserDeactivated(BaseUserEntity user);
}
