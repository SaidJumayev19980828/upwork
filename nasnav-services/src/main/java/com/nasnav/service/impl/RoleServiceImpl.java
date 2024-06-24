package com.nasnav.service.impl;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.RoleEmployeeUserRepository;
import com.nasnav.dao.RoleRepository;
import com.nasnav.dto.RoleDto;
import com.nasnav.enumerations.Roles;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.persistence.yeshtery.BaseYeshteryUserEntity;
import com.nasnav.service.RoleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleEmployeeUserRepository roleEmployeeUserRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository, RoleEmployeeUserRepository roleEmployeeUserRepository) {
        this.roleRepository = roleRepository;
        this.roleEmployeeUserRepository = roleEmployeeUserRepository;
    }

    @Override
    public List<Role> getRolesOfEmployeeUser(Long employeeUserId) {
        return roleRepository.getRolesOfEmployeeUser(employeeUserId);
    }

    @Override
    public List<Roles> getRolesEnumOfEmployeeUser(Long employeeUserId) {
        return getRolesOfEmployeeUser(employeeUserId)
                .stream()
                .map(s -> Roles.fromString(s.getName()))
                .distinct()
                .toList();
    }

    @Override
    public List<String> getRolesNamesOfEmployeeUser(Long employeeUserId) {
        return getRolesOfEmployeeUser(employeeUserId)
                .stream()
                .map(Role::getName)
                .toList();
    }

    @Override
    public Roles getEmployeeHighestRole(Long userId) {
        return Roles.getEmployeeHighestRole(getRolesEnumOfEmployeeUser(userId));
    }

    @Override
    public void createRoles(List<String> rolesList, EmployeeUserEntity employee, Long orgId) {
        List<Role> existingRoles = roleRepository.findByOrganizationId(orgId);
        if (!rolesList.isEmpty()) {
            roleEmployeeUserRepository.deleteByEmployee_Id(employee.getId()); //delete all existing rolesemployeeuser relations
        }
        for (String r : rolesList) {
            Optional<Role> firstMatchingRole = existingRoles.stream().filter(role -> role.getName().equals(r)).findFirst();
            Role role = firstMatchingRole.orElseGet(() -> {
                Role newRole = new Role(r, orgId);
                return roleRepository.save(newRole);
            });
            createRoleEmployeeUser(employee, role);
        }
    }

    private void createRoleEmployeeUser(EmployeeUserEntity employee, Role role) {
        RoleEmployeeUser roleEmployeeUser = new RoleEmployeeUser();
        roleEmployeeUser.setRole(role);
        roleEmployeeUser.setEmployee(employee);
        roleEmployeeUserRepository.save(roleEmployeeUser);
    }

    @Override
    public List<String> getAllRoleNames() {
        return roleRepository
                .findAll()
                .stream()
                .map(Role::getName)
                .distinct()
                .toList();
    }

    @Override
    public boolean roleCannotManageUsers(Long currentUserId) {
        List<Role> rolesEntity = roleRepository.getRolesOfEmployeeUser(currentUserId);
        return rolesEntity
                .stream()
                .map(Role::getName)
                .map(Roles::fromString)
                .noneMatch(Roles::isCanCreateUsers);
    }

    @Override
    public boolean employeeHasRoleOrHigher(EmployeeUserEntity employee, Roles requiredRole) {
        return employee.getRoles().stream()
                .map(Role::getName)
                .map(Roles::fromString)
                .anyMatch(role -> role.getLevel() <= requiredRole.getLevel());
    }

    @Override
    public boolean hasInsufficientLevel(Long currentUserId, List<String> otherUserRolesNames) {
        List<Roles> currentUserRoles = getRolesEnumOfEmployeeUser(currentUserId);
        List<Roles> newUserRoles =
                otherUserRolesNames
                        .stream()
                        .map(Roles::valueOf)
                        .toList();
        return newUserRoles
                .stream()
                .anyMatch(newUserRole -> isHigherThanAllGivenRoles(newUserRole, currentUserRoles));
    }

    @Override
    public boolean hasMaxRoleLevelOf(Roles role, Long currentUserId) {
        List<Roles> currentUserRoles = getRolesEnumOfEmployeeUser(currentUserId);
        return hasMaxRoleLevelOf(role, currentUserRoles);
    }

    @Override
    public boolean hasMaxRoleLevelOf(Roles role, List<Roles> userRoles) {
        boolean hasNoRolesWithHigherLevel =
                userRoles
                        .stream()
                        .noneMatch(currentUserRole -> currentUserRole.getLevel() < role.getLevel());
        boolean hasRole = userRoles.contains(role);
        return hasRole && hasNoRolesWithHigherLevel;
    }

    private boolean isHigherThanAllGivenRoles(Roles role, Collection<Roles> otherRoles) {
        //lower level number gets higher privilege, nasnav has max privilege with
        //negative level
        return otherRoles
                .stream()
                .allMatch(otherRole -> role.getLevel() < otherRole.getLevel());
    }

    @Override
    public List<String> getUserRoles(BaseYeshteryUserEntity user) {
        if (user == null)
            return new ArrayList<>();
        return getCustomerUserRoles();
    }

    @Override
    public List<String> getUserRoles(BaseUserEntity user) {
        if (user == null)
            return new ArrayList<>();

        if (user instanceof UserEntity)
            return getCustomerUserRoles();

        return getRolesEnumOfEmployeeUser(user.getId())
                .stream()
                .map(Roles::name)
                .toList();
    }

    @Override
    public List<String> getOrganizationRoles(Long orgId) {
        return roleRepository.findByOrganizationId(orgId)
                .stream()
                .map(Role::getName)
                .toList();
    }

    @Override
    public String createRole(RoleDto roleDto) {
        if (StringUtils.isBlankOrNull(roleDto.organizationId()) || StringUtils.isBlankOrNull(roleDto.name())) {
            throw new RuntimeBusinessException(HttpStatus.BAD_REQUEST, ErrorCodes.ROLE$002);
        }
        Role role = roleRepository.findByNameAndOrganizationId(roleDto.name(), roleDto.organizationId());
        if (role != null) {
            throw new RuntimeBusinessException(HttpStatus.BAD_REQUEST, ErrorCodes.ROLE$001);
        }
        role = new Role(roleDto.name(), roleDto.organizationId());
        roleRepository.save(role);
        return "Successfully added a role";
    }

    @Override
    public String deleteRole(Long id) {
        Role role = roleRepository.findById(id.intValue()).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.ROLE$001));
        roleRepository.delete(role);
        return "Successfully deleted a role";
    }

    @Override
    public String updateRole(Long id, RoleDto roleDto) {
        Role role = roleRepository.findById(id.intValue()).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.ROLE$001));
        if (roleDto.organizationId() != null) {
            role.setOrganizationId(roleDto.organizationId());
        }
        if (roleDto.name() != null) {
            Role existingRole = roleRepository.findByNameAndOrganizationId(roleDto.name(), roleDto.organizationId());
            if (existingRole != null) {
                throw new RuntimeBusinessException(HttpStatus.BAD_REQUEST, ErrorCodes.ROLE$003);
            }
            role.setName(roleDto.name());
        }
        roleRepository.save(role);
        return "Successfully updated a role";
    }

    private List<String> getCustomerUserRoles() {
        return Collections.singletonList(Roles.CUSTOMER.name());
    }

}
