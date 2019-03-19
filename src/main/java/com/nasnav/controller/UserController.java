package com.nasnav.controller;

import com.nasnav.persistence.UserEntity;
import com.nasnav.response.ApiResponse;
import com.nasnav.response.ResponseStatus;
import com.nasnav.service.UserService;
import com.nasnav.dao.UserRepository;
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
        return new UserService(userRepository).registerUser(request);
    }

}
