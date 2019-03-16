package com.nasnav.controller;

import com.nasnav.entity.User;
import com.nasnav.response.ApiResponse;
import com.nasnav.response.ResponseStatus;
import com.nasnav.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * Represent the User entity resources that
 * holds all APIs for User
 */
@RestController
@RequestMapping(path = "/user")
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * API for registering new user.
     *
     * @param request JSON object contain name and email.
     * @return ApiResponse object which is either success or fail response.
     */
    @RequestMapping(value = "register",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ApiResponse createUser(@RequestBody String request) {
        User user = this.userService.registerUser(request);
        return new ApiResponse(user.getId(), Arrays.asList(ResponseStatus.NEED_ACTIVATION, ResponseStatus.ACTIVATION_SENT));
    }
}
