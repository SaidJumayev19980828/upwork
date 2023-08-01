package com.nasnav.service;

import com.nasnav.dto.*;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.ActivateOtpDto;
import com.nasnav.response.UserApiResponse;

import java.util.List;

public interface EmployeeUserService extends CommonUserServiceInterface {

	UserApiResponse createEmployeeUser(UserDTOs.EmployeeUserCreationObject employeeUserJson);

	UserApiResponse createEmployeeUserWithPassword(UserDTOs.EmployeeUserWithPassword employeeUserWithPassword);

	UserApiResponse updateEmployeeUser(UserDTOs.EmployeeUserUpdatingObject employeeUserJson);

	List<UserRepresentationObject> getUserList(String token, Long orgId, Long storeId, String role);

	void suspendEmployeeAccount(Long id, Boolean suspend);

	List<UserRepresentationObject> getAvailableEmployeesByOrgId(Long orgId);

	void sendEmailRecovery(String email);

	public UserApiResponse activateUserAccount(ActivateOtpDto activateOtpDto);
}
