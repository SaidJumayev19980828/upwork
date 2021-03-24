package com.nasnav.service;

import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.enumerations.Roles;
import com.nasnav.response.UserApiResponse;

import java.util.List;

public interface EmployeeUserService extends CommonUserServiceInterface {

	UserApiResponse createEmployeeUser(UserDTOs.EmployeeUserCreationObject employeeUserJson);

	UserApiResponse updateEmployeeUser(UserDTOs.EmployeeUserUpdatingObject employeeUserJson);

	List<UserRepresentationObject> getUserList(String token, Long orgId, Long storeId, String role);

	void suspendEmployeeAccount(Long id, Boolean suspend);
}
