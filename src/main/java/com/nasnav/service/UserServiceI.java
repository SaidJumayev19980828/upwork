package com.nasnav.service;


import com.nasnav.persistence.User;

public interface UserServiceI {

    /**
     * Register the passed User entity
     * by saving it to DB
     *
     * @param userJson UserJson string
     * @return User entity after saving to DB
     */
    User registerUser(String userJson);

    /**
     * Delete user entity be id
     *
     * @param userId Entity id used for delete
     */
    void deleteUser(Long userId);
}
