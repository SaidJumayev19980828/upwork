package com.nasnav.controller;


import com.nasnav.dto.RoleDto;
import com.nasnav.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@CrossOrigin("*")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @GetMapping("/employee/{id}")
    public List<String> getRolesOfUser(@PathVariable Long id) {
        return roleService.getRolesNamesOfEmployeeUser(id);
    }

    @GetMapping("/organization/{id}")
    public List<String> getRolesOfOrganization(@PathVariable Long id) {
        return roleService.getOrganizationRoles(id);
    }

    @PostMapping()
    public String createRole(@RequestBody RoleDto roleDto) {
        return roleService.createRole(roleDto);
    }

    @DeleteMapping("/{id}")
    public String deleteRole(@PathVariable Long id) {
        return roleService.deleteRole(id);
    }

    @PutMapping("/{id}")
    public String updateRole(@PathVariable Long id, @RequestBody RoleDto roleDto) {
        return roleService.updateRole(id, roleDto);
    }
}
