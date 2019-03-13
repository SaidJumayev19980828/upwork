package com.nasnav.controller;

import com.nasnav.service.UserService;
import com.nasnav.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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
}
