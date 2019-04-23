package com.nasnav.service;

import com.nasnav.dto.UserDTOs;
import com.nasnav.response.UserApiResponse;

public interface EmployeeUserService {

	/**
     * create employee user on the system
     *
     * @param employeeUserJson json object containing email, password, name, org_id, store_id, role string
     * @return UserApiResponse object holding the employee user id
     */
	public UserApiResponse createEmployeeUser(UserDTOs.EmployeeUserCreationObject employeeUserJson);
	
    /**
     * login user to system
     *
     * @param body json object containing email and password
     * @return UserApiResponse object holding the status
     */
    UserApiResponse login(UserDTOs.UserLoginObject body);

}
