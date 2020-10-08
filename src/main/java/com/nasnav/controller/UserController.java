package com.nasnav.controller;

import com.nasnav.dto.AddressDTO;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.user.ActivationEmailResendDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.UserApiResponse;
import com.nasnav.security.oauth2.exceptions.InCompleteOAuthRegisteration;
import com.nasnav.service.EmployeeUserService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.UserService;
import io.swagger.annotations.*;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    public UserApiResponse createEmployeeUser(@RequestHeader (name = "User-Token", required = false) String userToken,
                                              @RequestBody UserDTOs.EmployeeUserCreationObject employeeUserJson) {
        return this.employeeUserService.createEmployeeUser(userToken, employeeUserJson);
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
    public UserApiResponse login(@RequestBody UserDTOs.UserLoginObject login, HttpServletResponse response) throws BusinessException {
        UserApiResponse userApiResponse = securityService.login(login);
        response.addCookie(userApiResponse.getCookie());
    	return userApiResponse;
    }

    @ApiOperation(value = "logout user", nickname = "userLogout")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "User logged out"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Invalid credentials"),
            @io.swagger.annotations.ApiResponse(code = 423, message = "Account unavailable"),
    })
    @PostMapping(value = "logout")
    public UserApiResponse logout(@RequestHeader(name = "User-Token", required = false) String token,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        if (token == null || token.isEmpty())
            token = request.getCookies()[0].getValue();
        UserApiResponse userApiResponse = securityService.logout(token);
        response.addCookie(userApiResponse.getCookie());
        return userApiResponse;
    }


    @ApiOperation(value = "logout user of all sessions", nickname = "userLogoutAll")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "User logged out"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Invalid credentials"),
            @io.swagger.annotations.ApiResponse(code = 423, message = "Account unavailable"),
    })
    @PostMapping(value = "logout_all")
    public UserApiResponse logoutAll(@RequestHeader(name = "User-Token", required = false) String token,
                                  HttpServletResponse response) {
        UserApiResponse userApiResponse = securityService.logoutAll();
        response.addCookie(userApiResponse.getCookie());
        return userApiResponse;
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
    public UserApiResponse updateEmployeeUser(@RequestHeader (name = "User-Token", required = false) String userToken,
                                              @RequestBody UserDTOs.EmployeeUserUpdatingObject json) throws BusinessException {
        if (json.employee) {
            return this.employeeUserService.updateEmployeeUser(userToken, json);
        }
        return this.userService.updateUser(userToken, json);
    }
    
    
    
    

    
    
    
    @ApiOperation(value = "Get user info", nickname = "userInfo")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "User not found"),
    })
    @GetMapping(value = "info", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public UserRepresentationObject getUserData(@RequestHeader (name = "User-Token", required = false) String userToken,
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
    @ResponseStatus(OK)
    public List<UserRepresentationObject> getUserList(@RequestHeader (name = "User-Token", required = false) String userToken,
                                      @RequestParam (value = "org_id", required = false) Long orgId,
                                      @RequestParam (value = "store_id", required = false) Long storeId,
                                      @RequestParam (value = "role", required = false) String role) throws BusinessException{
        return employeeUserService.getUserList(userToken, orgId, storeId, role);
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
    public ResponseEntity<UserApiResponse> oauth2Login(@RequestParam("token") String socialLoginToken) throws BusinessException {
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


    @ApiOperation(value = "Register a new user (v2)", nickname = "userRegisterV2", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 201, message = "User registered"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @PostMapping(value = "v2/register",
            produces = APPLICATION_JSON_UTF8_VALUE,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(CREATED)
    public UserApiResponse registerUserV2(
            @RequestBody UserDTOs.UserRegistrationObjectV2 userJson) throws BusinessException {
        return this.userService.registerUserV2(userJson);
    }


    @ApiOperation(value = "activate the user account", nickname = "userActivation")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Account activated"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @GetMapping(value = "v2/register/activate",
            produces = APPLICATION_JSON_UTF8_VALUE)
    public RedirectView sendEmailRecovery(@RequestParam(value = "token") String token,
                                          @RequestParam(value = "redirect") String redirect) throws BusinessException {
        return userService.activateUserAccount(token, redirect);
    }
    
    
    
    
    @ApiOperation(value = "activate the user account and login", nickname = "userActivation")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Account activated"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @PostMapping(value = "v2/register/activate",
            produces = APPLICATION_JSON_UTF8_VALUE)
    public UserApiResponse activateUser(@RequestParam(value = "token") String token) throws BusinessException {
        return userService.activateUserAccount(token);
    }
    
    
    
    
    
    @ApiOperation(value = "resend user activation email", nickname = "userActivationResend")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Activation Email Sent"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @PostMapping(value = "v2/register/activate/resend")
    public void resendActivationEmail(@RequestBody ActivationEmailResendDTO accountInfo) throws BusinessException {
        userService.resendActivationEmail(accountInfo);
    }


    @ApiOperation(value = "add/update user address", nickname = "userAddress")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Address added/updated"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @PutMapping(value = "/address")
    public AddressDTO updateUserAddress(@RequestHeader (name = "User-Token", required = false) String token,
                                        @RequestBody AddressDTO address)  {
        return userService.updateUserAddress(address);
    }


    @ApiOperation(value = "delete user address", nickname = "deleteUserAddress")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Address removed"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Address not found"),
    })
    @DeleteMapping(value = "/address")
    public void updateUserAddress(@RequestHeader (name = "User-Token", required = false) String token,
                                  @RequestParam Long id)  {
        userService.removeUserAddress(id);
    }


    @ApiOperation(value = "suspend user account", nickname = "suspendUser")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "account suspended"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @PostMapping(value = "suspend")
    public void suspendUserAccount(@RequestHeader (name = "User-Token", required = false) String token,
                                   @RequestParam (value = "user_id")Long userId,
                                   @RequestParam (required = true, defaultValue = "false") Boolean suspend) {
        userService.suspendUserAccount(userId, suspend);
    }
}
