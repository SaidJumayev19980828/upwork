package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.AddressDTO;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.user.ActivationEmailResendDTO;
import com.nasnav.dto.response.navbox.ProductRateRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.EmployeeUserService;
import com.nasnav.service.ReviewService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.UserService;
import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dto.response.YeshteryUserApiResponse;
import com.nasnav.service.yeshtery.YeshteryUserService;
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
import java.util.Set;

import static com.nasnav.enumerations.YeshteryState.ACTIVE;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(YeshteryUserController.API_PATH)
public class YeshteryUserController {

    static final String API_PATH = YeshteryConstants.API_PATH +"/user/";

    private static final String OAUTH_ENTER_EMAIL_PAGE = "/user/login/oauth2/complete_registeration?token=";


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
    public ResponseEntity<UserApiResponse> oauth2Login(@RequestParam("token") String socialLoginToken) throws BusinessException {
       //
        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
        try {
            UserApiResponse body = securityService.socialLogin(socialLoginToken, true);
            return response.body(body);
        }catch(Exception e) {
            //change it to forward to a server rendered page
            return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                    .header(HttpHeaders.LOCATION, OAUTH_ENTER_EMAIL_PAGE + socialLoginToken)
                    .build();
        }
    }

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

    @PostMapping(value = "create", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public UserApiResponse createEmployeeUser(@RequestHeader (name = "User-Token", required = false) String userToken,
                                              @RequestBody UserDTOs.EmployeeUserCreationObject employeeUserJson) {
        return employeeUserService.createEmployeeUser(employeeUserJson);
    }

    @GetMapping(value = "recover", produces = APPLICATION_JSON_VALUE)
    public void sendEmailRecovery(@RequestParam String email,
                                  @RequestParam(value = "org_id", required = false) Long orgId,
                                  @RequestParam() boolean employee) {
        if (employee) {
            employeeUserService.sendEmailRecovery(email);
        } else {
            userService.sendEmailRecovery(email, orgId);
        }
    }

    @GetMapping(value="/review", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductRateRepresentationObject> getVariantsRatings(@RequestHeader (name = "User-Token", required = false) String token,
                                                                    @RequestParam(value = "variant_ids") Set<Long> variantIds) {
        return reviewService.getUserProductsRatings(variantIds);
    }

    @PostMapping(value = "suspend")
    public void suspendUserAccount(@RequestHeader (name = "User-Token", required = false) String token,
                                   @RequestParam (value = "user_id")Long userId,
                                   @RequestParam (defaultValue = "false") Boolean suspend,
                                   @RequestParam (name = "is_employee", defaultValue = "false") Boolean isEmployee) {
        if (isEmployee) {
            employeeUserService.suspendEmployeeAccount(userId, suspend);
        } else {
            nasnavUserService.suspendUserAccount(userId, suspend);
        }
    }

    @PostMapping("link_nasnav_users_to_yeshtery_users")
    public int linkNonYeshteryUsersToCorrespondingYeshteryUserEntity(@RequestHeader (name = "User-Token", required = false) String token) {
        return userService.linkNonYeshteryUsersToCorrespondingYeshteryUserEntity();
    }
}
