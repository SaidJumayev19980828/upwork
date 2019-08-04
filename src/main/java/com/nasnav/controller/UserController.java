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
            @io.swagger.annotations.ApiResponse(code = 200, message = "account needs activation"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "the email is already registered in the database"),
    })
    @PostMapping(value = "create",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public UserApiResponse createEmployeeUser(@RequestHeader (value = "User-ID", required = true) Integer userId,
                                              @RequestHeader (value = "User-Token", required = true) String userToken,
                                              @RequestBody UserDTOs.EmployeeUserCreationObject employeeUserJson) {
        return this.employeeUserService.createEmployeeUser(userId, userToken, employeeUserJson);
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
    public UserApiResponse registerUser(
                                        @RequestBody UserDTOs.UserRegistrationObject userJson) {
        return this.userService.registerUser(userJson);
    }

    @ApiOperation(value = "Send password recovery email to the user", nickname = "userPasswordToken")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Password reset email sent"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @GetMapping(value = "recover",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public UserApiResponse sendEmailRecovery(@RequestParam(value = "email") String email,
                                             @RequestParam(value = "org_id") Long orgId,
                                             @RequestParam(value = "employee") boolean employee) {
        if (employee){
            return this.employeeUserService.sendEmailRecovery(email, orgId);
        }
        return this.userService.sendEmailRecovery(email, orgId);
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
        if (json.employee){
            return this.employeeUserService.recoverUser(json);
        }
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
    	if (login.employee) {
        return this.employeeUserService.login(login);
    	}
		// try to login using users table if employee_users does not contain current
		// login.
		return this.userService.login(login);
    }

    @ApiOperation(value = "Update an employee user", nickname = "employeeUserUpdate", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "account updated successfully"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Insufficient rights "),
    })
    @PostMapping(value = "update",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public UserApiResponse updateEmployeeUser(@RequestHeader (value = "User-ID", required = true) Integer userId,
                                              @RequestHeader (value = "User-Token", required = true) String userToken,
                                              @RequestBody UserDTOs.EmployeeUserUpdatingObject employeeUserJson) {
        return this.employeeUserService.updateEmployeeUser(userId, userToken, employeeUserJson);
    }
}
