package com.nasnav.service;

import com.nasnav.enumerations.Roles;
import com.nasnav.persistence.Role;

import java.util.List;

public interface RoleService {
    /**
     * Gel list of roles assigned to passed employeeUserId
     *
     * @param integer Id of employee user entity.
     * @return list or roles
     */
    List<Role> getRolesOfEmployeeUser(Long integer);

    Roles getEmployeeHighestRole(List<Roles> employeeRoles);

    Roles getEmployeeHighestRole(Long userId);

    boolean checkRoleOrder(String userRole, String requestedRole);

}
