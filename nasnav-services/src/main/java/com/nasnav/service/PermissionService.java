package com.nasnav.service;

import com.nasnav.dto.request.PermissionDto;
import com.nasnav.dto.request.PermissionListDto;
import com.nasnav.dto.response.PermissionResponse;
import com.nasnav.exceptions.BusinessException;

import java.util.List;

public interface PermissionService {
    String addPermission(PermissionDto permissionDto);

    String editPermission(Long id, PermissionDto permissionDto);

    String deletePermission(Long id);

    PermissionResponse getPermission(Long id);

    List<PermissionResponse> getPermissionsByRole(Long roleId);

    List<PermissionResponse> getPermissionsByServiceId(Long serviceId);

    List<PermissionResponse> getAllPermissions();

    List<PermissionResponse> getPermissionsByUserId(Long userId) throws BusinessException;

    List<PermissionResponse> getPermissions(Long roleId, Long serviceId, Long userId) throws BusinessException;

    String assignPermissions(PermissionListDto permissionListDto);

    String unassignPermissions(PermissionListDto permissionListDto);
}
