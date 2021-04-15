package com.nasnav.service;

import com.nasnav.dao.RoleRepository;
import com.nasnav.enumerations.Roles;
import com.nasnav.persistence.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> getRolesOfEmployeeUser(Long employeeUserId) {
        if ( isBlankOrNull(employeeUserId)) {
            return Collections.emptyList();
        }
        return roleRepository.getRolesOfEmployeeUser(employeeUserId);
    }


    public boolean checkRoleOrder(String userRole, String requestedRole) {
        Map<String,Set<String>> prelivedges = Roles.getAllPrevliges();
        if (prelivedges.containsKey(userRole)) {
            Set<String> roles = prelivedges.get(userRole);
            if (roles.contains(requestedRole))
                return true;
        }
        return false;
    }
}
