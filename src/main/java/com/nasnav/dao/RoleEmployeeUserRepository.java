package com.nasnav.dao;

import com.nasnav.persistence.RoleEmployeeUser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RoleEmployeeUserRepository extends JpaRepository<RoleEmployeeUser, Integer> {

    @Query("select roleEmp from RoleEmployeeUser roleEmp where roleEmp.employeeUserId = :employeeUserId")
    List<RoleEmployeeUser> findRoleEmployeeUsersById(@Param("employeeUserId") Integer employeeUserId);

    boolean existsByEmployeeUserIdAndRoleId(Long employeeUserId, Integer roleId);

    @Transactional
    void deleteByEmployeeUserId(Long employeeUserId);

    @Query("select roleEmp.employeeUserId from RoleEmployeeUser roleEmp where roleEmp.roleId in (select id from Roles roles where roles.name in :rolesNames)")
    List<Long> findEmployeeUsersIds(@Param("rolesNames") List<String> rolesNames);
    
    
    @Query("SELECT emp.email "
    		+ " FROM RoleEmployeeUser roleEmp "
    		+ " LEFT JOIN EmployeeUserEntity emp on roleEmp.employeeUserId = emp.id "
    		+ " WHERE roleEmp.roleId = (select id from Roles roles where roles.name = :roleName) "
    		+ "   AND emp.shopId = :shopId")
    List<String> findEmailOfEmployeeWithRoleAndShop(@Param("roleName") String roleName, @Param("shopId")Long shopId);
    
    
    @Query("SELECT emp.email "
    		+ " FROM RoleEmployeeUser roleEmp "
    		+ " LEFT JOIN EmployeeUserEntity emp on roleEmp.employeeUserId = emp.id "
    		+ " WHERE roleEmp.roleId = (select id from Roles roles where roles.name = :roleName) "
    		+ "   AND emp.organizationId = :orgId")
    List<String> findEmailOfEmployeeWithRoleAndOrganization(@Param("roleName") String roleName, @Param("orgId")Long orgId);
}
