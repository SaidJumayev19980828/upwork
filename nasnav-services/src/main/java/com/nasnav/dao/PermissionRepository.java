package com.nasnav.dao;

import com.nasnav.persistence.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Permission findByName(String name);

    boolean existsByName(String name);

    @Query("select p from Permission p join p.roles r join r.employees reu where reu.id = :employeeUserId")
    List<Permission> findByUserIdViaRole(@Param("employeeUserId") Long userId);

    @Query("select p from Permission p join p.services s where s.id IN (select os.serviceId from OrganizationServicesEntity os where os.orgId = :organizationId and os.enabled = true)")
    List<Permission> findByOrganizationId(@Param("organizationId") Long organizationId);
}
