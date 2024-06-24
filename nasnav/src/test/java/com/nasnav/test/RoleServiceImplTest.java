package com.nasnav.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.*;

import com.nasnav.dao.RoleEmployeeUserRepository;
import com.nasnav.dao.RoleRepository;
import com.nasnav.dto.RoleDto;
import com.nasnav.enumerations.Roles;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.Role;
import com.nasnav.persistence.yeshtery.BaseYeshteryUserEntity;
import com.nasnav.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;


@ExtendWith(MockitoExtension.class)
public class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleEmployeeUserRepository roleEmployeeUserRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role role;
    private EmployeeUserEntity employee;
    private RoleDto roleDto;

    @BeforeEach
    public void setUp() {
        role = new Role();
        role.setId(1);
        role.setName("STORE_EMPLOYEE");
        role.setOrganizationId(1L);

        employee = new EmployeeUserEntity();
        employee.setId(1L);

        roleDto = new RoleDto("STORE_EMPLOYEE", 1L);
    }

    @Test
    public void testGetRolesOfEmployeeUser() {
        when(roleRepository.getRolesOfEmployeeUser(anyLong())).thenReturn(Collections.singletonList(role));

        List<Role> roles = roleService.getRolesOfEmployeeUser(1L);

        assertEquals(1, roles.size());
        assertEquals("STORE_EMPLOYEE", roles.get(0).getName());
    }

    @Test
    public void testGetRolesEnumOfEmployeeUser() {
        when(roleRepository.getRolesOfEmployeeUser(anyLong())).thenReturn(Collections.singletonList(role));

        List<Roles> roles = roleService.getRolesEnumOfEmployeeUser(1L);

        assertEquals(1, roles.size());
        assertEquals(Roles.fromString("STORE_EMPLOYEE"), roles.get(0));
    }

    @Test
    public void testGetRolesNamesOfEmployeeUser() {
        when(roleRepository.getRolesOfEmployeeUser(anyLong())).thenReturn(Collections.singletonList(role));

        List<String> roleNames = roleService.getRolesNamesOfEmployeeUser(1L);

        assertEquals(1, roleNames.size());
        assertEquals("STORE_EMPLOYEE", roleNames.get(0));
    }

    @Test
    public void testGetEmployeeHighestRole() {
        when(roleRepository.getRolesOfEmployeeUser(anyLong())).thenReturn(Collections.singletonList(role));

        Roles highestRole = roleService.getEmployeeHighestRole(1L);

        assertEquals(Roles.fromString("STORE_EMPLOYEE"), highestRole);
    }

    @Test
    public void testCreateRoles() {
        List<String> rolesList = Collections.singletonList("NEW_ROLE");
        List<Role> existingRoles = Collections.singletonList(role);

        when(roleRepository.findByOrganizationId(anyLong())).thenReturn(existingRoles);
        when(roleRepository.save(any(Role.class))).thenReturn(new Role("NEW_ROLE", 1L));

        roleService.createRoles(rolesList, employee, 1L);

        verify(roleEmployeeUserRepository, times(1)).deleteByEmployee_Id(anyLong());
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    public void testGetAllRoleNames() {
        when(roleRepository.findAll()).thenReturn(Collections.singletonList(role));

        List<String> roleNames = roleService.getAllRoleNames();

        assertEquals(1, roleNames.size());
        assertEquals("STORE_EMPLOYEE", roleNames.get(0));
    }

    @Test
    public void testRoleCannotManageUsers() {
        when(roleRepository.getRolesOfEmployeeUser(anyLong())).thenReturn(Collections.singletonList(role));

        boolean result = roleService.roleCannotManageUsers(1L);

        assertTrue(result);
    }

    @Test
    public void testEmployeeHasRoleOrHigher() {
        role.setName("ORGANIZATION_MANAGER");
        employee.setRoles(Set.of(role));

        boolean result = roleService.employeeHasRoleOrHigher(employee, Roles.ORGANIZATION_MANAGER);

        assertTrue(result);
    }

    @Test
    public void testHasInsufficientLevel() {
        List<String> otherUserRolesNames = Collections.singletonList("ORGANIZATION_MANAGER");

        when(roleRepository.getRolesOfEmployeeUser(anyLong())).thenReturn(Collections.singletonList(role));

        boolean result = roleService.hasInsufficientLevel(1L, otherUserRolesNames);

        assertTrue(result);
    }

    @Test
    public void testHasMaxRoleLevelOf() {
        role.setName(Roles.ORGANIZATION_EMPLOYEE.name());
        when(roleRepository.getRolesOfEmployeeUser(anyLong())).thenReturn(Collections.singletonList(role));

        boolean result = roleService.hasMaxRoleLevelOf(Roles.ORGANIZATION_EMPLOYEE, 1L);

        assertTrue(result);
    }

    @Test
    public void testGetUserRoles_BaseYeshteryUserEntity() {
        BaseYeshteryUserEntity user = mock(BaseYeshteryUserEntity.class);

        List<String> roles = roleService.getUserRoles(user);

        assertEquals(1, roles.size());
        assertEquals(Roles.CUSTOMER.name(), roles.get(0));
    }

    @Test
    public void testGetUserRoles_BaseUserEntity() {
        BaseUserEntity user = mock(BaseUserEntity.class);
        when(user.getId()).thenReturn(1L);
        when(roleRepository.getRolesOfEmployeeUser(anyLong())).thenReturn(Collections.singletonList(role));

        List<String> roles = roleService.getUserRoles(user);

        assertEquals(1, roles.size());
        assertEquals("STORE_EMPLOYEE", roles.get(0));
    }

    @Test
    public void testGetOrganizationRoles() {
        when(roleRepository.findByOrganizationId(anyLong())).thenReturn(Collections.singletonList(role));

        List<String> roles = roleService.getOrganizationRoles(1L);

        assertEquals(1, roles.size());
        assertEquals("STORE_EMPLOYEE", roles.get(0));
    }

    @Test
    public void testCreateRole() {
        when(roleRepository.findByNameAndOrganizationId(anyString(), anyLong())).thenReturn(null);
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        String result = roleService.createRole(roleDto);

        assertEquals("Successfully added a role", result);
    }

    @Test
    public void testCreateRole_AlreadyExists() {
        when(roleRepository.findByNameAndOrganizationId(anyString(), anyLong())).thenReturn(role);

        RuntimeBusinessException exception = assertThrows(RuntimeBusinessException.class, () -> {
            roleService.createRole(roleDto);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals(ErrorCodes.ROLE$001.getValue(), exception.getErrorMessage());
    }

    @Test
    public void testDeleteRole() {
        when(roleRepository.findById(anyInt())).thenReturn(Optional.of(role));

        String result = roleService.deleteRole(1L);

        assertEquals("Successfully deleted a role", result);
        verify(roleRepository, times(1)).delete(any(Role.class));
    }

    @Test
    public void testDeleteRole_NotFound() {
        when(roleRepository.findById(anyInt())).thenReturn(Optional.empty());

        RuntimeBusinessException exception = assertThrows(RuntimeBusinessException.class, () -> {
            roleService.deleteRole(1L);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals(ErrorCodes.ROLE$001.getValue(), exception.getErrorMessage());
    }

    @Test
    public void testUpdateRole() {
        when(roleRepository.findById(anyInt())).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        String result = roleService.updateRole(1L, roleDto);

        assertEquals("Successfully updated a role", result);
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    public void testUpdateRole_NotFound() {
        when(roleRepository.findById(anyInt())).thenReturn(Optional.empty());

        RuntimeBusinessException exception = assertThrows(RuntimeBusinessException.class, () -> {
            roleService.updateRole(1L, roleDto);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals(ErrorCodes.ROLE$001.getValue(), exception.getErrorMessage());
    }

    @Test
    public void testUpdateRole_AlreadyExists() {
        when(roleRepository.findById(anyInt())).thenReturn(Optional.of(role));
        when(roleRepository.findByNameAndOrganizationId(anyString(), anyLong())).thenReturn(role);

        RoleDto updatedRoleDto = new RoleDto("UPDATED_ROLE", 1L);

        RuntimeBusinessException exception = assertThrows(RuntimeBusinessException.class, () -> {
            roleService.updateRole(1L, updatedRoleDto);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals(ErrorCodes.ROLE$003.getValue(), exception.getErrorMessage());
    }
}
