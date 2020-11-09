package com.nasnav.controller;

import static com.nasnav.payments.misc.Gateway.COD;
import static com.nasnav.payments.misc.Gateway.MASTERCARD;
import static com.nasnav.payments.misc.Gateway.UPG;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.OK;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.validation.Valid;

import com.nasnav.dto.response.PromotionResponse;
import com.nasnav.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.AppConfig;
import com.nasnav.dao.OrganizationPaymentGatewaysRepository;
import com.nasnav.dto.BrandDTO;
import com.nasnav.dto.ExtraAttributeDefinitionDTO;
import com.nasnav.dto.OrganizationDTO;
import com.nasnav.dto.OrganizationImageUpdateDTO;
import com.nasnav.dto.OrganizationThemesSettingsDTO;
import com.nasnav.dto.Organization_BrandRepresentationObject;
import com.nasnav.dto.ProductFeatureDTO;
import com.nasnav.dto.ProductFeatureUpdateDTO;
import com.nasnav.dto.PromotionSearchParamDTO;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.dto.TagsDTO;
import com.nasnav.dto.TagsTreeCreationDTO;
import com.nasnav.dto.ThemeClassDTO;
import com.nasnav.dto.request.organization.CartOptimizationSettingDTO;
import com.nasnav.dto.request.organization.SettingDTO;
import com.nasnav.dto.request.shipping.ShippingServiceRegistration;
import com.nasnav.dto.request.theme.OrganizationThemeClass;
import com.nasnav.dto.response.CartOptimizationStrategyDTO;
import com.nasnav.dto.response.OrgThemeRepObj;
import com.nasnav.dto.response.PromotionDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.payments.mastercard.MastercardAccount;
import com.nasnav.payments.misc.Tools;
import com.nasnav.payments.upg.UpgAccount;
import com.nasnav.persistence.OrganizationPaymentGatewaysEntity;
import com.nasnav.persistence.TagsEntity;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.response.ProductFeatureUpdateResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.response.TagResponse;
import com.nasnav.shipping.services.PickupFromShop;

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
    @Autowired
    private FileService fileService;
    
    @Autowired
    private ShippingManagementService shippingMngService;
    
    @Autowired
    private PromotionsService promotionsService;

    @Autowired
	private CartOptimizationService cartOptimizeService;
    
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
    public OrganizationResponse updateOrganizationData(@RequestHeader (name = "User-Token", required = false) String userToken,
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
    public List<Organization_BrandRepresentationObject> getOrganizationBrands(@RequestParam(value = "org_id") Long orgId) {
        return orgService.getOrganizationBrands(orgId);
    }






    @ApiOperation(value = "add or update Organization brand", nickname = "BrandModification", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "brand", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = {"multipart/form-data"})
    public OrganizationResponse updateBrandData(@RequestHeader (name = "User-Token", required = false) String userToken,
                                                 @RequestPart("properties") String jsonString,
                                                 @RequestPart(value = "logo", required = false) @Valid MultipartFile logo,
                                                 @RequestPart(value = "banner", required = false) @Valid MultipartFile banner,
                                                 @RequestPart(value = "cover", required = false) @Valid MultipartFile cover) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BrandDTO json = mapper.readValue(jsonString, BrandDTO.class);
        return orgService.validateAndUpdateBrand(json, logo, banner, cover);
    }


    @DeleteMapping(value = "brand")
    @ResponseStatus(OK)
    public void deleteBrand(@RequestHeader (name = "User-Token", required = false) String userToken,
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
    public List<ProductFeatureDTO> getOrganizationFeaturesData(@RequestParam("organization_id") Long orgId) {
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
    public ProductFeatureUpdateResponse updateProductFeature(@RequestHeader(name = "User-Token", required = false) String token,
    		                                                 @RequestBody ProductFeatureUpdateDTO featureDto) throws Exception {
        return orgService.updateProductFeature(featureDto);
    }


    @ApiOperation(value = "Delete organization extra attribute", nickname = "DeleteOrgExtraAttribute", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Provided token does not match any logged in user"),
    })
    @DeleteMapping(value = "extra_attribute")
    public void deleteExtraAttribute(@RequestHeader(name = "User-Token", required = false) String token,
                                     @RequestParam("attr_id") Integer attrId) {
        orgService.deleteExtraAttribute(attrId);
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
    public ProductImageUpdateResponse updateOrganizationImage(@RequestHeader(name = "User-Token", required = false) String token,
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
    public boolean deleteProductImage(@RequestHeader(name = "User-Token", required = false) String token,
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
    public TagResponse updateOrganizationTag(@RequestHeader (name = "User-Token", required = false) String userToken,
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
    public TagResponse deleteOrganizationTag(@RequestHeader (name = "User-Token", required = false) String userToken,
                                             @RequestParam (value = "tag_id")Long tagId) throws BusinessException {
        return categoryService.deleteOrgTag(tagId);
    }

    
    
    
    
    @ApiOperation(value = "create a new tag tree", nickname = "createTagTree", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "tag/tree", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(OK)
    public void createTagTree(@RequestHeader (name = "User-Token", required = false) String userToken,
                              @RequestBody TagsTreeCreationDTO tree) throws BusinessException {
        categoryService.createTagTree(tree);
    }


    @ApiOperation(value = "Assign category to list of tags", nickname = "assignTagsCategory", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "tag/category")
    @ResponseStatus(OK)
    public void assignTagsCategory(@RequestHeader (name = "User-Token", required = false) String userToken,
                                  @RequestParam("category_id") Long categoryId,
                                  @RequestParam(value = "tags", required = false) List<Long> tagsIds) throws BusinessException {
        categoryService.assignTagsCategory(categoryId, tagsIds);
    }


    @ApiOperation(value = "get themes assigned a certain organization", nickname = "GetOrgThemes", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @GetMapping(value = "themes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<OrgThemeRepObj> getOrgThemes(@RequestHeader (name = "User-Token", required = false) String userToken) {
        return themeService.getOrgThemes();
    }


    @ApiOperation(value = "get theme classes assigned a certain organization", nickname = "GetOrgThemeClasses", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @GetMapping(value = "themes/class", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<ThemeClassDTO> getOrgThemeClasses(@RequestHeader (name = "User-Token", required = false) String userToken,
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
    public void assignOrgThemeClass(@RequestHeader (name = "User-Token", required = false) String userToken,
                                    @RequestBody OrganizationThemeClass orgThemeClassDTO) throws BusinessException {
        themeService.assignOrgThemeClass(orgThemeClassDTO);
    }


    @ApiOperation(value = "Remove the organization from a certain theme class", nickname = "removeOrgThemeClass", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @DeleteMapping(value = "themes/class")
    @ResponseStatus(OK)
    public void removeOrgThemeClass(@RequestHeader (name = "User-Token", required = false) String userToken,
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
    public void changeOrgTheme(@RequestHeader (name = "User-Token", required = false) String userToken,
                                    @RequestBody OrganizationThemesSettingsDTO dto) throws BusinessException {
        themeService.changeOrgTheme(dto);
    }




    @ApiOperation(value = "Get list of payment gateways for the organization", nickname = "getGateways", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @GetMapping(value = "payments", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> getOrganizationPaymentGateways(@RequestParam(value = "org_id") Long orgId,
                                                            @RequestParam(value = "delivery", required = false) String deliveryService) {

        List<OrganizationPaymentGatewaysEntity> gateways = orgPaymentGatewaysRep.findAllByOrganizationId(orgId);
        if (gateways == null || gateways.size() == 0) {
            // no specific gateways defined for this org, use the default ones
            gateways = orgPaymentGatewaysRep.findAllByOrganizationIdIsNull();
        }
        StringBuilder list = new StringBuilder();
        list.append("{ ");
        for (OrganizationPaymentGatewaysEntity gateway: gateways) {
            if (deliveryService != null) {
                // For now - hardcoded rule for not allowing CoD for Pickup service (to prevent misuse)
                if (COD.getValue().equalsIgnoreCase(gateway.getGateway()) && !PaymentControllerCoD.isCodAvailableForService(deliveryService)) {
                    continue;
                }
            }
            if (list.length() > 2) {
                list.append(", ");
            }
            list.append('"');
            list.append(gateway.getGateway());
            list.append("\": { ");

            if (MASTERCARD.getValue().equalsIgnoreCase(gateway.getGateway())) {
                list.append("\"script\": \"");
                MastercardAccount account = new MastercardAccount();
                account.init(Tools.getPropertyForAccount(gateway.getAccount(), classLogger, config.paymentPropertiesDir), gateway.getId());
                list.append(account.getScriptUrl());
                list.append('"');
            } else if (UPG.getValue().equalsIgnoreCase(gateway.getGateway())) {
                list.append("\"script\": \"");
                UpgAccount account = new UpgAccount();
                account.init(Tools.getPropertyForAccount(gateway.getAccount(), classLogger, config.paymentPropertiesDir));
                list.append(account.getUpgScriptUrl());
                list.append('"');
            }
            list.append("}");
        }
        list.append(" }");

        return new ResponseEntity<>(list.toString(), HttpStatus.OK);
    }
    
    
    
    
    
    
    
    
    @ApiOperation(value = "register the organization to a shipping service", nickname = "registerShippingService", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "shipping/service")
    @ResponseStatus(OK)
    public void registerToShippingService(@RequestHeader (name = "User-Token", required = false) String userToken,
                                    @RequestBody ShippingServiceRegistration registration) throws BusinessException {
    	shippingMngService.registerToShippingService(registration);
    }
    
    
    
    
    
    @ApiOperation(value = "list shipping services that the organization is registered to", nickname = "getOrgShippingService", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @GetMapping(value = "shipping/service")
    @ResponseStatus(OK)
    public List<ShippingServiceRegistration> listShippingServices(
    		@RequestHeader (name = "User-Token", required = false) String userToken) throws BusinessException {
    	return shippingMngService.listShippingServices();
    }
    
    
    
    
    
    @ApiOperation(value = "get organization extra attributes", nickname = "GetOrgExtraAttr", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @GetMapping(value = "extra_attribute", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<ExtraAttributeDefinitionDTO> getOrgExtraAttibute(@RequestHeader (name = "User-Token", required = false) String userToken) throws Exception {
        return orgService.getExtraAttributes();
    }
    
    
    
    
    @ApiOperation(value = "get organization promotions", nickname = "GetPromotions", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @GetMapping(value = "promotions", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PromotionResponse getPromotions(
    		@RequestHeader (name = "User-Token", required = false) String userToken
    		,@RequestParam(name="status", required = false)String status
    		,@RequestParam(name="start_date", required = false)String startTime
    		,@RequestParam(name="end_date", required = false)String endTime
    		,@RequestParam(name="id", required = false)Long id
            ,@RequestParam(name="start", required = false)Integer start
            ,@RequestParam(name="count", required = false)Integer count){
    	PromotionSearchParamDTO searchParams = new PromotionSearchParamDTO(status, startTime, endTime, id, start, count);
    	return promotionsService.getPromotions(searchParams);
    }
    
    
    
    
    
    
    @ApiOperation(value = "add new promotions", nickname = "addPromotion", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "promotion")
    @ResponseStatus(OK)
    public Long addPromotion(@RequestHeader (name = "User-Token", required = false) String userToken,
                             @RequestBody PromotionDTO promotion) {
    	return promotionsService.updatePromotion(promotion);
    }



    @ApiOperation(value = "remove/deactivate promotions", nickname = "deletePromotion", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @DeleteMapping(value = "promotion")
    @ResponseStatus(OK)
    public void removePromotion(@RequestHeader (name = "User-Token", required = false) String userToken,
                             @RequestParam("id") Long promotionId) {
        promotionsService.removePromotion(promotionId);
    }



    @ApiOperation(value = "cancelling the registration of an organization into a certain shipping service", nickname = "unregisterShippingService", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @DeleteMapping(value = "shipping/service")
    @ResponseStatus(OK)
    public void unregisterFromShippingService(@RequestHeader (name = "User-Token", required = false) String userToken,
                                              @RequestParam("service_id") String serviceId){
        shippingMngService.unregisterFromShippingService(serviceId);
    }
    
    
    
    
    
    @ApiOperation(value = "delete an organization setting", nickname = "deleteSetting", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @DeleteMapping(value = "settings")
    @ResponseStatus(OK)
    public void deleteSetting(@RequestHeader (name = "User-Token", required = false) String userToken,
                                              @RequestParam("name") String settingName){
        orgService.deleteSetting(settingName);
    }
    
    
    
    
    @ApiOperation(value = "add/udpate an organization setting", nickname = "udpateSetting", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "settings")
    @ResponseStatus(OK)
    public void updateSetting(@RequestHeader (name = "User-Token", required = false) String userToken,
    								@RequestBody SettingDTO setting){
        orgService.updateSetting(setting);
    }
    
    
    
    
    @ApiOperation(value = "set cart optimization strategy for the organization or the shipping service", nickname = "setCartOptimization", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "settings/cart_optimization/strategy")
    @ResponseStatus(OK)
    public void setCartOptmizationStrategy(@RequestHeader (name = "User-Token", required = false) String userToken,
    								@RequestBody CartOptimizationSettingDTO setting){
    	cartOptimizeService.setCartOptimizationStrategy(setting);
    }
    
    
    
    
    @ApiOperation(value = "get cart optimization strategies for the organization or the shipping service", nickname = "setCartOptimization", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @GetMapping(value = "settings/cart_optimization/strategy")
    @ResponseStatus(OK)
    public List<CartOptimizationSettingDTO> getCartOptmizationStrategy(@RequestHeader (name = "User-Token", required = false) String userToken){
    	return cartOptimizeService.getCartOptimizationStrategy();
    }
    
    
    
    
    @ApiOperation(value = "get all cart optimization strategies", nickname = "getAllCartOptimization", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @GetMapping(value = "settings/cart_optimization/strategies")
    @ResponseStatus(OK)
    public List<CartOptimizationStrategyDTO> listCartOptmizationStrategies(@RequestHeader (name = "User-Token", required = false) String userToken){
    	return cartOptimizeService.listAllCartOptimizationStrategies();
    }
    
    
    
    @ApiOperation(value = "get all organization shops including warehouses", nickname = "getAllShops", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @GetMapping(value = "shops")
    @ResponseStatus(OK)
    public List<ShopRepresentationObject> getAllShops(@RequestHeader (name = "User-Token", required = false) String userToken){
    	return orgService.getOrganizationShops();
    }


    @ApiOperation(value = "Get Organization subscribed users", nickname = "getSubscribedUsers", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @GetMapping(value = "subscribed_users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<String> getSubscribedUsers(@RequestHeader (name = "User-Token", required = false) String userToken){
        return orgService.getSubscribedUsers();
    }

    @ApiOperation(value = "Remove Organization subscribed user", nickname = "removeSubscribedUser", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @DeleteMapping(value = "subscribed_users")
    public void removeSubscribedUser(@RequestHeader (name = "User-Token", required = false) String userToken,
                                     @RequestParam String email){
         orgService.removeSubscribedUser(email);
    }


    @ApiOperation(value = "Get organization images info", nickname = "getOrgImagesInfo", code = 201)
    @ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
    @GetMapping(value = "images_info")
    @ResponseBody
    public ResponseEntity<String> getImagesInfo(@RequestHeader (name = "User-Token", required = false) String userToken,
                                                @RequestParam (value = "org_id", required = false) Long orgId) {
        ByteArrayOutputStream s =  fileService.getImagesInfo(orgId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(CONTENT_DISPOSITION, "attachment; filename=images-info.csv")
                .body(s.toString());
    }
}
