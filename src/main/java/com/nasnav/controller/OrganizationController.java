package com.nasnav.controller;

import java.util.List;

import javax.validation.Valid;

import com.nasnav.dto.*;
import com.nasnav.persistence.TagsEntity;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.response.TagResponse;
import com.nasnav.service.CategoryService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @Autowired
    private CategoryService categoryService;

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
                                                 @RequestPart("properties") String jsonString,
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
    public ResponseEntity updateBrandData(@RequestHeader (value = "User-Token") String userToken,
                                                 @RequestPart("properties") String jsonString,
                                                 @RequestPart(value = "logo", required = false) @Valid MultipartFile logo,
                                                 @RequestPart(value = "banner", required = false) @Valid MultipartFile banner) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        OrganizationResponse response = null;
        BrandDTO json = mapper.readValue(jsonString, BrandDTO.class);
        if (json.operation != null) {
            if (json.operation.equals("create")) {
                response = orgService.createOrganizationBrand(json, logo, banner);
            } else if (json.operation.equals("update")) {
                response = orgService.updateOrganizationBrand(json, logo, banner);
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
    public List<ProductFeatureDTO> updateOrganizationFeaturesData(@RequestParam("organization_id") Long orgId) throws Exception {
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


    @ApiOperation(value = "add/update organization images", nickname = "PostOrgImg", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "image"
            , produces = MediaType.APPLICATION_JSON_UTF8_VALUE
            , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductImageUpdateResponse updateOrganizationImage(@RequestHeader("User-Token") String token,
                                                           @RequestPart(value = "image", required = false) @Valid MultipartFile file,
                                                           @RequestPart("properties") @Valid String jsonString) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        OrganizationImageUpdateDTO imgMetaData = mapper.readValue(jsonString, OrganizationImageUpdateDTO.class);
        return orgService.updateOrganizationImage(file, imgMetaData);
    }


    @ApiOperation(value = "delete image for organization", nickname = "organization image delete")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Organization image deleted"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insufficient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = "image", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public boolean deleteProductImage(@RequestHeader("User-Token") String token,
                                      @RequestParam("image_id") @Valid Long imageId) throws BusinessException {
        return  orgService.deleteImage(imageId);
    }

    @ApiOperation(value = "Create or update Organization tag", nickname = "orgTagModification", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "tag", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity createOrganizationTag(@RequestHeader (value = "User-Token") String userToken,
                                         @RequestBody TagsDTO tagDTO) throws BusinessException {
        TagsEntity tag = categoryService.createOrgTag(tagDTO);
        return new ResponseEntity(new TagResponse(tag.getId()),HttpStatus.OK);
    }

    @ApiOperation(value = "Delete Organization tag", nickname = "orgTagDeletion", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @DeleteMapping(value = "tag", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity deleteOrganizationTag(@RequestHeader (value = "User-Token") String userToken,
                                                @RequestParam (value = "tag_id")Long tagId) throws BusinessException {
        TagResponse tag = categoryService.deleteOrgTag(tagId);
        return new ResponseEntity(tag, HttpStatus.OK);
    }

    @ApiOperation(value = "Add children to parent tag", nickname = "addTagsLinks", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "tag/link", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity createTagChildren(@RequestHeader (value = "User-Token") String userToken,
                                            @RequestBody TagsLinkDTO tagsLinks) throws BusinessException {
        categoryService.createTagEdges(tagsLinks);
        return new ResponseEntity(new JSONObject("{\"Message\":\"Children created successfully\"}").toString(),HttpStatus.OK);
    }


    @ApiOperation(value = "Delete children from parent tag", nickname = "deleteTagsLinks", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @DeleteMapping(value = "tag/link", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity deleteTagChildren(@RequestHeader (value = "User-Token") String userToken,
                                            @RequestBody TagsLinkDTO tagsLinks) throws BusinessException {
        categoryService.deleteTagLink(tagsLinks);
        return new ResponseEntity(new JSONObject("{\"Message\":\"Children removed from parent successfully\"}").toString(),HttpStatus.OK);
    }

}
