package com.nasnav.controller;


import com.nasnav.dto.request.PermissionDto;
import com.nasnav.dto.request.PermissionListDto;
import com.nasnav.dto.response.PermissionResponse;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;

    @PostMapping()
    public String create(@RequestBody PermissionDto permissionDto) {
        return permissionService.addPermission(permissionDto);
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @RequestBody PermissionDto permissionDto) {
        return permissionService.editPermission(id, permissionDto);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        return permissionService.deletePermission(id);
    }

    @GetMapping("/{id}")
    public PermissionResponse get(@PathVariable Long id) {
        return permissionService.getPermission(id);
    }

    @GetMapping()
    public List<PermissionResponse> getAll(
            @RequestParam(name = "roleId", required = false) Long roleId,
            @RequestParam(name = "serviceId", required = false) Long serviceId,
            @RequestParam(name = "userId", required = false) Long userId) throws BusinessException {
        return permissionService.getPermissions(roleId, serviceId, userId);
    }

    @PostMapping("/assign")
    public String assign(@RequestBody PermissionListDto permissionListDto) {
        return permissionService.assignPermissions(permissionListDto);
    }

    @PostMapping("/unassign")
    public String unassign(@RequestBody PermissionListDto permissionListDto) {
        return permissionService.unassignPermissions(permissionListDto);
    }
}
