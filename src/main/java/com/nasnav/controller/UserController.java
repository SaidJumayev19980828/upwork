package com.nasnav.controller;

import com.nasnav.response.ApiResponse;
import com.nasnav.service.EmployeeUserService;
import com.nasnav.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@CrossOrigin("*") // allow all origins
public class UserController {

    private UserService userService;
    private EmployeeUserService employeeUserService;

    @Autowired
    public UserController(UserService userService, EmployeeUserService employeeUserService) {
        this.userService = userService;
        this.employeeUserService = employeeUserService;
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

    /**
     * API for sending token to user to be used to change or create new password.
     * @param email user email
     * @return status of request
     */
    @GetMapping(value = "recover",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ApiResponse sendEmailRecovery(@RequestParam(value = "email") String email) {
        return this.userService.sendEmailRecovery(email);
    }

    /**
     * API used to change user password
     * @param body token, password fields
     * @return response status
     */
    @PostMapping(value = "recover",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ApiResponse recoverUser(@RequestBody String body) {
        return this.userService.recoverUser(body);
    }


    /**
     * API used to login user.
     * @param body email, password fields
     * @return response status
     */
    @PostMapping(value = "login",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ApiResponse login(@RequestBody String body) {
        return this.employeeUserService.login(body);
    }
}
