package com.nasnav.dao;

import com.nasnav.persistence.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    @Query("select r from Role r left join r.employees e where e.id = :employeeUserId")
    List<Role> getRolesOfEmployeeUser(@Param("employeeUserId") Long employeeUserId);

    // get role by its name
    Role findByName(String name);
}
