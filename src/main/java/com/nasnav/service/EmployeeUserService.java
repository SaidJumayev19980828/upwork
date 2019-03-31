package com.nasnav.service;

import com.nasnav.dto.UserDTOs;
import com.nasnav.response.UserApiResponse;

public interface EmployeeUserService {

    /**
     * login user to system
     *
     * @param body json object containing email and password
     * @return UserApiResponse object holding the status
     */
    UserApiResponse login(UserDTOs.UserLoginObject body);

}
