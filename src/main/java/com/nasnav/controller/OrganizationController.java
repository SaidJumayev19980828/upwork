package com.nasnav.controller;

import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/navbox")
public class OrganizationController {


    private final OrganizationService organizationService;

    @Autowired
    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/organization/{requested_name}")
    @ResponseStatus(value = HttpStatus.OK)
    public @ResponseBody
    OrganizationRepresentationObject getOrganizationByName(@PathVariable("requested_name") String organizationName) throws BusinessException {

        return organizationService.getOrganizationByName(organizationName);
    }

    @GetMapping("/categories/{organization_id}")
    public ResponseEntity<?> getOrganizationCategories(@PathVariable("organization_id") Long organizationId) throws BusinessException {

        return null;
    }

    @GetMapping("/shopRepresentationObjects/{organization_id}")
    public ResponseEntity<?> getOrganizationShops(@PathVariable("organization_id") Long organizationId) throws BusinessException {

        return null;
    }
}
