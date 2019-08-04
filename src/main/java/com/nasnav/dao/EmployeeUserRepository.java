package com.nasnav.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.EmployeeUserEntity;


public interface EmployeeUserRepository extends JpaRepository<EmployeeUserEntity, Long> {

    /**
     * Check if the passed authenticationToken already exist before or not
     *
     * @param authenticationToken to be checked
     * @return true if authenticationToken already exists
     */
    boolean existsByAuthenticationToken(String authenticationToken);
    /**
     * Find the user by email and orgId.
     *
     * @param email email of user.
	 * @param orgId Organization id of the user
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

	/**
	 * Get userEntity by AuthenticationToken.
	 *
	 * @param authToken
	 * @return UserEntity
	 */
	EmployeeUserEntity getByAuthenticationToken(String authToken);

	Optional<EmployeeUserEntity> findByAuthenticationToken(String authToken);

	EmployeeUserEntity getById(Long id);

	boolean existsByIdAndAuthenticationToken(Long userId, String authenticationToken);
	
	
}



