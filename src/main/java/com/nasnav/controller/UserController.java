package com.nasnav.controller;

import com.nasnav.persistence.User;
import com.nasnav.response.ApiResponse;
import com.nasnav.response.ResponseStatus;
import com.nasnav.service.UserService;
import com.nasnav.dao.UserRepository;
import com.nasnav.service.UserServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserServiceI userService;


    @PostMapping("/register")
    public String registerUser(HttpServletRequest request)
    {
        return new UserService().register(userRepository, request.getParameterMap());
    }

    /**
     * API for registering new user.
     *
     * @param request JSON object contain name and email.
     * @return ApiResponse object which is either success or fail response.
     */
    @RequestMapping(value = "register",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ApiResponse createUser(@RequestBody String request) {
        User user = this.userService.registerUser(request);
        return new ApiResponse(user.getId(), Arrays.asList(ResponseStatus.NEED_ACTIVATION, ResponseStatus.ACTIVATION_SENT));
    }

}
