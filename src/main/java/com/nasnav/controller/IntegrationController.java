package com.nasnav.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.dto.OrganizationIntegrationInfoDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.IntegrationService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/integration")
@Api(description = "Set of endpoints for adding, updating and deleting Dashboard data.")
@CrossOrigin("*") // allow all origins
public class IntegrationController {
	
	
	@Autowired
	IntegrationService integrationSrv;
	
	
	@ApiOperation(value = "Register a new integration module for an organization", nickname = "IntegrationModuleRegister", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "module", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void registerIntegrationModule(@RequestHeader (value = "User-Token") String userToken,
                                             @RequestBody OrganizationIntegrationInfoDTO integrationInfo)  throws BusinessException {
		integrationSrv.registerIntegrationModule(integrationInfo);
    }
}
