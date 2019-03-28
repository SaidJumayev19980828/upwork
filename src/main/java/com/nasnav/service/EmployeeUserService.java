package com.nasnav.service;

import com.nasnav.response.ApiResponse;

public interface EmployeeUserService {

    /**
     * login user to system
     *
     * @param body json object containing email and password
     * @return ApiResponse object holding the status
     */
    ApiResponse login(String body);

}
