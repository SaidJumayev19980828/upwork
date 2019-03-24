package com.nasnav.service;

import java.util.Optional;

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

}
