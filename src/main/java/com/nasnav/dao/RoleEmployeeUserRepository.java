package com.nasnav.dao;

import com.nasnav.persistence.RoleEmployeeUser;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleEmployeeUserRepository extends JpaRepository<RoleEmployeeUser, Integer> {
    
}
