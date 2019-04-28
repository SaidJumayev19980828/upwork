package com.nasnav.dao;

import com.nasnav.persistence.RoleEmployeeUser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoleEmployeeUserRepository extends JpaRepository<RoleEmployeeUser, Integer> {

    @Query("select roleEmp from RoleEmployeeUser roleEmp where roleEmp.employeeUserId = :employeeUserId")
    List<RoleEmployeeUser> findRoleEmployeeUsersById(Integer employeeUserId);
}
