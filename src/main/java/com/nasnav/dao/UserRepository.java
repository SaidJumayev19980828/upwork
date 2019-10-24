package com.nasnav.dao;

import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.Role;
import com.nasnav.persistence.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
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
	 * Get UserEntity by passed email and orgId.
	 *
	 * @param email email
	 * @param orgId orgId
	 * @return UserEntity
	 */
	UserEntity getByEmailAndOrganizationId(String email, Long orgId);

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

	boolean existsByIdAndAuthenticationToken(long id, String authenticationToken);

	UserEntity getByIdAndAuthenticationToken(Long userId, String authToken);

	/**
	 * check if the created user has the same email and org_id
	 *
	 * @param userEmail
	 * @param userOrgId
	 * @return UserEntity if a user exists having both email and org_id
	 */
	@Query("select users from  UserEntity users where users.email = :userEmail and users.organizationId = :userOrgId")
	UserEntity existsByEmailAndOrgId(@Param("userEmail") String userEmail, @Param("userOrgId") Long userOrgId);
	
	
	
	Optional<UserEntity> findByAuthenticationToken(String authToken);

	UserEntity getByEmailIgnoreCaseAndOrganizationId(String email, Long orgId);

	UserEntity getByEmailIgnoreCase(String email);
}
