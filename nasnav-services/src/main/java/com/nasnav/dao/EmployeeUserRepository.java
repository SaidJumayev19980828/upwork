package com.nasnav.dao;

import com.nasnav.persistence.EmployeeUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface EmployeeUserRepository extends JpaRepository<EmployeeUserEntity, Long> {

	/**
	 * Check if the passed authenticationToken already exist before or not
	 *
	 * @param authenticationToken to be checked
	 * @return true if authenticationToken already exists
	 */
	boolean existsByAuthenticationToken(String authenticationToken);

	boolean existsByEmailIgnoreCase(String email);

	Optional<EmployeeUserEntity> findByIdAndOrganizationId(Long id, Long orgId);

	Optional<EmployeeUserEntity> findByEmailIgnoreCaseAndOrganizationId(String email, Long orgId);

	Optional<EmployeeUserEntity> findByEmailIgnoreCase(String email);

	EmployeeUserEntity getOneByEmail(String email);


	boolean existsByResetPasswordToken(String resetPasswordToken);


	Optional<EmployeeUserEntity> getByResetPasswordToken(String resetPasswordToken);

	EmployeeUserEntity getById(Long id);

	List<EmployeeUserEntity> findByOrganizationId(Long orgId);

	List<EmployeeUserEntity> findByShopId(Long shopId);

	List<EmployeeUserEntity> findByIdIn(List<Long> employeesIds);

	EmployeeUserEntity getByEmailIgnoreCase(String email);

	@Query("select e from EmployeeUserEntity e " +
			" left join e.roles r where e.id = :id and e.organizationId = :orgId and r.name in :roles")
	Optional<EmployeeUserEntity> findByIdAndOrgIdAndRoles(@Param("id") Long id,
										@Param("orgId") Long orgId,
										@Param("roles") Set<String> roles);
}



