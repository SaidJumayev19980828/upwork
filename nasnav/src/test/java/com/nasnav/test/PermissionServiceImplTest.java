package com.nasnav.test;

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
import com.nasnav.service.impl.PermissionServiceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
public class PermissionServiceImplTest {
    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private PermissionDto permissionDto;
    private Permission permission;
    private Role role;
    private ServiceEntity serviceEntity;
    private PermissionResponse permission1;
    private PermissionResponse permission2;
    private PermissionResponse permission3;

    @BeforeEach
    public void setUp() {
        permissionDto = new PermissionDto();
        permissionDto.setName("TEST_PERMISSION");
        permissionDto.setRoleIds(Arrays.asList(1, 2));
        permissionDto.setServiceIds(Arrays.asList(1L, 2L));

        permission = new Permission();
        permission.setId(1L);
        permission.setName("TEST_PERMISSION");
        Set<Permission> permissions = new HashSet<>();
        permissions.add(permission);

        role = new Role();
        role.setId(1);
        role.setName("TEST_ROLE");
        role.setPermissions(permissions);
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        serviceEntity = new ServiceEntity();
        serviceEntity.setId(1L);
        serviceEntity.setName("TEST_SERVICE");
        serviceEntity.setPermissions(permissions);
        Set<ServiceEntity> services = new HashSet<>();
        services.add(serviceEntity);

        permission.setServices(services);
        permission.setRoles(roles);


        permission1 = new PermissionResponse(1L, "TEST_PERMISSION");
        permission2 = new PermissionResponse(2L, "PERMISSION_2");
        permission3 = new PermissionResponse(3L, "PERMISSION_3");
    }

    @Test
    public void testAddPermission_Success() {
        when(permissionRepository.existsByName(permissionDto.getName())).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenReturn(permission);
        when(roleRepository.findByIdIn(anyList())).thenReturn(Collections.singletonList(role));
        when(serviceRepository.findByIdIn(anyList())).thenReturn(Collections.singletonList(serviceEntity));

        String result = permissionService.addPermission(permissionDto);

        assertEquals("Permission added successfully", result);
        verify(permissionRepository, times(1)).save(any(Permission.class));
    }

    @Test
    public void testAddPermission_AlreadyExists() {
        when(permissionRepository.existsByName(permissionDto.getName())).thenReturn(true);

        RuntimeBusinessException exception = assertThrows(RuntimeBusinessException.class, () -> {
            permissionService.addPermission(permissionDto);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
        assertEquals(ErrorCodes.PERMISSION$001.getValue(), exception.getErrorMessage());
    }

    @Test
    public void testEditPermission_Success() {
        when(permissionRepository.findById(anyLong())).thenReturn(Optional.of(permission));
        when(roleRepository.findByIdIn(anyList())).thenReturn(Collections.singletonList(role));
        when(serviceRepository.findByIdIn(anyList())).thenReturn(Collections.singletonList(serviceEntity));

        String result = permissionService.editPermission(1L, permissionDto);

        assertEquals("Permission updated successfully", result);
        verify(permissionRepository, times(1)).save(any(Permission.class));
    }

    @Test
    public void testEditPermission_NotFound() {
        when(permissionRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeBusinessException exception = assertThrows(RuntimeBusinessException.class, () -> {
            permissionService.editPermission(1L, permissionDto);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals(ErrorCodes.PERMISSION$003.getValue(), exception.getErrorMessage());
    }

    @Test
    public void testDeletePermission_Success() {
        when(permissionRepository.findById(anyLong())).thenReturn(Optional.of(permission));

        String result = permissionService.deletePermission(1L);

        assertEquals("Permission deleted successfully", result);
        verify(permissionRepository, times(1)).delete(any(Permission.class));
    }

    @Test
    public void testDeletePermission_NotFound() {
        when(permissionRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeBusinessException exception = assertThrows(RuntimeBusinessException.class, () -> {
            permissionService.deletePermission(1L);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals(ErrorCodes.PERMISSION$003.getValue(), exception.getErrorMessage());
    }

    @Test
    public void testGetPermission_Success() {
        when(permissionRepository.findById(anyLong())).thenReturn(Optional.of(permission));

        PermissionResponse response = permissionService.getPermission(1L);

        assertEquals(permission.getId(), response.getId());
        assertEquals(permission.getName(), response.getName());
    }

    @Test
    public void testGetPermission_NotFound() {
        when(permissionRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeBusinessException exception = assertThrows(RuntimeBusinessException.class, () -> {
            permissionService.getPermission(1L);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals(ErrorCodes.PERMISSION$003.getValue(), exception.getErrorMessage());
    }

    @Test
    public void testGetPermissionsByRole_Success() {
        when(roleRepository.findById(anyInt())).thenReturn(Optional.of(role));
        List<PermissionResponse> responses = permissionService.getPermissionsByRole(1L);
        assertFalse(responses.isEmpty());
    }

    @Test
    public void testGetPermissionsByRole_NotFound() {
        when(roleRepository.findById(anyInt())).thenReturn(Optional.empty());

        RuntimeBusinessException exception = assertThrows(RuntimeBusinessException.class, () -> {
            permissionService.getPermissionsByRole(1L);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals(ErrorCodes.ROLE$001.getValue(), exception.getErrorMessage());
    }

    @Test
    public void testGetPermissionsByServiceId_Success() {
        when(serviceRepository.findById(anyLong())).thenReturn(Optional.of(serviceEntity));

        List<PermissionResponse> responses = permissionService.getPermissionsByServiceId(1L);

        assertFalse(responses.isEmpty());
    }

    @Test
    public void testGetPermissionsByServiceId_NotFound() {
        when(serviceRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeBusinessException exception = assertThrows(RuntimeBusinessException.class, () -> {
            permissionService.getPermissionsByServiceId(1L);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals(String.format(ErrorCodes.PA$SRV$0002.getValue(), "1"), exception.getErrorMessage());
    }

    @Test
    public void testGetAllPermissions_Success() {
        when(permissionRepository.findAll()).thenReturn(Collections.singletonList(permission));

        List<PermissionResponse> responses = permissionService.getAllPermissions();

        assertFalse(responses.isEmpty());
    }

    @Test
    public void testGetPermissionsByUserId_Success() throws BusinessException {
        when(permissionRepository.findByUserIdViaRole(anyLong())).thenReturn(Collections.singletonList(permission));

        List<PermissionResponse> responses = permissionService.getPermissionsByUserId(1L);

        assertFalse(responses.isEmpty());
    }

    @Test
    public void testGetPermissions_Success() throws BusinessException {
        when(permissionRepository.findAll()).thenReturn(Collections.singletonList(permission));

        List<PermissionResponse> responses = permissionService.getPermissions(null, null, null);

        assertFalse(responses.isEmpty());
    }


    @Test
    public void testGetPermissions_AllNull() throws BusinessException {
        List<PermissionResponse> allPermissions = Arrays.asList(permission1, permission2, permission3);
        when(permissionRepository.findAll()).thenReturn(Arrays.asList(
                new Permission(1L, "TEST_PERMISSION"),
                new Permission(2L, "PERMISSION_2"),
                new Permission(3L, "PERMISSION_3")
        ));

        List<PermissionResponse> permissions = permissionService.getPermissions(null, null, null);

        assertEquals(3, permissions.size());
        assertTrue(permissions.contains(permission1));
        assertTrue(permissions.contains(permission2));
        assertTrue(permissions.contains(permission3));
    }

    @Test
    public void testGetPermissions_RoleIdProvided() throws BusinessException {
        when(roleRepository.findById(anyInt())).thenReturn(Optional.of(role));
        List<PermissionResponse> permissions = permissionService.getPermissions(1L, null, null);

        assertEquals(1, permissions.size());
        assertTrue(permissions.contains(permission1));
    }

    @Test
    public void testGetPermissions_UserIdProvided() throws BusinessException {
        when(permissionRepository.findByUserIdViaRole(anyLong())).thenReturn(Arrays.asList(
                new Permission(3L, "PERMISSION_3")
        ));

        List<PermissionResponse> permissions = permissionService.getPermissions(null, null, 1L);

        assertEquals(1, permissions.size());
        assertTrue(permissions.contains(permission3));
    }

    @Test
    public void testAssignPermissions_Success() {
        PermissionListDto permissionListDto = new PermissionListDto();
        permissionListDto.setPermissionNames(Collections.singletonList("TEST_PERMISSION"));
        permissionListDto.setRoleIds(Arrays.asList(1, 2));
        permissionListDto.setServiceIds(Arrays.asList(1L, 2L));

        when(permissionRepository.findByName(anyString())).thenReturn(permission);
        when(roleRepository.findByIdIn(anyList())).thenReturn(Collections.singletonList(role));
        when(serviceRepository.findByIdIn(anyList())).thenReturn(Collections.singletonList(serviceEntity));

        String result = permissionService.assignPermissions(permissionListDto);

        assertEquals("Permissions assigned successfully", result);
    }

    @Test
    public void testUnassignPermissions_Success() {
        PermissionListDto permissionListDto = new PermissionListDto();
        permissionListDto.setPermissionNames(Collections.singletonList("TEST_PERMISSION"));
        permissionListDto.setRoleIds(Arrays.asList(1, 2));
        permissionListDto.setServiceIds(Arrays.asList(1L, 2L));

        when(permissionRepository.findByName(anyString())).thenReturn(permission);
        when(roleRepository.findByIdIn(anyList())).thenReturn(Collections.singletonList(role));
        when(serviceRepository.findByIdIn(anyList())).thenReturn(Collections.singletonList(serviceEntity));

        String result = permissionService.unassignPermissions(permissionListDto);

        assertEquals("Permissions unassigned successfully", result);
    }
}
