package com.nasnav.service.impl;

import com.nasnav.dao.RoleRepository;
import com.nasnav.enumerations.Roles;
import com.nasnav.persistence.Role;
import com.nasnav.service.RoleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> getRolesOfEmployeeUser(Long employeeUserId) {
        return ofNullable(employeeUserId)
                .map(roleRepository::getRolesOfEmployeeUser)
                .orElse(emptyList());
    }

    private List<Roles> getRolesEnumOfEmployeeUser(Long employeeUserId) {
        return ofNullable(employeeUserId)
                .map(roleRepository::getRolesOfEmployeeUser)
                .orElse(emptyList())
                .stream()
                .map(r -> Roles.fromString(r.getName()))
                .collect(toList());
    }

    public Roles getEmployeeHighestRole(List<Roles> employeeRoles) {
        return Roles
                .getSortedEmployeeRoles()
                .stream()
                .filter(employeeRoles::contains)
                .findFirst()
                .orElse(null);
    }

    public Roles getEmployeeHighestRole(Long userId) {
        return getEmployeeHighestRole(getRolesEnumOfEmployeeUser(userId));
    }

    public boolean checkRoleOrder(String userRole, String requestedRole) {
        Map<String,Set<String>> privileges = Roles.getAllPrivileges();
        if (privileges.containsKey(userRole)) {
            Set<String> roles = privileges.get(userRole);
            if (roles.contains(requestedRole))
                return true;
        }
        return false;
    }
}
