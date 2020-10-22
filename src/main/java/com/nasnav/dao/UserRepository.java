package com.nasnav.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasnav.persistence.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

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
	Optional<UserEntity> getByResetPasswordToken(String resetPasswordToken);

	/**
	 * Check if the passed authenticationToken already exist before or not.
	 *
	 * @param authenticationToken to be checked
	 * @return true if authenticationToken already exists
	 */
	boolean existsByAuthenticationToken(String authenticationToken);
	
	Optional<UserEntity> findByAuthenticationToken(String authToken);

	UserEntity findByResetPasswordToken(String token);

	UserEntity getByEmailIgnoreCaseAndOrganizationId(String email, Long orgId);

	boolean existsByEmailIgnoreCaseAndOrganizationId(String email, Long orgId);

	Optional<UserEntity> findByIdAndOrganizationId(Long id, Long orgId);
}
