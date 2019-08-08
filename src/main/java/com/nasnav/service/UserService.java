package com.nasnav.service;

import com.nasnav.dto.UserDTOs;
import com.nasnav.response.UserApiResponse;

public interface UserService extends CommonUserServiceInterface{


    /**
     * Register the passed User entity
     * by saving it to DB
     *
     * @param userJson UserJson string
     * @return User entity after saving to DB
     */
    UserApiResponse registerUser(UserDTOs.UserRegistrationObject userJson);

    public UserApiResponse updateUser(Long userId, String userToken, UserDTOs.EmployeeUserUpdatingObject userJson);
}
