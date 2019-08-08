package com.nasnav.service;

import com.nasnav.dao.RoleRepository;
import com.nasnav.persistence.EntityUtils;
import com.nasnav.persistence.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> getRolesOfEmployeeUser(Long employeeUserId) {
        if (EntityUtils.isBlankOrNull(employeeUserId)) {
            return Collections.emptyList();
        }
        return roleRepository.getRolesOfEmployeeUser(employeeUserId);
    }
}
