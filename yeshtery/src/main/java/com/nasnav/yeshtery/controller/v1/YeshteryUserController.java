package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.ActivationMethod;
import com.nasnav.dto.AddressDTO;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.ActivateOtpDto;
import com.nasnav.dto.request.user.ActivationEmailResendDTO;
import com.nasnav.dto.response.navbox.ProductRateRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.request.ImageBase64;
import com.nasnav.response.RecoveryUserResponse;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.CommonUserService;
import com.nasnav.service.EmployeeUserService;
import com.nasnav.service.ReviewService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.UserService;
import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dto.response.YeshteryUserApiResponse;
import com.nasnav.service.yeshtery.YeshteryUserService;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.nasnav.enumerations.YeshteryState.ACTIVE;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(YeshteryUserController.API_PATH)
@RequiredArgsConstructor
public class YeshteryUserController {
    static final String API_PATH = YeshteryConstants.API_PATH +"/user/";
    private final CommonUserService commonUserService;
    @Autowired
    private YeshteryUserService userService;
    @Autowired
    private UserService nasnavUserService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private EmployeeUserService employeeUserService;

    @GetMapping(value = "info")
    public UserRepresentationObject getUserData(@RequestHeader(name = "User-Token", required = false) String token,
                                                @RequestParam(value = "id", required = false) Long id,
                                                @RequestParam (value = "is_employee", required = false) Boolean isEmployee) throws BusinessException {
        return userService.getYeshteryUserData(id, isEmployee);
    }

    @PostMapping(value = "change/password", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public UserApiResponse changePasswordUser(@RequestHeader (name = "User-Token", required = false) String userToken, @RequestBody UserDTOs.ChangePasswordUserObject userJson) {
        return commonUserService.changePasswordUser(userJson);
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

    @PostMapping(value = "login")
    public UserApiResponse login(@RequestBody UserDTOs.UserLoginObject login, HttpServletResponse response) {

        UserApiResponse userApiResponse = securityService.login(login, ACTIVE);
        response.addCookie(userApiResponse.getCookie());
        return userApiResponse;
    }

    @PostMapping(value = "login/oauth2")
    public UserApiResponse oauth2Login(@RequestParam("token") String socialLoginToken) throws BusinessException {
        return securityService.socialLogin(socialLoginToken, true);
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
    public UserApiResponse logoutAll(HttpServletResponse response) {
        UserApiResponse userApiResponse = securityService.logoutAll();
        response.addCookie(userApiResponse.getCookie());
        return userApiResponse;
    }

    @PostMapping(value = "recover")
    public UserApiResponse recoverUser(@RequestBody UserDTOs.PasswordResetObject json) {
        if (json.employee){
            return employeeUserService.recoverUser(json);
        }
        return userService.recoverYeshteryUser(json);
    }

    @PostMapping(value = "subscribe")
    public void subscribeEmail(@RequestHeader(name = "User-Token", required = false) String token,
                               @RequestParam String email, @RequestParam("org_id") Long orgId) {
        userService.subscribeYeshteryEmail(email, orgId);
    }

    @GetMapping(value = "subscribe/activate")
    public RedirectView activateSubscribedEmail(@RequestParam String token,
                                                @RequestParam("org_id") Long orgId) {
        return userService.activateYeshterySubscribedEmail(token, orgId);
    }

    @PostMapping(value = "register")
    @ResponseStatus(CREATED)
    public YeshteryUserApiResponse registerUserV2( @RequestParam(required = false) Long referral,
                                                   @RequestBody UserDTOs.UserRegistrationObjectV2 userJson) throws BusinessException {
        return this.userService.registerYeshteryUserV2(referral, userJson);
    }

    @GetMapping(value = "register/activate")
    public RedirectView sendEmailRecovery(@RequestParam(value = "token") String token,
                                          @RequestParam(value = "redirect") String redirect) throws BusinessException {
        return userService.activateYeshteryUserAccount(token, redirect);
    }

    @PostMapping(value = "register/activate")
    public YeshteryUserApiResponse activateUser(@RequestParam(value = "token") String token) throws BusinessException {
        return userService.activateYeshteryUserAccount(token);
    }

    @PostMapping(value = "register/activate/resend")
    public void resendActivationEmail(@RequestBody ActivationEmailResendDTO accountInfo) throws BusinessException {
        userService.resendActivationYeshteryEmail(accountInfo);
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

    @PostMapping(value = "update", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public UserApiResponse updateEmployeeUser(@RequestHeader (name = "User-Token", required = false) String userToken,
                                              @RequestBody UserDTOs.EmployeeUserUpdatingObject json) {
        if (json.employee) {
            return employeeUserService.updateEmployeeUser(json);
        }
        return userService.updateUser(json);
    }

    @PostMapping("notification-token")
    public void updateNotificationToken(@RequestHeader(name = "User-Token") String userToken,
            @Schema(example = "YYYYYYYYYY:XXXXXXXXXXXX") @RequestBody String notificationToken) {
        securityService.setCurrentUserNotificationToken(userToken, notificationToken);
    }

    @PostMapping(value = "create", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public UserApiResponse createEmployeeUser(@RequestHeader (name = "User-Token", required = false) String userToken,
                                              @RequestBody UserDTOs.EmployeeUserCreationObject employeeUserJson) {
        return employeeUserService.createEmployeeUser(employeeUserJson);
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

    @GetMapping(value="/review", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductRateRepresentationObject> getVariantsRatings(@RequestHeader (name = "User-Token", required = false) String token,
                                                                    @RequestParam(value = "variant_ids") Set<Long> variantIds) {
        return reviewService.getUserProductsRatings(variantIds);
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
        nasnavUserService.suspendUserAccount(userId, suspend);
    }

    @PostMapping("link_nasnav_users_to_yeshtery_users")
    public int linkNonYeshteryUsersToCorrespondingYeshteryUserEntity(@RequestHeader (name = "User-Token", required = false) String token) {
        return userService.linkNonYeshteryUsersToCorrespondingYeshteryUserEntity();
    }

    @PostMapping(value = "register/otp/activate", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserApiResponse> activateUser(@Valid @RequestBody ActivateOtpDto activateOtp) throws BusinessException {
        return ResponseEntity.ok(userService.activateUserAccount(activateOtp));
    }

    @PostMapping(value = "/recovery/otp-verify", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<RecoveryUserResponse> verifyOtp(@Valid @RequestBody ActivateOtpDto activateOtp) throws BusinessException {
        return ResponseEntity.ok(userService.activateRecoveryOtp(activateOtp));
    }

    @PostMapping(value = "uploadUserAvatar")
    public UserApiResponse uploadUserAvatar(@RequestHeader(name = "User-Token", required = true) String token, @RequestBody @Valid ImageBase64 image)
            throws IOException {

        return this.nasnavUserService.processUserAvatar(image);
    }
}
