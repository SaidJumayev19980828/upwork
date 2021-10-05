package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.user.ActivationEmailResendDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.SecurityService;
import com.nasnav.yeshtery.YeshteryConstants;
import com.nasnav.yeshtery.response.YeshteryUserApiResponse;
import com.nasnav.yeshtery.services.interfaces.YeshteryUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping(YeshteryUserController.API_PATH)
@Tag(name = "Yeshtery User management.")
public class YeshteryUserController {

    static final String API_PATH = YeshteryConstants.API_PATH +"/user/";

    private static final String OAUTH_ENTER_EMAIL_PAGE = "/user/login/oauth2/complete_registeration?token=";


    @Autowired
    private YeshteryUserService userService;

    @Autowired
    private SecurityService securityService;


    @GetMapping(value = "info")
    public UserRepresentationObject getUserData(@RequestHeader(name = "User-Token", required = false) String token,
                                                @RequestParam(value = "id", required = false) Long id,
                                                @RequestParam (value = "is_employee", required = false) Boolean isEmployee) throws BusinessException {
        return userService.getYeshteryUserData(id, isEmployee);
    }

    @PostMapping(value = "login")
    public UserApiResponse login(@RequestBody UserDTOs.UserLoginObject login, HttpServletResponse response) {
        if (securityService.getYeshteryState() == 1) {
            UserApiResponse userApiResponse = securityService.login(login);
            response.addCookie(userApiResponse.getCookie());
            return userApiResponse;
        }
        return null;
    }

    @PostMapping(value = "login/oauth2")
    public ResponseEntity<UserApiResponse> oauth2Login(@RequestParam("token") String socialLoginToken) throws BusinessException {
        if (securityService.getYeshteryState() != 1) {
            return null;
        }
        //
        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
        try {
            UserApiResponse body = securityService.socialLogin(socialLoginToken);
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
        if (securityService.getYeshteryState() == 1) {
            if (token == null || token.isEmpty())
                token = request.getCookies()[0].getValue();
            UserApiResponse userApiResponse = securityService.logout(token);
            response.addCookie(userApiResponse.getCookie());
            return userApiResponse;
        }
        return null;
    }

    @PostMapping(value = "logout_all")
    public UserApiResponse logoutAll(HttpServletResponse response) {
        if (securityService.getYeshteryState() == 1) {
            UserApiResponse userApiResponse = securityService.logoutAll();
            response.addCookie(userApiResponse.getCookie());
            return userApiResponse;
        }
        return null;
    }

    @PostMapping(value = "recover")
    public YeshteryUserApiResponse recoverUser(@RequestBody UserDTOs.PasswordResetObject json) {
        return userService.recoverYeshteryUser(json);
    }

    @PostMapping(value = "subscribe")
    public void subscribeEmail(@RequestHeader(name = "User-Token", required = false) String token,
                               @RequestParam String email, @RequestParam("org_id") Long orgId) {
        userService.subscribeYeshteryEmail(email, orgId);
    }

    @GetMapping(value = "subscribe/activate")
    public RedirectView activateSubscribedEmail(@RequestHeader(name = "User-Token", required = false) String token,
                                                @RequestParam("org_id") Long orgId) {
        return userService.activateYeshterySubscribedEmail(token, orgId);
    }

    @PostMapping(value = "register")
    @ResponseStatus(CREATED)
    public YeshteryUserApiResponse registerUserV2(
            @RequestBody UserDTOs.UserRegistrationObjectV2 userJson) throws BusinessException {
        return this.userService.registerYeshteryUserV2(userJson);
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

}
