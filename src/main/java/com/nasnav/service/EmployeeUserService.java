package com.nasnav.service;

import com.nasnav.dto.UserDTOs;
import com.nasnav.response.UserApiResponse;

public interface EmployeeUserService extends CommonUserServiceInterface {

	/**
     * create employee user on the system
     *
     * @param employeeUserJson json object containing email, password, name, org_id, store_id, role string
     * @return UserApiResponse object holding the employee user id
     */
	public UserApiResponse createEmployeeUser(Integer userId, UserDTOs.EmployeeUserCreationObject employeeUserJson);

}
