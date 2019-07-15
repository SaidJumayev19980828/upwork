package com.nasnav.dao;

import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;


public interface EmployeeUserRepository extends JpaRepository<EmployeeUserEntity, Integer> {

    /**
     * Check if the passed authenticationToken already exist before or not
     *
     * @param authenticationToken to be checked
     * @return true if authenticationToken already exists
     */
    boolean existsByAuthenticationToken(String authenticationToken);

    /**
     * Find the user by email.
     *
     * @param email email and orgId of user.
     * @return EmployeeUserEntity
     */
    EmployeeUserEntity getByEmailAndOrganizationId(String email, Long orgId);


    
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
	EmployeeUserEntity getByResetPasswordToken(String resetPasswordToken);
}



