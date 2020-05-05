package com.nasnav.controller;

import static org.springframework.http.HttpStatus.OK;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.validation.Valid;

import com.nasnav.AppConfig;
import com.nasnav.dao.OrganizationPaymentGatewaysRepository;
import com.nasnav.dto.*;
import com.nasnav.payments.mastercard.MastercardAccount;
import com.nasnav.payments.misc.Tools;
import com.nasnav.payments.upg.UpgAccount;
import com.nasnav.persistence.OrganizationPaymentGatewaysEntity;
import com.nasnav.service.BrandService;
import com.nasnav.service.ThemeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.TagsEntity;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.response.ProductFeatureUpdateResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.response.TagResponse;
import com.nasnav.service.CategoryService;
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
    private AppConfig config;

    @Autowired
    private OrganizationService orgService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OrganizationPaymentGatewaysRepository orgPaymentGatewaysRep;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private BrandService brandService;

    private Logger classLogger = LogManager.getLogger(OrganizationController.class);


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
    public OrganizationResponse updateOrganizationData(@RequestHeader (value = "User-Token") String userToken,
                                                 @RequestPart("properties") String jsonString,
                                                 @RequestPart(value = "logo", required = false) @Valid MultipartFile file) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        OrganizationDTO.OrganizationModificationDTO json = mapper.readValue(jsonString, OrganizationDTO.OrganizationModificationDTO.class);
        return orgService.updateOrganizationData(json, file);
    }






    @ApiOperation(value = "Get Organization brands data", nickname = "getBrands", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @GetMapping(value = "brands", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<?>> getOrganizationBrands(@RequestParam(value = "org_id") Long orgId) {
        return new ResponseEntity<List<?>>(orgService.getOrganizationBrands(orgId), HttpStatus.OK);
    }






    @ApiOperation(value = "add or update Organization brand", nickname = "BrandModification", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "brand", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = {"multipart/form-data"})
    public OrganizationResponse updateBrandData(@RequestHeader (value = "User-Token") String userToken,
                                                 @RequestPart("properties") String jsonString,
                                                 @RequestPart(value = "logo", required = false) @Valid MultipartFile logo,
                                                 @RequestPart(value = "banner", required = false) @Valid MultipartFile banner) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BrandDTO json = mapper.readValue(jsonString, BrandDTO.class);
        return orgService.validateAndUpdateBrand(json, logo, banner);
    }


    @DeleteMapping(value = "brand")
    @ResponseStatus(OK)
    public void deleteBrand(@RequestHeader (value = "User-Token") String userToken,
                            @RequestParam("brand_id") Long brandId) throws BusinessException {
        brandService.deleteBrand(brandId);

    }



    @ApiOperation(value = "get product features for organization", nickname = "GetOrgProductFeatures", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @GetMapping(value = "products_features", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<ProductFeatureDTO> getOrganizationFeaturesData(@RequestParam("organization_id") Long orgId) throws Exception {
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
    public ProductFeatureUpdateResponse updateProductFeature(@RequestHeader("User-Token") String token
    		, @RequestBody ProductFeatureUpdateDTO featureDto) throws Exception {
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
    public TagResponse updateOrganizationTag(@RequestHeader (value = "User-Token") String userToken,
                                         @RequestBody TagsDTO tagDTO) throws BusinessException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        tagDTO.setHasCategory(true);
    	TagsEntity tag = categoryService.createOrUpdateTag(tagDTO);
        return new TagResponse(tag.getId());
    }


    @ApiOperation(value = "Delete Organization tag", nickname = "orgTagDeletion", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @DeleteMapping(value = "tag", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<TagResponse> deleteOrganizationTag(@RequestHeader (value = "User-Token") String userToken,
                                                @RequestParam (value = "tag_id")Long tagId) throws BusinessException {
        TagResponse tag = categoryService.deleteOrgTag(tagId);
        return new ResponseEntity<TagResponse>(tag, HttpStatus.OK);
    }

    
    
    
    
    @ApiOperation(value = "create a new tag tree", nickname = "createTagTree", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "tag/tree", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(OK)
    public void createTagTree(@RequestHeader (value = "User-Token") String userToken,
                                            @RequestBody TagsTreeCreationDTO tree) throws BusinessException {
        categoryService.createTagTree(tree);
    }


    @ApiOperation(value = "Assign category to list of tags", nickname = "assignTagsCategory", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to \n" +
                    "    //TODO: >>> use separate DTO that shows theme info + settings, as both the default settings and current settings should\n" +
                    "    //be returned.do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "tag/category")
    @ResponseStatus(OK)
    public void assignTagsCategory(@RequestHeader (value = "User-Token") String userToken,
                                  @RequestParam("category_id") Long categoryId,
                                  @RequestParam(value = "tags", required = false) List<Long> tagsIds) throws BusinessException {
        categoryService.assignTagsCategory(categoryId, tagsIds);
    }


    @ApiOperation(value = "get theme classes assigned a certain organization", nickname = "GetOrgThemeClasses", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @GetMapping(value = "themes/class", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<ThemeClassDTO> getOrgThemeClasses(@RequestHeader (value = "User-Token") String userToken,
                                                  @RequestParam("org_id") Long orgId) throws Exception {
        return themeService.getOrgThemeClasses(orgId);
    }


    @ApiOperation(value = "Assign the organization to a certain theme class", nickname = "assignOrgThemeClass", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "themes/class")
    @ResponseStatus(OK)
    public void assignOrgThemeClass(@RequestHeader (value = "User-Token") String userToken,
                                   @RequestParam("org_id") Long orgId,
                                   @RequestParam(value = "class_id") List<Integer> classIds) throws BusinessException {
        themeService.assignOrgThemeClass(orgId, classIds);
    }


    @ApiOperation(value = "Remove the organization from a certain theme class", nickname = "removeOrgThemeClass", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @DeleteMapping(value = "themes/class")
    @ResponseStatus(OK)
    public void removeOrgThemeClass(@RequestHeader (value = "User-Token") String userToken,
                                    @RequestParam("org_id") Long orgId,
                                    @RequestParam(value = "class_id") Integer classId) throws BusinessException {
        themeService.removeOrgThemeClass(orgId, classId);
    }


    @ApiOperation(value = "Change an organization current theme", nickname = "changeOrgTheme", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "themes")
    @ResponseStatus(OK)
    public void changeOrgTheme(@RequestHeader (value = "User-Token") String userToken,
                                    @RequestBody OrganizationThemesSettingsDTO dto) throws BusinessException {
        themeService.changeOrgTheme(dto);
    }




    @ApiOperation(value = "Get list of payment gateways for the organization", nickname = "getGateways", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @GetMapping(value = "payments", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> getOrganizationPaymentGateways(@RequestParam(value = "org_id") Long orgId) {

        List<OrganizationPaymentGatewaysEntity> gateways = orgPaymentGatewaysRep.findAllByOrganizationId(orgId);
        if (gateways == null || gateways.size() == 0) {
            // no specific gateways defined for this org, use the default ones
            gateways = orgPaymentGatewaysRep.findAllByOrganizationIdIsNull();
        }
        StringBuilder list = new StringBuilder();
        list.append("{ ");
        for (OrganizationPaymentGatewaysEntity gateway: gateways) {
            if (list.length() > 2) {
                list.append(", ");
            }
            list.append('"');
            list.append(gateway.getGateway());
            list.append("\": { ");

            if ("mcard".equalsIgnoreCase(gateway.getGateway())) {
                list.append("\"script\": \"");
                MastercardAccount account = new MastercardAccount();
                account.init(Tools.getPropertyForAccount(gateway.getAccount(), classLogger, config.paymentPropertiesDir));
                list.append(account.getScriptUrl());
            } else if ("upg".equalsIgnoreCase(gateway.getGateway())) {
                list.append("\"script\": \"");
                UpgAccount account = new UpgAccount();
                account.init(Tools.getPropertyForAccount(gateway.getAccount(), classLogger, config.paymentPropertiesDir));
                list.append("https://upgstaging.egyptianbanks.com:3006/js/Lightbox.js");
            }
            list.append("\"}");
        }
        list.append(" }");

        return new ResponseEntity<>(list.toString(), HttpStatus.OK);
    }

}
