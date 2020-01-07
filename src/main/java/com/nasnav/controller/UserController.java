package com.nasnav.controller;

import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.UserApiResponse;
import com.nasnav.security.oauth2.exceptions.InCompleteOAuthRegisteration;
import com.nasnav.service.EmployeeUserService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.UserService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Api(description = "Set of endpoints for registering and updating user data.")
@CrossOrigin("*") // allow all origins
public class UserController {

    private static final String OAUTH_ENTER_EMAIL_PAGE = "/user/login/oauth2/complete_registeration?token=";
    
	private UserService userService;
    private EmployeeUserService employeeUserService;
    
    @Autowired
    private SecurityService securityService;

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
    public UserApiResponse createEmployeeUser(@RequestHeader (value = "User-ID", required = true) Long userId,
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
    public UserApiResponse login(@RequestBody UserDTOs.UserLoginObject login) throws BusinessException {
    	return securityService.login(login);
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
    public UserApiResponse updateEmployeeUser(@RequestHeader (value = "User-ID", required = true) Long userId,
                                              @RequestHeader (value = "User-Token", required = true) String userToken,
                                              @RequestBody UserDTOs.EmployeeUserUpdatingObject json) {
        if (json.employee) {
            return this.employeeUserService.updateEmployeeUser(userId, userToken, json);
        }
        return this.userService.updateUser(userId, userToken, json);
    }
    
    
    
    

    
    
    
    @ApiOperation(value = "Get user info", nickname = "userInfo")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "User not found"),
    })
    @GetMapping(value = "info", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public UserRepresentationObject getUserData(@RequestHeader (value = "User-Token") String userToken,
                                      @RequestParam (value = "id", required = false) Long id
                                      ,@RequestParam (value = "is_employee", required = false) Boolean isEmployee) throws BusinessException{

        return userService.getUserData(id, isEmployee);
    }
    
    
    
    

    
    
    
    @ApiOperation(value = "Get employee users list", nickname = "employeesInfo")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Customers doesn't have access to view Employee users data"),
    })
    @GetMapping(value = "list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity getUserList(@RequestHeader (value = "User-Token") String userToken,
                                      @RequestParam (value = "org_id", required = false) Long orgId,
                                      @RequestParam (value = "store_id", required = false) Long storeId,
                                      @RequestParam (value = "role", required = false) String role) throws BusinessException{
        return new ResponseEntity(employeeUserService.getUserList(userToken, orgId, storeId, role), HttpStatus.OK);
    }
    
    
    
    
    
    @ApiOperation(value = "Log in user using a social login token, "
    		+ "mainly used as a redirect destination at the end of the OAuth2 login process"
    		, nickname = "userSocialLogin")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "User logged in"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Invalid credentials"),
            @io.swagger.annotations.ApiResponse(code = 423, message = "Account unavailable"),
    })
    @PostMapping(value = "login/oauth2",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserApiResponse> login(@RequestParam("token") String socialLoginToken) throws BusinessException {
    	ResponseEntity.BodyBuilder response = ResponseEntity.ok();
    	try {
    		UserApiResponse body = securityService.socialLogin(socialLoginToken);
    		return response.body(body);
    	}catch(InCompleteOAuthRegisteration e) {
    		//change it to forward to a server rendered page
    		return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
    							.header(HttpHeaders.LOCATION, OAUTH_ENTER_EMAIL_PAGE + socialLoginToken)
    							.build();
    	}    	
    }
    
    
    
    
    
    
}
