package com.nasnav.yeshtery.services.interfaces;

import com.nasnav.dto.UserDTOs;
import com.nasnav.persistence.BaseYeshteryUserEntity;
import com.nasnav.yeshtery.response.YeshteryUserApiResponse;

public interface CommonYeshteryUserServiceInterface {

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
    BaseYeshteryUserEntity getUserById(Long userId);

    /**
     * update the passed user entity
     * @param userEntity user entity
     * @return user entity after update
     */
    BaseYeshteryUserEntity update(BaseYeshteryUserEntity userEntity);

    /**
     * Used to send the user a recovery token to reset his password
     *
     * @param email user email
     * @param orgId user organization id
     * @return UserApiResponse object holding the status
     */
    void sendEmailRecovery(String email, Long orgId);


    /**
     * change user password
     *
     * @param body json object containing token and new password
     * @return UserApiResponse object holding the status
     */
    YeshteryUserApiResponse recoverUser(UserDTOs.PasswordResetObject  body);

    YeshteryUserApiResponse recoverYeshteryUser(UserDTOs.PasswordResetObject  body);

    Boolean isUserDeactivated(BaseYeshteryUserEntity user);
}
