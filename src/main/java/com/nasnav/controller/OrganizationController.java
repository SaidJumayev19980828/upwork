package com.nasnav.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dto.BrandDTO;
import com.nasnav.dto.OrganizationDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.service.OrganizationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @ApiOperation(value = "Get Organization brands data", nickname = "getBrands", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @GetMapping(value = "brands", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity getOrganizationBrands(@RequestParam(value = "org_id") Long orgId) {
        return new ResponseEntity(organizationService.getOrganizationBrands(orgId), HttpStatus.OK);
    }

    @ApiOperation(value = "add or update Organization brand", nickname = "BrandModification", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "brand", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = {"multipart/form-data"})
    public ResponseEntity updateOrganizationData(@RequestHeader(value = "User-ID") Long userId,
                                                 @RequestHeader (value = "User-Token") String userToken,
                                                 @RequestPart String jsonString,
                                                 @RequestPart(value = "logo", required = false) @Valid MultipartFile logo,
                                                 @RequestPart(value = "banner", required = false) @Valid MultipartFile banner) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        OrganizationResponse response = null;
        BrandDTO json = mapper.readValue(jsonString, BrandDTO.class);
        if (json.operation != null) {
            if (json.operation.equals("create")) {
                response = organizationService.createOrganizationBrand(userToken, json, logo, banner);
            } else if (json.operation.equals("update")) {
                response = organizationService.updateOrganizationBrand(userToken, json, logo, banner);
            } else
                throw new BusinessException("INVALID_PARAM: operation", "", HttpStatus.NOT_ACCEPTABLE);
        } else {
            throw new BusinessException("MISSING_PARAM: operation", "", HttpStatus.NOT_ACCEPTABLE);
        }
        return new ResponseEntity(response, response.getHttpStatus());
    }
}
