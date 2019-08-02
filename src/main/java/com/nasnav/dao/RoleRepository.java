package com.nasnav.dao;

import com.nasnav.persistence.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    /**
     * Gel list of roles assigned to passed employeeUserId
     *
     * @param employeeUserId Id of employee user entity.
     * @return list or roles
     */
    @Query("from Role  role where role.id in (select empRole.roleId from  RoleEmployeeUser empRole where empRole.employeeUserId = :employeeUserId)")
    List<Role> getRolesOfEmployeeUser(@Param("employeeUserId") Long employeeUserId);

    // get all existing roles
    List<Role> findAll();

    // get role by its name
    Role findByName(String name);
}
