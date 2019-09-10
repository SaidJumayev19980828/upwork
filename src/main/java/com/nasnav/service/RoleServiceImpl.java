package com.nasnav.service;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.RoleRepository;
import com.nasnav.enumerations.Roles;
import com.nasnav.persistence.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> getRolesOfEmployeeUser(Long employeeUserId) {
        if (StringUtils.isBlankOrNull(employeeUserId)) {
            return Collections.emptyList();
        }
        return roleRepository.getRolesOfEmployeeUser(employeeUserId);
    }

    public boolean checkRoleOrder(String userRole, String requestedRole) {
        Map prelivedges = Roles.getAllPrevliges();
        if (prelivedges.containsKey(userRole)) {
            Set<String> roles = (Set) prelivedges.get(userRole);
            if (roles.contains(requestedRole))
                return true;
        }
        return false;
    }
}
