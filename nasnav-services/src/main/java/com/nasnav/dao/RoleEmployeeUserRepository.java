package com.nasnav.dao;

import com.nasnav.persistence.RoleEmployeeUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RoleEmployeeUserRepository extends JpaRepository<RoleEmployeeUser, Integer> {


    @Transactional
    void deleteByEmployee_Id(Long employeeUserId);
    
    @Query("SELECT e.email " +
            " from RoleEmployeeUser roleEmp " +
            " left join roleEmp.employee e " +
            " left join roleEmp.role r " +
            " where r.name = :roleName and e.shopId = :shopId AND e.userStatus = 201")
    List<String> findEmailOfEmployeeWithRoleAndShop(@Param("roleName") String roleName, @Param("shopId")Long shopId);
    
    
    @Query("SELECT e.email " +
            " from RoleEmployeeUser roleEmp " +
            " left join roleEmp.employee e " +
            " left join roleEmp.role r " +
            " where r.name = :roleName and e.organizationId = :orgId AND e.userStatus = 201")
    List<String> findEmailOfEmployeeWithRoleAndOrganization(@Param("roleName") String roleName, @Param("orgId")Long orgId);
}
