package com.nasnav.controller;

import com.nasnav.response.ApiResponse;
import com.nasnav.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private UserService userService;
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    /**
     * API for registering new user.
     *
     * @param userJson JSON object contain name and email.
     * @return ApiResponse object which is either success or fail response.
     */
    @RequestMapping(value = "register",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ApiResponse createUser(@RequestBody String userJson) {
        return this.userService.registerUser(userJson);
    }

}
