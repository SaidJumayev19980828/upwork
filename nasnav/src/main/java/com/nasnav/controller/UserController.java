package com.nasnav.controller;

import com.nasnav.dto.ActivationMethod;
import com.nasnav.dto.AddressDTO;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.ActivateOtpDto;
import com.nasnav.dto.request.user.ActivationEmailResendDTO;
import com.nasnav.dto.response.navbox.ProductRateRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.response.RecoveryUserResponse;
import com.nasnav.response.UserApiResponse;
import com.nasnav.security.oauth2.exceptions.InCompleteOAuthRegistration;
import com.nasnav.service.CommonUserService;
import com.nasnav.service.EmployeeUserService;
import com.nasnav.service.ReviewService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Set;

import static com.nasnav.enumerations.YeshteryState.DISABLED;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("/user")
@CrossOrigin("*") // allow all origins
@RequiredArgsConstructor
public class UserController {
    private static final String OAUTH_ENTER_EMAIL_PAGE = "/user/login/oauth2/complete_registeration?token=";
    private final CommonUserService commonUserService;
    @Autowired
	private UserService userService;
    @Autowired
    private EmployeeUserService employeeUserService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private ReviewService reviewService;

    @PostMapping(value = "create", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public UserApiResponse createEmployeeUser(@RequestHeader (name = "User-Token", required = false) String userToken,
                                              @RequestBody UserDTOs.EmployeeUserCreationObject employeeUserJson) {
        return employeeUserService.createEmployeeUser(employeeUserJson);
    }

    @PostMapping(value = "change/password", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public UserApiResponse changePasswordUser(@RequestHeader (name = "User-Token", required = false) String userToken, @RequestBody UserDTOs.ChangePasswordUserObject userJson) {
        return commonUserService.changePasswordUser(userJson);
    }

    @GetMapping(value = "recover", params = "employee=true", produces = APPLICATION_JSON_VALUE)
    public void sendEmailRecoveryToEmplyee(@RequestParam String email,
            @RequestParam(value = "org_id", required = false) Long orgId,
            @RequestParam boolean employee) {
        employeeUserService.sendEmailRecovery(email);
    }

    @GetMapping(value = "recover", produces = APPLICATION_JSON_VALUE)
    public void sendEmailRecoveryToUser(@RequestParam String email,
            @RequestParam(value = "org_id", required = false) Long orgId,
            @RequestParam boolean employee,
            @RequestParam(value = "activation_method", defaultValue = "VERIFICATION_LINK") ActivationMethod activationMethod) {
        userService.sendEmailRecovery(email, orgId, activationMethod);
    }

    @PostMapping(value = "recover", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public UserApiResponse recoverUser(@RequestBody UserDTOs.PasswordResetObject json) {
        if (json.employee){
            return employeeUserService.recoverUser(json);
        }
        return userService.recoverUser(json);
    }

    @PostMapping(value = "login", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public UserApiResponse login(@RequestBody UserDTOs.UserLoginObject login, HttpServletResponse response) {
        UserApiResponse userApiResponse = securityService.login(login, DISABLED);
        response.addCookie(userApiResponse.getCookie());
    	return userApiResponse;
    }

    @PostMapping(value = "logout")
    public UserApiResponse logout(@RequestHeader(name = "User-Token", required = false) String headerToken,
            @CookieValue(name = "User-Token", required = false) String cookieToken,
            HttpServletResponse response) {
        UserApiResponse userApiResponse = securityService.logout(headerToken, cookieToken);
        response.addCookie(userApiResponse.getCookie());
        return userApiResponse;
    }


    @PostMapping(value = "logout_all")
    public UserApiResponse logoutAll(@RequestHeader(name = "User-Token", required = false) String token,
                                     HttpServletResponse response) {
        UserApiResponse userApiResponse = securityService.logoutAll();
        response.addCookie(userApiResponse.getCookie());
        return userApiResponse;
    }

    @PostMapping(value = "update", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public UserApiResponse updateEmployeeUser(@RequestHeader (name = "User-Token", required = false) String userToken,
                                              @RequestBody UserDTOs.EmployeeUserUpdatingObject json) {
        if (json.employee) {
            return this.employeeUserService.updateEmployeeUser(json);
        }
        return this.userService.updateUser(json);
    }

    @GetMapping(value = "info", produces = APPLICATION_JSON_VALUE)
    public UserRepresentationObject getUserData(@RequestHeader (name = "User-Token", required = false) String userToken,
                                                @RequestParam (value = "id", required = false) Long id,
                                                @RequestParam (value = "is_employee", required = false, defaultValue = "false") Boolean isEmployee) throws BusinessException{
        return userService.getUserData(id, isEmployee);
    }

    @GetMapping(value = "list", produces = APPLICATION_JSON_VALUE)
    public List<UserRepresentationObject> getUserList(@RequestHeader (name = "User-Token", required = false) String userToken,
                                      @RequestParam (value = "org_id", required = false) Long orgId,
                                      @RequestParam (value = "shop_id", required = false) Long storeId,
                                      @RequestParam (value = "role", required = false) String role) {
        return employeeUserService.getUserList(userToken, orgId, storeId, role);
    }

    @GetMapping(value = "list/customer", produces = APPLICATION_JSON_VALUE)
    public List<UserRepresentationObject> getCustomersList(@RequestHeader (name = "User-Token", required = false) String userToken) {
        return userService.getUserList();
    }
    
    
    @Operation(description = "Log in user using a social login token, "
            + "mainly used as a redirect destination at the end of the OAuth2 login process", summary = "userSocialLogin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200", description = "User logged in"),
            @ApiResponse(responseCode = " 401", description = "Invalid credentials"),
            @ApiResponse(responseCode = " 423", description = "Account unavailable"),
    })
    @PostMapping(value = "login/oauth2", produces = APPLICATION_JSON_VALUE)
    public UserApiResponse oauth2Login(@RequestParam("token") String socialLoginToken) throws BusinessException {
        return securityService.socialLogin(socialLoginToken, false);
    }
    @PostMapping(value = "v2/register", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(CREATED)
    public UserApiResponse registerUserV2(@RequestBody UserDTOs.UserRegistrationObjectV2 userJson,
            @RequestParam(required = false) Long referrer) throws BusinessException {
        return this.userService.registerUserReferral(userJson, referrer);
    }

    @GetMapping(value = "v2/register/activate", produces = APPLICATION_JSON_VALUE)
    public RedirectView sendEmailRecovery(@RequestParam(value = "token") String token,
                                          @RequestParam(value = "redirect") String redirect) throws BusinessException {
        return userService.activateUserAccount(token, redirect);
    }

    @PostMapping(value = "v2/register/activate", produces = APPLICATION_JSON_VALUE)
    public UserApiResponse activateUser(@RequestParam(value = "token") String token) throws BusinessException {
        return userService.activateUserAccount(token);
    }

    @PostMapping(value = "v2/register/activate/resend", consumes = APPLICATION_JSON_VALUE)
    public void resendActivationEmail(@RequestBody ActivationEmailResendDTO accountInfo) throws BusinessException {
        userService.resendActivationEmail(accountInfo);
    }

    @PutMapping(value = "/address", produces = APPLICATION_JSON_VALUE)
    public AddressDTO updateUserAddress(@RequestHeader (name = "User-Token", required = false) String token,
                                        @RequestBody AddressDTO address)  {
        return userService.updateUserAddress(address);
    }

    @DeleteMapping(value = "/address")
    public void updateUserAddress(@RequestHeader (name = "User-Token", required = false) String token,
                                  @RequestParam Long id)  {
        userService.removeUserAddress(id);
    }

    @PostMapping(value = "suspend", params = "is_employee=true")
    public void suspendEmployeeAccount(@RequestHeader(name = "User-Token", required = false) String token,
            @RequestParam(value = "user_id") Long userId,
            @RequestParam(defaultValue = "false") Boolean suspend,
            @RequestParam(name = "is_employee", defaultValue = "false") Boolean isEmployee) {
        employeeUserService.suspendEmployeeAccount(userId, suspend);
    }

    @PostMapping(value = "suspend")
    public void suspendUserAccount(@RequestHeader(name = "User-Token", required = false) String token,
            @RequestParam(value = "user_id") Long userId,
            @RequestParam(defaultValue = "false") Boolean suspend,
            @RequestParam(name = "is_employee", defaultValue = "false") Boolean isEmployee) {
        userService.suspendUserAccount(userId, suspend);
    }

    @PostMapping(value = "subscribe")
    public void subscribeEmail(@RequestParam String email, @RequestParam("org_id") Long orgId) {
        userService.subscribeEmail(email, orgId);
    }

    @GetMapping(value = "subscribe/activate")
    public RedirectView activateSubscribedEmail(@RequestParam String token,
                                                @RequestParam("org_id") Long orgId) {
        return userService.activateSubscribedEmail(token, orgId);
    }

    @GetMapping(value="/review", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductRateRepresentationObject> getVariantsRatings(@RequestHeader (name = "User-Token", required = false) String token,
                                                                   @RequestParam(value = "variant_ids") Set<Long> variantIds) {
        return reviewService.getUserProductsRatings(variantIds);
    }

    @PostMapping(value = "v2/register/otp/activate", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserApiResponse> activateUser(@Valid @RequestBody ActivateOtpDto activateOtp) throws BusinessException {
        return ResponseEntity.ok(userService.activateUserAccount(activateOtp));
    }

    @PostMapping(value = "v2/employee/otp/activate", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserApiResponse> activateEmployeeUser(@Valid @RequestBody ActivateOtpDto activateOtp) throws BusinessException {
        return ResponseEntity.ok(employeeUserService.activateUserAccount(activateOtp));
    }

    @PostMapping(value = "/recovery/otp-verify", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<RecoveryUserResponse> verifyOtp(@Valid @RequestBody ActivateOtpDto activateOtp) throws BusinessException {
        return ResponseEntity.ok(userService.activateRecoveryOtp(activateOtp));
    }
    @PostMapping(value = "uploadAvatar", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
    public UserApiResponse uploadUserAvatar(@RequestHeader(name = "User-Token", required = false) String token, @RequestPart("file") @Valid MultipartFile file)
            throws BusinessException, ImportProductException {
        return this.userService.updateUserAvatar(file);
    }
}
