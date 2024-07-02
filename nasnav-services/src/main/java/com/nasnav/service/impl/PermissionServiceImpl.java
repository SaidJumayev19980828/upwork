package com.nasnav.service.impl;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.PermissionRepository;
import com.nasnav.dao.RoleRepository;
import com.nasnav.dao.ServiceRepository;
import com.nasnav.dto.request.PermissionDto;
import com.nasnav.dto.request.PermissionListDto;
import com.nasnav.dto.response.PermissionResponse;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.Permission;
import com.nasnav.persistence.Role;
import com.nasnav.persistence.ServiceEntity;
import com.nasnav.service.PermissionService;
import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final ServiceRepository serviceRepository;

    @Override
    public String addPermission(PermissionDto permissionDto) {
        if (StringUtils.isBlankOrNull(permissionDto.getName())) {
            throw new RuntimeBusinessException(HttpStatus.BAD_REQUEST, ErrorCodes.PERMISSION$002);
        }
        String name = permissionDto.getName();
        if (permissionRepository.existsByName(name)) {
            throw new RuntimeBusinessException(HttpStatus.CONFLICT, ErrorCodes.PERMISSION$001);
        }
        Permission permission = new Permission();
        permission.setName(name);

        addRoleAndServices(permissionDto, permission);
        return "Permission added successfully";
    }

    @Override
    public String editPermission(Long id, PermissionDto permissionDto) {
        Permission permission = permissionRepository.findById(id).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.PERMISSION$003));
        if (StringUtils.isNotBlankOrNull(permissionDto.getName())) {
            permission.setName(permissionDto.getName());
        }
        addRoleAndServices(permissionDto, permission);
        return "Permission updated successfully";
    }

    private void addRoleAndServices(PermissionDto permissionDto, Permission permission) {
        if (!Collections.isEmpty(permissionDto.getRoleIds())) {
            List<Role> roles = roleRepository.findByIdIn(permissionDto.getRoleIds());
            Set<Role> roles1 = permission.getRoles() != null ? permission.getRoles() : new HashSet<>();
            roles1.addAll(roles);
            permission.setRoles(roles1);
        }
        if (!Collections.isEmpty(permissionDto.getServiceIds())) {
            List<ServiceEntity> serviceEntityList = serviceRepository.findByIdIn(permissionDto.getServiceIds());
            Set<ServiceEntity> services1 = permission.getServices() != null ? permission.getServices() : new HashSet<>();
            services1.addAll(serviceEntityList);
            permission.setServices(services1);
        }
        permissionRepository.save(permission);
    }

    @Override
    public String deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.PERMISSION$003));
        permissionRepository.delete(permission);
        return "Permission deleted successfully";
    }

    @Override
    public PermissionResponse getPermission(Long id) {
        Permission permission = permissionRepository.findById(id).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.PERMISSION$003));
        return new PermissionResponse(permission.getId(), permission.getName());
    }

    @Override
    public List<PermissionResponse> getPermissionsByRole(Long roleId) {
        if (StringUtils.isBlankOrNull(roleId)) {
            throw new RuntimeBusinessException(HttpStatus.BAD_REQUEST, ErrorCodes.U$EMP$0007, String.valueOf(roleId));
        }
        Integer id = roleId.intValue();
        Role role = roleRepository.findById(id).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.ROLE$001, roleId));
        return role.getPermissions().stream().map(permission -> new PermissionResponse(permission.getId(), permission.getName())).toList();
    }

    @Override
    public List<PermissionResponse> getPermissionsByServiceId(Long serviceId) {
        if (StringUtils.isBlankOrNull(serviceId)) {
            throw new RuntimeBusinessException(HttpStatus.BAD_REQUEST, ErrorCodes.PA$SRV$0002, String.valueOf(serviceId));
        }
        ServiceEntity serviceEntity = serviceRepository.findById(serviceId).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, ErrorCodes.PA$SRV$0002, serviceId));
        return serviceEntity.getPermissions().stream().map(permission -> new PermissionResponse(permission.getId(), permission.getName())).toList();
    }

    @Override
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream().map(permission -> new PermissionResponse(permission.getId(), permission.getName())).toList();
    }

    @Override
    public List<PermissionResponse> getPermissionsByUserId(Long userId) throws BusinessException {
        Set<PermissionResponse> permissionResponses = new HashSet<>();
        permissionRepository.findByUserIdViaRole(userId).stream()
                .map(permission -> new PermissionResponse(permission.getId(), permission.getName()))
                .forEach(permissionResponses::add);
        return new ArrayList<>(permissionResponses);
    }

    @Override
    public List<PermissionResponse> getPermissions(Long roleId, Long serviceId, Long userId) throws BusinessException {
        if (roleId == null && serviceId == null && userId == null) {
            return getAllPermissions();
        }

        Set<PermissionResponse> permissions = new HashSet<>();
        boolean isRoleNameProvided = roleId != null;
        boolean isServiceIdProvided = serviceId != null;
        boolean isUserIdProvided = userId != null;

        if (isRoleNameProvided) {
            permissions.addAll(getPermissionsByRole(roleId));
        }

        if (isServiceIdProvided) {
            if (permissions.isEmpty()) {
                permissions.addAll(getPermissionsByServiceId(serviceId));
            } else {
                permissions.retainAll(getPermissionsByServiceId(serviceId));
            }
        }

        if (isUserIdProvided) {
            if (permissions.isEmpty()) {
                permissions.addAll(getPermissionsByUserId(userId));
            } else {
                permissions.retainAll(getPermissionsByUserId(userId));
            }
        }

        return new ArrayList<>(permissions);
    }

    @Override
    public String assignPermissions(PermissionListDto permissionListDto) {
        validatePermissionNames(permissionListDto.getPermissionNames());

        List<Permission> permissions = permissionListDto.getPermissionNames().stream()
                .map(this::createOrGetPermission)
                .toList();
        for (Permission permission : permissions) {
            PermissionDto permissionDto = new PermissionDto();
            permissionDto.setName(permission.getName());
            permissionDto.setRoleIds(permissionListDto.getRoleIds());
            permissionDto.setServiceIds(permissionListDto.getServiceIds());
            addRoleAndServices(permissionDto, permission);
        }
        return "Permissions assigned successfully";
    }

    private void validatePermissionNames(List<String> permissionNames) {
        if (Collections.isEmpty(permissionNames)) {
            throw new RuntimeBusinessException(HttpStatus.BAD_REQUEST, ErrorCodes.PERMISSION$002);
        }
    }

    private Permission createOrGetPermission(String permissionName) {
        return ofNullable(permissionRepository.findByName(permissionName))
                .orElseGet(() -> new Permission(permissionName));
    }

    @Override
    public String unassignPermissions(PermissionListDto permissionListDto) {
        validatePermissionNames(permissionListDto.getPermissionNames());

        List<Permission> permissions = permissionListDto.getPermissionNames().stream()
                .map(permissionRepository::findByName)
                .filter(Objects::nonNull)
                .toList();
        for (Permission p : permissions) {
            PermissionDto permissionDto = new PermissionDto();
            permissionDto.setName(p.getName());
            permissionDto.setRoleIds(permissionListDto.getRoleIds());
            permissionDto.setServiceIds(permissionListDto.getServiceIds());
            removeRoleAndServices(permissionDto, p);
        }
        return "Permissions unassigned successfully";
    }

    private void removeRoleAndServices(PermissionDto permissionDto, Permission permission) {
        if (!Collections.isEmpty(permissionDto.getRoleIds())) {
            List<Role> roles = roleRepository.findByIdIn(permissionDto.getRoleIds());
            Set<Role> roles1 = permission.getRoles() != null ? permission.getRoles() : new HashSet<>();
            roles.forEach(roles1::remove);
            permission.setRoles(roles1);
        }
        if (!Collections.isEmpty(permissionDto.getServiceIds())) {
            List<ServiceEntity> serviceEntityList = serviceRepository.findByIdIn(permissionDto.getServiceIds());
            Set<ServiceEntity> services1 = permission.getServices() != null ? permission.getServices() : new HashSet<>();
            serviceEntityList.forEach(services1::remove);
            permission.setServices(services1);
        }
        permissionRepository.save(permission);
    }
}
