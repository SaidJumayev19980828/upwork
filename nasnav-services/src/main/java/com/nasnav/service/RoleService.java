package com.nasnav.service;

import com.nasnav.dto.RoleDto;
import com.nasnav.enumerations.Roles;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.Role;
import com.nasnav.persistence.yeshtery.BaseYeshteryUserEntity;

import java.util.List;

public interface RoleService {
    List<Role> getRolesOfEmployeeUser(Long employeeUserId);

    List<Roles> getRolesEnumOfEmployeeUser(Long employeeUserId);

    List<String> getRolesNamesOfEmployeeUser(Long employeeUserId);

    Roles getEmployeeHighestRole(Long userId);

    void createRoles(List<String> rolesList, EmployeeUserEntity employee, Long orgId);

    List<String> getAllRoleNames();

    boolean roleCannotManageUsers(Long currentUserId);

    boolean employeeHasRoleOrHigher(EmployeeUserEntity employee, Roles requiredRole);

    boolean hasInsufficientLevel(Long currentUserId, List<String> otherUserRolesNames);

    boolean hasMaxRoleLevelOf(Roles role, Long currentUserId);

    boolean hasMaxRoleLevelOf(Roles role, List<Roles> userRoles);

    List<String> getUserRoles(BaseYeshteryUserEntity user);

    List<String> getUserRoles(BaseUserEntity user);

    List<String> getOrganizationRoles(Long orgId);
    
    String createRole(RoleDto roleDto);

    String deleteRole(Long id);

    String updateRole(Long id, RoleDto roleDto);
}
