package com.nasnav.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
public class TemplateController {


    /**
     * Used to load the change password html page to user to enter his new password
     *
     * @param token Token string sent to user via email.
     * @return html page name to be rendered.
     */
    @GetMapping(value = "recover")
    public String testHtml(@RequestParam(value = "token") String token){
        return "change_password";
    }


}
