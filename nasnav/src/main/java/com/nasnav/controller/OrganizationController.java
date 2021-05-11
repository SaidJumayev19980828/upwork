package com.nasnav.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dto.*;
import com.nasnav.dto.request.organization.CartOptimizationSettingDTO;
import com.nasnav.dto.request.organization.SettingDTO;
import com.nasnav.dto.request.organization.SubAreasUpdateDTO;
import com.nasnav.dto.request.shipping.ShippingServiceRegistration;
import com.nasnav.dto.request.theme.OrganizationThemeClass;
import com.nasnav.dto.response.CartOptimizationStrategyDTO;
import com.nasnav.dto.response.OrgThemeRepObj;
import com.nasnav.dto.response.PromotionDTO;
import com.nasnav.dto.response.PromotionResponse;
import com.nasnav.enumerations.ProductFeatureType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.TagsEntity;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.response.ProductFeatureUpdateResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.response.TagResponse;
import com.nasnav.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/organization")
@Tag(name = "Set of endpoints for adding, updating and deleting Dashboard data.")
@CrossOrigin("*") // allow all origins
public class OrganizationController {
    @Autowired
    private OrganizationService orgService;
    @Autowired
    private CategoryService categoryService;
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
    @Autowired
    private SearchService searchService;
    @Autowired
    private SeoService seoService;
    @Autowired
    private AddressService addressService;


    public OrganizationController(OrganizationService orgService) {
        this.orgService = orgService;
    }

    @Operation(description =  "add or update Organization data", summary = "OrganizationModification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @PostMapping(value = "info", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = {"multipart/form-data"})
    public OrganizationResponse updateOrganizationData(@RequestHeader (name = "User-Token", required = false) String userToken,
                                                       @RequestPart("properties") String jsonString,
                                                       @RequestPart(value = "logo", required = false) @Valid MultipartFile file) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        OrganizationDTO.OrganizationModificationDTO json = mapper.readValue(jsonString, OrganizationDTO.OrganizationModificationDTO.class);
        return orgService.updateOrganizationData(json, file);
    }






    @Operation(description =  "Get Organization brands data", summary = "getBrands")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @GetMapping(value = "brands", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Organization_BrandRepresentationObject> getOrganizationBrands(@RequestParam(value = "org_id") Long orgId) {
        return orgService.getOrganizationBrands(orgId);
    }






    @Operation(description =  "add or update Organization brand", summary = "BrandModification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @PostMapping(value = "brand", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = {"multipart/form-data"})
    public OrganizationResponse updateBrandData(@RequestHeader (name = "User-Token", required = false) String userToken,
                                                @RequestPart("properties") String jsonString,
                                                @RequestPart(value = "logo", required = false) @Valid MultipartFile logo,
                                                @RequestPart(value = "banner", required = false) @Valid MultipartFile banner,
                                                @RequestPart(value = "cover", required = false) @Valid MultipartFile cover) throws IOException {
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



    @Operation(description =  "get product features for organization", summary = "GetOrgProductFeatures")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @GetMapping(value = "products_features", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<ProductFeatureDTO> getOrganizationFeaturesData(@RequestParam("organization_id") Long orgId) {
        return orgService.getProductFeatures(orgId);
    }




    @Operation(description =  "get product features for organization", summary = "GetOrgProductFeatures")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @GetMapping(value = "products_features/types", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<ProductFeatureType> getOrganizationFeaturesTypes(@RequestHeader(name = "User-Token", required = false) String token) {
        return orgService.getProductFeatureTypes();
    }






    @Operation(description =  "add/update product features for organization", summary = "PostOrgProductFeatures")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @PostMapping(value = "products_feature"
            , produces = MediaType.APPLICATION_JSON_UTF8_VALUE
            , consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ProductFeatureUpdateResponse updateProductFeature(@RequestHeader(name = "User-Token", required = false) String token,
                                                             @RequestBody ProductFeatureUpdateDTO featureDto) throws Exception {
        return orgService.updateProductFeature(featureDto);
    }



    @Operation(description =  "removed product features for organization, making it unavailable for usage", summary = "PostOrgProductFeatures")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @DeleteMapping(value = "products_feature")
    public void removeProductFeature(@RequestHeader(name = "User-Token", required = false) String token,
                                                             @RequestParam("id") Integer featureId) throws Exception {
        orgService.removeProductFeature(featureId);
    }


    @Operation(description =  "Delete organization extra attribute", summary = "DeleteOrgExtraAttribute")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 401" ,description = "Provided token does not match any logged in user"),
    })
    @DeleteMapping(value = "extra_attribute")
    public void deleteExtraAttribute(@RequestHeader(name = "User-Token", required = false) String token,
                                     @RequestParam("attr_id") Integer attrId) {
        orgService.deleteExtraAttribute(attrId);
    }


    @Operation(description =  "add/update organization images", summary = "PostOrgImg")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
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


    @Operation(description =  "delete image for organization", summary = "organization image delete")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Organization image deleted"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insufficient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = "image", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public boolean deleteProductImage(@RequestHeader(name = "User-Token", required = false) String token,
                                      @RequestParam("image_id") @Valid Long imageId) throws BusinessException {
        return  orgService.deleteImage(imageId);
    }




    @Operation(description =  "Create or update Organization tag", summary = "orgTagModification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @PostMapping(value = "tag", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public TagResponse updateOrganizationTag(@RequestHeader (name = "User-Token", required = false) String userToken,
                                             @RequestBody TagsDTO tagDTO) throws BusinessException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        tagDTO.setHasCategory(true);
        TagsEntity tag = categoryService.createOrUpdateTag(tagDTO);
        return new TagResponse(tag.getId());
    }


    @Operation(description =  "Delete Organization tag", summary = "orgTagDeletion")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @DeleteMapping(value = "tag", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public TagResponse deleteOrganizationTag(@RequestHeader (name = "User-Token", required = false) String userToken,
                                             @RequestParam (value = "tag_id")Long tagId) throws BusinessException {
        return categoryService.deleteOrgTag(tagId);
    }





    @Operation(description =  "create a new tag tree", summary = "createTagTree")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @PostMapping(value = "tag/tree", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(OK)
    public void createTagTree(@RequestHeader (name = "User-Token", required = false) String userToken,
                              @RequestBody TagsTreeCreationDTO tree) throws BusinessException {
        categoryService.createTagTree(tree);
    }


    @Operation(description =  "Assign category to list of tags", summary = "assignTagsCategory")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @PostMapping(value = "tag/category")
    @ResponseStatus(OK)
    public void assignTagsCategory(@RequestHeader (name = "User-Token", required = false) String userToken,
                                   @RequestParam("category_id") Long categoryId,
                                   @RequestParam(value = "tags", required = false) List<Long> tagsIds) throws BusinessException {
        categoryService.assignTagsCategory(categoryId, tagsIds);
    }


    @Operation(description =  "get themes assigned a certain organization", summary = "GetOrgThemes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @GetMapping(value = "themes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<OrgThemeRepObj> getOrgThemes(@RequestHeader (name = "User-Token", required = false) String userToken) {
        return themeService.getOrgThemes();
    }


    @Operation(description =  "get theme classes assigned a certain organization", summary = "GetOrgThemeClasses")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @GetMapping(value = "themes/class", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<ThemeClassDTO> getOrgThemeClasses(@RequestHeader (name = "User-Token", required = false) String userToken,
                                                  @RequestParam("org_id") Long orgId) throws Exception {
        return themeService.getOrgThemeClasses(orgId);
    }


    @Operation(description =  "Assign the organization to a certain theme class", summary = "assignOrgThemeClass")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @PostMapping(value = "themes/class")
    @ResponseStatus(OK)
    public void assignOrgThemeClass(@RequestHeader (name = "User-Token", required = false) String userToken,
                                    @RequestBody OrganizationThemeClass orgThemeClassDTO) throws BusinessException {
        themeService.assignOrgThemeClass(orgThemeClassDTO);
    }


    @Operation(description =  "Change an organization current theme", summary = "changeOrgTheme")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @PostMapping(value = "themes")
    @ResponseStatus(OK)
    public void changeOrgTheme(@RequestHeader (name = "User-Token", required = false) String userToken,
                               @RequestBody OrganizationThemesSettingsDTO dto) throws BusinessException {
        themeService.changeOrgTheme(dto);
    }




    @Operation(description =  "Get list of payment gateways for the organization", summary = "getGateways")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @GetMapping(value = "payments", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public LinkedHashMap getOrganizationPaymentGateways(@RequestParam(value = "org_id") Long orgId,
                                                        @RequestParam(value = "delivery", required = false) String deliveryService) {
        return orgService.getOrganizationPaymentGateways(orgId, deliveryService);
    }








    @Operation(description =  "register the organization to a shipping service", summary = "registerShippingService")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @PostMapping(value = "shipping/service")
    @ResponseStatus(OK)
    public void registerToShippingService(@RequestHeader (name = "User-Token", required = false) String userToken,
                                          @RequestBody ShippingServiceRegistration registration) {
        shippingMngService.registerToShippingService(registration);
    }





    @Operation(description =  "list shipping services that the organization is registered to", summary = "getOrgShippingService")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @GetMapping(value = "shipping/service")
    @ResponseStatus(OK)
    public List<ShippingServiceRegistration> listShippingServices(
            @RequestHeader (name = "User-Token", required = false) String userToken) {
        return shippingMngService.listShippingServices();
    }





    @Operation(description =  "get organization extra attributes", summary = "GetOrgExtraAttr")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @GetMapping(value = "extra_attribute", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<ExtraAttributeDefinitionDTO> getOrgExtraAttibute(@RequestHeader (name = "User-Token", required = false) String userToken) {
        return orgService.getExtraAttributes();
    }




    @Operation(description =  "get organization promotions", summary = "GetPromotions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
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






    @Operation(description =  "add new promotions", summary = "addPromotion")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @PostMapping(value = "promotion")
    @ResponseStatus(OK)
    public Long addPromotion(@RequestHeader (name = "User-Token", required = false) String userToken,
                             @RequestBody PromotionDTO promotion) {
        return promotionsService.updatePromotion(promotion);
    }



    @Operation(description =  "remove/deactivate promotions", summary = "deletePromotion")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @DeleteMapping(value = "promotion")
    @ResponseStatus(OK)
    public void removePromotion(@RequestHeader (name = "User-Token", required = false) String userToken,
                                @RequestParam("id") Long promotionId) {
        promotionsService.removePromotion(promotionId);
    }



    @Operation(description =  "cancelling the registration of an organization into a certain shipping service", summary = "unregisterShippingService")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @DeleteMapping(value = "shipping/service")
    @ResponseStatus(OK)
    public void unregisterFromShippingService(@RequestHeader (name = "User-Token", required = false) String userToken,
                                              @RequestParam("service_id") String serviceId){
        shippingMngService.unregisterFromShippingService(serviceId);
    }





    @Operation(description =  "delete an organization setting", summary = "deleteSetting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @DeleteMapping(value = "settings")
    @ResponseStatus(OK)
    public void deleteSetting(@RequestHeader (name = "User-Token", required = false) String userToken,
                              @RequestParam("name") String settingName){
        orgService.deleteSetting(settingName);
    }




    @Operation(description =  "add/udpate an organization setting", summary = "udpateSetting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @PostMapping(value = "settings")
    @ResponseStatus(OK)
    public void updateSetting(@RequestHeader (name = "User-Token", required = false) String userToken,
                              @RequestBody SettingDTO setting){
        orgService.updateSetting(setting);
    }




    @Operation(description =  "set cart optimization strategy for the organization or the shipping service", summary = "setCartOptimization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @PostMapping(value = "settings/cart_optimization/strategy")
    @ResponseStatus(OK)
    public void setCartOptimizationStrategy(@RequestHeader (name = "User-Token", required = false) String userToken,
                                           @RequestBody CartOptimizationSettingDTO setting){
        cartOptimizeService.setCartOptimizationStrategy(setting);
    }



    @Operation(description =  "get cart optimization strategies for the organization or the shipping service", summary = "setCartOptimization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @GetMapping(value = "settings/cart_optimization/strategy")
    @ResponseStatus(OK)
    public List<CartOptimizationSettingDTO> getCartOptimizationStrategy(@RequestHeader (name = "User-Token", required = false) String userToken){
        return cartOptimizeService.getCartOptimizationStrategy();
    }


    @Operation(description =  "delete cart optimization strategy for the organization or the shipping service", summary = "setCartOptimization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @DeleteMapping(value = "settings/cart_optimization/strategy")
    @ResponseStatus(OK)
    public void deleteCartOptimizationStrategy(@RequestHeader (name = "User-Token", required = false) String userToken,
                                           @RequestParam("strategy_name")String strategyName
                                            ,@RequestParam(value = "shipping_service", required = false)String shippingService){
        cartOptimizeService.deleteCartOptimizationStrategy(strategyName, shippingService);
    }




    @Operation(description =  "get all cart optimization strategies", summary = "getAllCartOptimization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @GetMapping(value = "settings/cart_optimization/strategies")
    @ResponseStatus(OK)
    public List<CartOptimizationStrategyDTO> listCartOptmizationStrategies(@RequestHeader (name = "User-Token", required = false) String userToken){
        return cartOptimizeService.listAllCartOptimizationStrategies();
    }



    @Operation(description =  "get all organization shops including warehouses", summary = "getAllShops")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @GetMapping(value = "shops")
    @ResponseStatus(OK)
    public List<ShopRepresentationObject> getAllShops(@RequestHeader (name = "User-Token", required = false) String userToken){
        return orgService.getOrganizationShops();
    }





    @Operation(description =  "Get Organization subscribed users", summary = "getSubscribedUsers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @GetMapping(value = "subscribed_users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<String> getSubscribedUsers(@RequestHeader (name = "User-Token", required = false) String userToken){
        return orgService.getSubscribedUsers();
    }




    @Operation(description =  "Remove Organization subscribed user", summary = "removeSubscribedUser")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @DeleteMapping(value = "subscribed_users")
    public void removeSubscribedUser(@RequestHeader (name = "User-Token", required = false) String userToken,
                                     @RequestParam String email){
        orgService.removeSubscribedUser(email);
    }


    @Operation(description =  "Get organization images info", summary = "getOrgImagesInfo")
    @ApiResponses(value = {@ApiResponse(responseCode = " 200" ,description = "OK")})
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



    @Operation(description =  "synchronize data with search server", summary = "syncSearchData")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @PostMapping(value = "search/data/sync")
    @ResponseStatus(OK)
    public Mono<Void> syncSearchData(@RequestHeader (name = "User-Token", required = false) String userToken){
        return searchService.syncSearchData();
    }



    @Operation(description =  "update seo keywords for an entity", summary = "addSeoKeywords")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @PostMapping(value = "seo")
    @ResponseStatus(OK)
    public void addSeoKeywords(@RequestHeader (name = "User-Token", required = false) String userToken
        , @RequestBody SeoKeywordsDTO seoKeywords){
        seoService.addSeoKeywords(seoKeywords);
    }




    @Operation(description =  "update organization sub-areas", summary = "addSeoKeywords")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @PostMapping(value = "sub_areas")
    @ResponseStatus(OK)
    public void updateSubAreas(@RequestHeader (name = "User-Token", required = false) String userToken
            , @RequestBody SubAreasUpdateDTO subAreas){
        addressService.updateSubAreas(subAreas);
    }


    @Operation(description =  "delete organization sub-areas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @DeleteMapping(value = "sub_areas")
    @ResponseStatus(OK)
    public void deleteSubAreas(@RequestHeader (name = "User-Token", required = false) String userToken, @RequestParam("sub_areas") Set<Long> subAreas){
        addressService.deleteSubAreas(subAreas);
    }


    @Operation(description =  "get organization sub-areas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "process completed successfully"),
            @ApiResponse(responseCode = " 403" ,description = "User not authorized to do this action"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid or missing parameter"),
    })
    @GetMapping(value = "sub_areas")
    public List<SubAreasRepObj> getOrgSubAreas(@RequestHeader (name = "User-Token", required = false) String userToken,
                                               @RequestParam(value = "area_id", required = false) Long areaId,
                                               @RequestParam(value = "city_id", required = false) Long cityId,
                                               @RequestParam(value = "country_id", required = false) Long countryId){
        return addressService.getOrgSubAreas(areaId, cityId, countryId);
    }
}
