package com.nasnav.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.OrganizationService;

@RestController
public class OrganizationController {

	private final OrganizationService organizationService;

	@Autowired
    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

	@GetMapping(value="/navbox/organization")
    @ResponseStatus(value = HttpStatus.OK)
    public @ResponseBody
    OrganizationRepresentationObject getOrganizationByName(@RequestParam(name="p_name",required=false) String organizationName,@RequestParam(name="org_id",required=false) Long organizationId) throws BusinessException {

		if(organizationName==null && organizationId==null)
			throw new BusinessException("Provide org_id or p_name request params", null, HttpStatus.BAD_REQUEST);
		
		if(organizationName!=null)
			return organizationService.getOrganizationByName(organizationName);
		
		return organizationService.getOrganizationById(organizationId);
    	
        
    }

}