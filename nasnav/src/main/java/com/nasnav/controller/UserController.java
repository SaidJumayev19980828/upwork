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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping("/user")
@Tag(name = "Set of endpoints for registering and updating user data.")
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

    
    @Operation(description =  "Create a new employee user", summary = "employeeUserCreation")
    @ApiResponses(value = {@ApiResponse(responseCode = " 200" ,description = "account needs activation"),
                           @ApiResponse(responseCode = " 406" ,description = "Invalid data")})
    @PostMapping(value = "create",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public UserApiResponse createEmployeeUser(@RequestHeader (name = "User-Token", required = false) String userToken,
                                              @RequestBody UserDTOs.EmployeeUserCreationObject employeeUserJson) {
        return this.employeeUserService.createEmployeeUser(employeeUserJson);
    }


    @Operation(description =  "Send password recovery email to the user", summary = "userPasswordToken")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Password reset email sent"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @GetMapping(value = "recover",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void sendEmailRecovery(@RequestParam(value = "email") String email,
                                  @RequestParam(value = "org_id") Long orgId,
                                  @RequestParam(value = "employee") boolean employee) {
        if (employee) {
            employeeUserService.sendEmailRecovery(email, orgId);
        } else {
            userService.sendEmailRecovery(email, orgId);
        }
    }


    @Operation(description =  "Change user's password ", summary = "userPasswordReset")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Password changed"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @PostMapping(value = "recover",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public UserApiResponse recoverUser(@RequestBody UserDTOs.PasswordResetObject json) {
        if (json.employee){
            return employeeUserService.recoverUser(json);
        }
        return userService.recoverUser(json);
    }


    @Operation(description =  "Log in user", summary = "userLogin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "User logged in"),
            @ApiResponse(responseCode = " 401" ,description = "Invalid credentials"),
            @ApiResponse(responseCode = " 423" ,description = "Account unavailable"),
    })
    @PostMapping(value = "login",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public UserApiResponse login(@RequestBody UserDTOs.UserLoginObject login, HttpServletResponse response) {
        UserApiResponse userApiResponse = securityService.login(login);
        response.addCookie(userApiResponse.getCookie());
    	return userApiResponse;
    }


    @Operation(description =  "logout user", summary = "userLogout")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "User logged out"),
            @ApiResponse(responseCode = " 401" ,description = "Invalid credentials"),
            @ApiResponse(responseCode = " 423" ,description = "Account unavailable"),
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


    @Operation(description =  "logout user of all sessions", summary = "userLogoutAll")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "User logged out"),
            @ApiResponse(responseCode = " 401" ,description = "Invalid credentials"),
            @ApiResponse(responseCode = " 423" ,description = "Account unavailable"),
    })
    @PostMapping(value = "logout_all")
    public UserApiResponse logoutAll(@RequestHeader(name = "User-Token", required = false) String token,
                                  HttpServletResponse response) {
        UserApiResponse userApiResponse = securityService.logoutAll();
        response.addCookie(userApiResponse.getCookie());
        return userApiResponse;
    }
    

    @Operation(description =  "Update an employee user", summary = "employeeUserUpdate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "account updated successfully"),
            @ApiResponse(responseCode = " 406" ,description = "Insufficient rights "),
    })
    @PostMapping(value = "update",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public UserApiResponse updateEmployeeUser(@RequestHeader (name = "User-Token", required = false) String userToken,
                                              @RequestBody UserDTOs.EmployeeUserUpdatingObject json) {
        if (json.employee) {
            return this.employeeUserService.updateEmployeeUser(json);
        }
        return this.userService.updateUser(json);
    }


    
    @Operation(description =  "Get user info", summary = "userInfo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK"),
            @ApiResponse(responseCode = " 404" ,description = "User not found"),
    })
    @GetMapping(value = "info", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public UserRepresentationObject getUserData(@RequestHeader (name = "User-Token", required = false) String userToken,
                                      @RequestParam (value = "id", required = false) Long id
                                      ,@RequestParam (value = "is_employee", required = false) Boolean isEmployee) throws BusinessException{

        return userService.getUserData(id, isEmployee);
    }

    
    
    @Operation(description =  "Get employee users list", summary = "employeesInfo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK"),
            @ApiResponse(responseCode = " 403" ,description = "Customers doesn't have access to view Employee users data"),
    })
    @GetMapping(value = "list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(OK)
    public List<UserRepresentationObject> getUserList(@RequestHeader (name = "User-Token", required = false) String userToken,
                                      @RequestParam (value = "org_id", required = false) Long orgId,
                                      @RequestParam (value = "shop_id", required = false) Long storeId,
                                      @RequestParam (value = "role", required = false) String role) {
        return employeeUserService.getUserList(userToken, orgId, storeId, role);
    }


    @Operation(description =  "Get users list", summary = "usersList")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK"),
            @ApiResponse(responseCode = " 403" ,description = "Customer doesn't have access to view Customers users data"),
    })
    @GetMapping(value = "list/customer", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(OK)
    public List<UserRepresentationObject> getCustomersList(@RequestHeader (name = "User-Token", required = false) String userToken) {
        return userService.getUserList();
    }
    
    
    @Operation(description =  "Log in user using a social login token, "
    		+ "mainly used as a redirect destination at the end of the OAuth2 login process"
    		, summary = "userSocialLogin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "User logged in"),
            @ApiResponse(responseCode = " 401" ,description = "Invalid credentials"),
            @ApiResponse(responseCode = " 423" ,description = "Account unavailable"),
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


    @Operation(description =  "Register a new user (v2)", summary = "userRegisterV2")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 201" ,description = "User registered"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @PostMapping(value = "v2/register",
            produces = APPLICATION_JSON_UTF8_VALUE,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(CREATED)
    public UserApiResponse registerUserV2(
            @RequestBody UserDTOs.UserRegistrationObjectV2 userJson) throws BusinessException {
        return this.userService.registerUserV2(userJson);
    }


    @Operation(description =  "activate the user account", summary = "userActivation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Account activated"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @GetMapping(value = "v2/register/activate",
            produces = APPLICATION_JSON_UTF8_VALUE)
    public RedirectView sendEmailRecovery(@RequestParam(value = "token") String token,
                                          @RequestParam(value = "redirect") String redirect) throws BusinessException {
        return userService.activateUserAccount(token, redirect);
    }
    
    
    
    
    @Operation(description =  "activate the user account and login", summary = "userActivation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Account activated"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @PostMapping(value = "v2/register/activate",
            produces = APPLICATION_JSON_UTF8_VALUE)
    public UserApiResponse activateUser(@RequestParam(value = "token") String token) throws BusinessException {
        return userService.activateUserAccount(token);
    }
    
    
    
    
    
    @Operation(description =  "resend user activation email", summary = "userActivationResend")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Activation Email Sent"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @PostMapping(value = "v2/register/activate/resend")
    public void resendActivationEmail(@RequestBody ActivationEmailResendDTO accountInfo) throws BusinessException {
        userService.resendActivationEmail(accountInfo);
    }


    @Operation(description =  "add/update user address", summary = "userAddress")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Address added/updated"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @PutMapping(value = "/address")
    public AddressDTO updateUserAddress(@RequestHeader (name = "User-Token", required = false) String token,
                                        @RequestBody AddressDTO address)  {
        return userService.updateUserAddress(address);
    }


    @Operation(description =  "delete user address", summary = "deleteUserAddress")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Address removed"),
            @ApiResponse(responseCode = " 404" ,description = "Address not found"),
    })
    @DeleteMapping(value = "/address")
    public void updateUserAddress(@RequestHeader (name = "User-Token", required = false) String token,
                                  @RequestParam Long id)  {
        userService.removeUserAddress(id);
    }


    @Operation(description =  "suspend user account", summary = "suspendUser")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "account suspended"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @PostMapping(value = "suspend")
    public void suspendUserAccount(@RequestHeader (name = "User-Token", required = false) String token,
                                   @RequestParam (value = "user_id")Long userId,
                                   @RequestParam (defaultValue = "false") Boolean suspend,
                                   @RequestParam (name = "is_employee", defaultValue = "false") Boolean isEmployee) {
        if (isEmployee) {
            employeeUserService.suspendEmployeeAccount(userId, suspend);
        } else {
            userService.suspendUserAccount(userId, suspend);
        }
    }


    @Operation(description =  "subscribe via email", summary = "subscribeEmail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "email subscribed"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @PostMapping(value = "subscribe")
    public void subscribeEmail(@RequestParam String email, @RequestParam("org_id") Long orgId) {
        userService.subscribeEmail(email, orgId);
    }


    @Operation(description =  "activate subscribe via email", summary = "activateSubscription")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "activated email subscription"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @GetMapping(value = "subscribe/activate")
    public RedirectView activateSubscribedEmail(@RequestParam String token,
                                                @RequestParam("org_id") Long orgId) {
        return userService.activateSubscribedEmail(token, orgId);
    }
}
