package com.nasnav.controller;

import java.util.List;

import javax.validation.Valid;

import com.nasnav.dto.*;
import com.nasnav.response.ProductImageUpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.response.ProductFeatureUpdateResponse;
import com.nasnav.service.OrganizationService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/organization")
@Api(description = "Set of endpoints for adding, updating and deleting Dashboard data.")
@CrossOrigin("*") // allow all origins
public class OrganizationController {


    @Autowired
    private OrganizationService orgService;

    public OrganizationController(OrganizationService orgService) {
        this.orgService = orgService;
    }
    
    @ApiOperation(value = "add or update Organization data", nickname = "OrganizationModification", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "info", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = {"multipart/form-data"})
    public ResponseEntity updateOrganizationData(@RequestHeader (value = "User-Token") String userToken,
                                                 @RequestPart String jsonString,
                                                 @RequestPart(value = "logo", required = false) @Valid MultipartFile file) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        OrganizationDTO.OrganizationModificationDTO json = mapper.readValue(jsonString, OrganizationDTO.OrganizationModificationDTO.class);
        OrganizationResponse response = orgService.updateOrganizationData(userToken, json, file);
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
        return new ResponseEntity(orgService.getOrganizationBrands(orgId), HttpStatus.OK);
    }






    @ApiOperation(value = "add or update Organization brand", nickname = "BrandModification", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "brand", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = {"multipart/form-data"})
    public ResponseEntity updateOrganizationData(@RequestHeader (value = "User-Token") String userToken,
                                                 @RequestPart String jsonString,
                                                 @RequestPart(value = "logo", required = false) @Valid MultipartFile logo,
                                                 @RequestPart(value = "banner", required = false) @Valid MultipartFile banner) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        OrganizationResponse response = null;
        BrandDTO json = mapper.readValue(jsonString, BrandDTO.class);
        if (json.operation != null) {
            if (json.operation.equals("create")) {
                response = orgService.createOrganizationBrand(userToken, json, logo, banner);
            } else if (json.operation.equals("update")) {
                response = orgService.updateOrganizationBrand(userToken, json, logo, banner);
            } else
                throw new BusinessException("INVALID_PARAM: operation", "", HttpStatus.NOT_ACCEPTABLE);
        } else {
            throw new BusinessException("MISSING_PARAM: operation", "", HttpStatus.NOT_ACCEPTABLE);
        }
        return new ResponseEntity(response, response.getHttpStatus());
    }
    
    
    
    

    @ApiOperation(value = "get product features for organization", nickname = "GetOrgProductFeatures", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @GetMapping(value = "products_features", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<ProductFeatureDTO> updateOrganizationData(@RequestParam("organization_id") Long orgId) throws Exception {
    	return orgService.getProductFeatures(orgId);
    }
    
    
    
    
    @ApiOperation(value = "add/update product features for organization", nickname = "PostOrgProductFeatures", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "products_feature"
    			, produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    			, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ProductFeatureUpdateResponse updateProductFeature(@RequestBody ProductFeatureUpdateDTO featureDto) throws Exception {
    	return orgService.updateProductFeature(featureDto);
    }
}
