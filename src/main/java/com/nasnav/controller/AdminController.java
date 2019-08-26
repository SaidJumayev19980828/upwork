package com.nasnav.controller;

import com.nasnav.dto.OrganizationDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.service.OrganizationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@Api(description = "Set of endpoints for adding, updating and deleting Dashboard data.")
@CrossOrigin("*") // allow all origins
public class AdminController {

    @Autowired
    private OrganizationService organizationService;

    public AdminController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @ApiOperation(value = "Create an Organization", nickname = "OrganizationCreation", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "organization", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity createOrganization(@RequestHeader (value = "User-ID") Long userId,
                                         @RequestHeader (value = "User-Token") String userToken,
                                         @RequestBody OrganizationDTO.OrganizationCreationDTO json) throws BusinessException {
        OrganizationResponse response = organizationService.createOrganization(json);
        return new ResponseEntity(response, response.getHttpStatus());
    }
}
