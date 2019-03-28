package com.nasnav.dao;

import com.nasnav.persistence.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<UserEntity, Long> {
    /**
     * Ensure that the new email is not registered to another user
     *
     * @param email email to be checked
     * @return true if the passed email parameter exists
     */
    boolean existsByEmail(String email);
    
    Optional<UserEntity> findById(Long id);

    /**
     * Get UserEntity by passed email.
     *
     * @param email email
     * @return UserEntity
     */
    UserEntity getByEmail(String email);


    /**
     * Check if the passed resetPasswordToken already exist before or not.
     *
     * @param resetPasswordToken to be checked
     * @return true if esetPasswordToken already exists
     */
    boolean existsByResetPasswordToken(String resetPasswordToken);

    /**
     * Get userEntity by resetPasswordToken.
     *
     * @param resetPasswordToken
     * @return UserEntity
     */
    UserEntity getByResetPasswordToken(String resetPasswordToken);

    /**
     * Check if the passed authenticationToken already exist before or not.
     *
     * @param authenticationToken to be checked
     * @return true if authenticationToken already exists
     */
    boolean existsByAuthenticationToken(String authenticationToken);
}



