package com.nasnav.service;

import com.nasnav.persistence.Role;

import java.util.List;

public interface RoleService {
    /**
     * Gel list of roles assigned to passed employeeUserId
     *
     * @param integer Id of employee user entity.
     * @return list or roles
     */
    List<Role> getRolesOfEmployeeUser(Integer integer);

}
