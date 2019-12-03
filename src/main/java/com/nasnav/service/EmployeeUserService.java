package com.nasnav.service;

import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.UserApiResponse;

import java.util.List;

public interface EmployeeUserService extends CommonUserServiceInterface {

	/**
     * create employee user on the system
     *
     * @param employeeUserJson json object containing email, password, name, org_id, store_id, role string
     * @return UserApiResponse object holding the employee user id
     */
	public UserApiResponse createEmployeeUser(Long userId, String userToken, UserDTOs.EmployeeUserCreationObject employeeUserJson);

	/**
	 * update employee user on the system
	 *
	 * @param employeeUserJson json object containing email, password, name, org_id, store_id, role string
	 * @return UserApiResponse object holding the employee user id
	 */
	public UserApiResponse updateEmployeeUser(Long userId, String userToken, UserDTOs.EmployeeUserUpdatingObject employeeUserJson);


	public List<UserRepresentationObject> getUserList(String token, Long orgId, Long storeId, String role) throws BusinessException;

	public UserRepresentationObject getUserData(String token, Long id) throws BusinessException;
}
