package com.nasnav.controller;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.nasnav.dto.UserDTOs.UserLoginObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.SecurityService;

import io.swagger.models.HttpMethod;

@RestController
@RequestMapping("/dirty_dashboard")
public class DirtyDashboardController {
	
	
	@Autowired
    private SecurityService securityService;
	
	
	@RequestMapping(value = "login_page" , method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView loginPage(@PathParam("msg") String msg, ModelMap model)	throws BusinessException {
		model.addAttribute("msg", msg);
		return  new ModelAndView("csv_upload_login", model);
    }
	
	
	
	
	@RequestMapping(value = "login" , method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView login(@RequestParam String username
    		, @RequestParam String password
    		, @RequestParam Long orgId 
    		, ModelMap model)	throws BusinessException {
		
		UserLoginObject loginObj = new UserLoginObject();
		loginObj.email = username;
		loginObj.employee = true;
		loginObj.orgId = orgId;
		loginObj.password = password;
		
		String token = null;
		try {
			token = securityService.login(loginObj).getToken();
			model.addAttribute("token", token);
	        return new ModelAndView("dashboard", model);
		}catch(BusinessException e) {
			model.addAttribute("msg", "invalid username - password - organization id combination!");
	        return new ModelAndView("csv_upload_login", model);
		}

    }
	
	
	
	
	
	@GetMapping(value = "dashboard")
    public ModelAndView dasboardPage(@RequestHeader("User-Token") String token, ModelMap model)	throws BusinessException {
		model.addAttribute("token", token);
		return  new ModelAndView("dashboard", model);
    }
	
	
	
	
	
	@GetMapping("upload_product_csv_form")
	public ModelAndView csvUploadPage(@RequestHeader("User-Token") String token, ModelMap model)	throws BusinessException {
		model.addAttribute("token", token);
        return new ModelAndView("upload_product_csv_form", model);
    }
}
