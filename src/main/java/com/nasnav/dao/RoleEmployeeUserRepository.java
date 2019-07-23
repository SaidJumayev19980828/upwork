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

    boolean existsByEmployeeUserIdAndRoleId(Integer employeeUserId, Integer roleId);

    @Transactional
    void deleteByEmployeeUserId(Integer employeeUserId);
}
