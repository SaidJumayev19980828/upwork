package com.nasnav.controller;

import com.nasnav.dto.UserDTOs;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.EmployeeUserService;
import com.nasnav.service.UserService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Api(description = "Set of endpoints for registering and updating user data.")
@CrossOrigin("*") // allow all origins
public class UserController {

    private UserService userService;
    private EmployeeUserService employeeUserService;

    @Autowired
    public UserController(UserService userService, EmployeeUserService employeeUserService) {
        this.userService = userService;
        this.employeeUserService = employeeUserService;
    }
    
    @ApiOperation(value = "Create a new employee user", nickname = "employeeUserCreation", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 201, message = "account needs activation"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "provided email is invalid"),
    })
    @PostMapping(value = "create",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UserApiResponse createEmployeeUser(@RequestBody UserDTOs.EmployeeUserCreationObject employeeUserJson) {
        return this.employeeUserService.createEmployeeUser(employeeUserJson);
    }


    @ApiOperation(value = "Register a new user", nickname = "userRegister", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 201, message = "User registered"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @PostMapping(value = "register",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UserApiResponse registerUser(@RequestBody UserDTOs.UserRegistrationObject userJson) {
        return this.userService.registerUser(userJson);
    }

    @ApiOperation(value = "Send password recovery email to the user", nickname = "userPasswordToken")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Password reset email sent"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @GetMapping(value = "recover",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public UserApiResponse sendEmailRecovery(@RequestParam(value = "email") String email) {
        return this.userService.sendEmailRecovery(email);
    }

    @ApiOperation(value = "Change user's password ", nickname = "userPasswordReset")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Password changed"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @PostMapping(value = "recover",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public UserApiResponse recoverUser(@RequestBody UserDTOs.PasswordResetObject json) {
        return this.userService.recoverUser(json);
    }


    @ApiOperation(value = "Log in user", nickname = "userLogin")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "User logged in"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Invalid credentials"),
            @io.swagger.annotations.ApiResponse(code = 423, message = "Account unavailable"),
    })
    @PostMapping(value = "login",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public UserApiResponse login(@RequestBody UserDTOs.UserLoginObject login) {
        return this.employeeUserService.login(login);
    }
}
