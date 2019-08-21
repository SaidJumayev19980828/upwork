package com.nasnav.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dto.OrganizationDTO;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.service.OrganizationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@RestController
@RequestMapping("/organization")
@Api(description = "Set of endpoints for adding, updating and deleting Dashboard data.")
@CrossOrigin("*") // allow all origins
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @ApiOperation(value = "add or update Organization data", nickname = "OrganizationModification", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "info", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = {"multipart/form-data"})
    public ResponseEntity updateOrganizationData(@RequestHeader(value = "User-ID") Long userId,
                                         @RequestHeader (value = "User-Token") String userToken,
                                         @RequestPart String jsonString,
                                         @RequestPart(value = "logo", required = false) @Valid MultipartFile file) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        OrganizationDTO.OrganizationModificationDTO json = mapper.readValue(jsonString, OrganizationDTO.OrganizationModificationDTO.class);
        OrganizationResponse response = organizationService.updateOrganizationData(userToken, json, file);
        return new ResponseEntity(response, response.getHttpStatus());
    }
}
