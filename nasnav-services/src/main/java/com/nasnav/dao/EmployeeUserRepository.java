package com.nasnav.dao;

import com.nasnav.persistence.EmployeeUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface EmployeeUserRepository extends JpaRepository<EmployeeUserEntity, Long> {

	/**
	 * Check if the passed authenticationToken already exist before or not
	 *
	 * @param authenticationToken to be checked
	 * @return true if authenticationToken already exists
	 */
	boolean existsByAuthenticationToken(String authenticationToken);

	boolean existsByEmailIgnoreCaseAndOrganizationId(String email, Long orgId);

	boolean existsByEmailIgnoreCase(String email);

	Optional<EmployeeUserEntity> findByIdAndOrganizationId(Long id, Long orgId);

	Optional<EmployeeUserEntity> findByEmailIgnoreCaseAndOrganizationId(String email, Long orgId);

	EmployeeUserEntity getOneByEmail(String email);


	boolean existsByResetPasswordToken(String resetPasswordToken);


	Optional<EmployeeUserEntity> getByResetPasswordToken(String resetPasswordToken);

	EmployeeUserEntity getById(Long id);

	List<EmployeeUserEntity> findByOrganizationId(Long orgId);

	List<EmployeeUserEntity> findByShopId(Long shopId);

	List<EmployeeUserEntity> findByOrganizationIdAndShopId(Long orgId, Long shopId);

	List<EmployeeUserEntity> findByIdIn(List<Long> employeesIds);

	List<EmployeeUserEntity> findByOrganizationIdAndIdIn(Long orgId, List<Long> employeesIds);

	List<EmployeeUserEntity> findByShopIdAndIdIn(Long shopId, List<Long> employeesIds);

	List<EmployeeUserEntity> findByOrganizationIdAndShopIdAndIdIn(Long orgId, Long shopId, List<Long> employeesIds);

	EmployeeUserEntity getByEmailIgnoreCase(String email);

}



