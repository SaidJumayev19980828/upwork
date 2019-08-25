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
    
    EmployeeUserEntity getByEmailAndOrganizationId(String email, Long orgId);
    
    EmployeeUserEntity getOneByEmail(String email);
    
    
	boolean existsByResetPasswordToken(String resetPasswordToken);

	
	EmployeeUserEntity getByResetPasswordToken(String resetPasswordToken);

	
	EmployeeUserEntity getByAuthenticationToken(String authToken);

	Optional<EmployeeUserEntity> findByAuthenticationToken(String authToken);

	EmployeeUserEntity getById(Long id);

	boolean existsByIdAndAuthenticationToken(Long userId, String authenticationToken);

	EmployeeUserEntity getByIdAndAuthenticationToken(Long userId, String authToken);
}



