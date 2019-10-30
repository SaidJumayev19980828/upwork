package com.nasnav.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;

import com.nasnav.dto.OrganizationDTO;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.nasnav.dto.UserDTOs.UserLoginObject;
import com.nasnav.enumerations.Roles;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.EmployeeUserService;
import com.nasnav.service.SecurityService;

@RestController
@RequestMapping("/dirty_dashboard")
public class DirtyDashboardController {
	
	
	@Autowired
    private SecurityService securityService;
	
	
	@Autowired
	private EmployeeUserService empService;
	
	@GetMapping(value = "login_page")
    public ModelAndView loginPage(@PathParam("msg") String msg, ModelMap model)	throws BusinessException {
		model.addAttribute("msg", msg);
		return  new ModelAndView("csv_upload_login", model);
    }
	
	
	
	
	@PostMapping(value = "login")
    public ModelAndView login(@RequestParam String username
    		, @RequestParam String password
    		, @RequestParam Long orgId 
    		, ModelMap model
    		, HttpServletRequest request
    		, HttpServletResponse response)	throws BusinessException {
		
		UserLoginObject loginObj = new UserLoginObject();
		loginObj.email = username;
		loginObj.employee = true;
		loginObj.orgId = orgId;
		loginObj.password = password;
		
		String token = null;
		try {
			UserApiResponse loginDetails = securityService.login(loginObj);
			token = loginDetails.getToken();			
			BaseUserEntity user = empService.getUserById(loginDetails.getEntityId());
			return prepareDasboardPage(token, model, user);
		}catch(BusinessException e) {
			String msg = "invalid username - password - organization id combination!";
	        return new ModelAndView("/dirty_dashboard/login_page?msg=" + msg);
		}

    }
	
	
	
	
	
	@GetMapping(value = "dashboard")
    public ModelAndView dasboardPage(@RequestHeader("User-Token") String token, ModelMap model)	throws BusinessException {
		BaseUserEntity user = securityService.getCurrentUser();
		return prepareDasboardPage(token, model, user);
    }
	
	
	
	
	
	
	private ModelAndView prepareDasboardPage(String token, ModelMap model, BaseUserEntity user) {
		Boolean isNasnavAdmin = securityService.userHasRole(user, Roles.NASNAV_ADMIN);
		Boolean isOrgAdmin = securityService.userHasRole(user, Roles.ORGANIZATION_ADMIN);
		
		model.addAttribute("isNasnavAdmin", isNasnavAdmin);
		model.addAttribute("isOrgAdmin", isOrgAdmin);
		model.addAttribute("token", token);		
		return  new ModelAndView("dashboard", model);
	}
	
	
	
	
	@GetMapping("upload_product_csv_form")
	public ModelAndView csvUploadPage(@RequestHeader("User-Token") String token, ModelMap model)	throws BusinessException {
		model.addAttribute("token", token);
        return new ModelAndView("upload_product_csv_form", model);
    }
	
	
	@GetMapping("org_mgr")
	public ModelAndView orgMgrPage(@RequestHeader("User-Token") String token, ModelMap model)	throws BusinessException {
		model.addAttribute("token", token);
        return new ModelAndView("org_mgr", model);
    }

	@GetMapping("org_info")
	public ModelAndView orgInfoPage(@RequestHeader("User-Token") String token, ModelMap model)	throws BusinessException {
		Long organization_id = securityService.getCurrentUserOrganizationId();

		model.addAttribute("org_id", organization_id);
		model.addAttribute("token", token);
		return new ModelAndView("org_info", model);
	}

	@GetMapping("brand_mgr")
	public ModelAndView brandMgrPage(@RequestHeader("User-Token") String token, ModelMap model)	throws BusinessException {
		model.addAttribute("token", token);
		return new ModelAndView("brand_mgr", model);
	}

	@GetMapping("cat_mgr")
	public ModelAndView catMgrPage(@RequestHeader("User-Token") String token, ModelMap model)	throws BusinessException {
		model.addAttribute("token", token);
		return new ModelAndView("cat_mgr", model);
	}

	@GetMapping("products_feature")
	public ModelAndView prodFeaturePage(@RequestHeader("User-Token") String token, ModelMap model)	throws BusinessException {
		model.addAttribute("token", token);
		return new ModelAndView("products_feature", model);
	}

	@GetMapping("upload_product_image_csv_form")
	public ModelAndView csvUploadImgPage(@RequestHeader("User-Token") String token, ModelMap model)	throws BusinessException {
		model.addAttribute("token", token);
		return new ModelAndView("upload_product_image_csv_form", model);
	}

	@GetMapping("file_upload")
	public ModelAndView fileUploadPage(@RequestHeader("User-Token") String token, ModelMap model)	throws BusinessException {
		BaseUserEntity user = securityService.getCurrentUser();
		Boolean isNasnavAdmin = securityService.userHasRole(user, Roles.NASNAV_ADMIN);
		Long organization_id = securityService.getCurrentUserOrganizationId();

		model.addAttribute("isNasnavAdmin", isNasnavAdmin);
		model.addAttribute("organization_id", organization_id);
		model.addAttribute("token", token);
		return new ModelAndView("file_upload", model);
	}
}
