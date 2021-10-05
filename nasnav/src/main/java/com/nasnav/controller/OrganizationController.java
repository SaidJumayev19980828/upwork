package com.nasnav.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dto.*;
import com.nasnav.dto.request.organization.CartOptimizationSettingDTO;
import com.nasnav.dto.request.organization.OrganizationModificationDTO;
import com.nasnav.dto.request.organization.SettingDTO;
import com.nasnav.dto.request.organization.SubAreasUpdateDTO;
import com.nasnav.dto.request.shipping.ShippingServiceRegistration;
import com.nasnav.dto.request.theme.OrganizationThemeClass;
import com.nasnav.dto.response.*;
import com.nasnav.enumerations.ProductFeatureType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.TagsEntity;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.response.ProductFeatureUpdateResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.response.TagResponse;
import com.nasnav.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nasnav.exceptions.ErrorCodes.ORG$IMG$0003;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("/organization")
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

    @PostMapping(value = "info", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
    public OrganizationResponse updateOrganizationData(@RequestHeader (name = "User-Token", required = false) String userToken,
                                                       @RequestPart("properties") String jsonString,
                                                       @RequestPart(value = "logo", required = false) @Valid MultipartFile file) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        OrganizationModificationDTO json = mapper.readValue(jsonString, OrganizationModificationDTO.class);
        return orgService.updateOrganizationData(json, file);
    }

    @GetMapping(value = "brands", produces = APPLICATION_JSON_VALUE)
    public List<Organization_BrandRepresentationObject> getOrganizationBrands(@RequestParam(value = "org_id") Long orgId) {
        return brandService.getOrganizationBrands(orgId);
    }

    @PostMapping(value = "brand", produces = APPLICATION_JSON_VALUE, consumes = {"multipart/form-data"})
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
    public void deleteBrand(@RequestHeader (name = "User-Token", required = false) String userToken,
                            @RequestParam("brand_id") Long brandId) throws BusinessException {
        brandService.deleteBrand(brandId);
    }

    @GetMapping(value = "products_features", produces = APPLICATION_JSON_VALUE)
    public List<ProductFeatureDTO> getOrganizationFeaturesData(@RequestParam("organization_id") Long orgId) {
        return orgService.getProductFeatures(orgId);
    }

    @GetMapping(value = "products_features/types", produces = APPLICATION_JSON_VALUE)
    public List<ProductFeatureType> getOrganizationFeaturesTypes(@RequestHeader(name = "User-Token", required = false) String token) {
        return orgService.getProductFeatureTypes();
    }

    @PostMapping(value = "products_feature", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ProductFeatureUpdateResponse updateProductFeature(@RequestHeader(name = "User-Token", required = false) String token,
                                                             @RequestBody ProductFeatureUpdateDTO featureDto) {
        return orgService.updateProductFeature(featureDto);
    }

    @DeleteMapping(value = "products_feature")
    public void removeProductFeature(@RequestHeader(name = "User-Token", required = false) String token,
                                     @RequestParam("id") Integer featureId) {
        orgService.removeProductFeature(featureId);
    }

    @DeleteMapping(value = "extra_attribute")
    public void deleteExtraAttribute(@RequestHeader(name = "User-Token", required = false) String token,
                                     @RequestParam("attr_id") Integer attrId) {
        orgService.deleteExtraAttribute(attrId);
    }

    @PostMapping(value = "image", produces = APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductImageUpdateResponse updateOrganizationImage(@RequestHeader(name = "User-Token", required = false) String token,
                                                              @RequestPart(value = "image", required = false) @Valid MultipartFile file,
                                                              @RequestPart("properties") @Valid String jsonString) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        OrganizationImageUpdateDTO imgMetaData = mapper.readValue(jsonString, OrganizationImageUpdateDTO.class);
        return orgService.updateOrganizationImage(file, imgMetaData);
    }

    @DeleteMapping(value = "image", produces = APPLICATION_JSON_VALUE)
    public void deleteProductImage(@RequestHeader(name = "User-Token", required = false) String token,
                                      @RequestParam(value = "image_id", required = false) Long imageId,
                                      @RequestParam(value = "url", required = false) String url) {
        if (imageId == null && url == null) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$IMG$0003);
        }
        orgService.deleteImage(imageId, url);
    }

    @PostMapping(value = "tag", produces = APPLICATION_JSON_VALUE)
    public TagResponse updateOrganizationTag(@RequestHeader (name = "User-Token", required = false) String userToken,
                                             @RequestBody TagsDTO tagDTO) throws BusinessException {
        tagDTO.setHasCategory(true);
        TagsEntity tag = categoryService.createOrUpdateTag(tagDTO);
        return new TagResponse(tag.getId());
    }

    @DeleteMapping(value = "tag", produces = APPLICATION_JSON_VALUE)
    public TagResponse deleteOrganizationTag(@RequestHeader (name = "User-Token", required = false) String userToken,
                                             @RequestParam (value = "tag_id")Long tagId) throws BusinessException {
        return categoryService.deleteOrgTag(tagId);
    }

    @PostMapping(value = "tag/tree", produces = APPLICATION_JSON_VALUE)
    public void createTagTree(@RequestHeader (name = "User-Token", required = false) String userToken,
                              @RequestBody TagsTreeCreationDTO tree) throws BusinessException {
        categoryService.createTagTree(tree);
    }

    @PostMapping(value = "tag/category")
    public void assignTagsCategory(@RequestHeader (name = "User-Token", required = false) String userToken,
                                   @RequestParam("category_id") Long categoryId,
                                   @RequestParam(value = "tags", required = false) List<Long> tagsIds) throws BusinessException {
        categoryService.assignTagsCategory(categoryId, tagsIds);
    }

    @GetMapping(value = "themes", produces = APPLICATION_JSON_VALUE)
    public List<OrgThemeRepObj> getOrgThemes(@RequestHeader (name = "User-Token", required = false) String userToken) {
        return themeService.getOrgThemes();
    }

    @GetMapping(value = "themes/class", produces = APPLICATION_JSON_VALUE)
    public List<ThemeClassDTO> getOrgThemeClasses(@RequestHeader (name = "User-Token", required = false) String userToken,
                                                  @RequestParam("org_id") Long orgId) throws Exception {
        return themeService.getOrgThemeClasses(orgId);
    }

    @PostMapping(value = "themes/class")
    public void assignOrgThemeClass(@RequestHeader (name = "User-Token", required = false) String userToken,
                                    @RequestBody OrganizationThemeClass orgThemeClassDTO) throws BusinessException {
        themeService.assignOrgThemeClass(orgThemeClassDTO);
    }

    @PostMapping(value = "themes")
    public void changeOrgTheme(@RequestHeader (name = "User-Token", required = false) String userToken,
                               @RequestBody OrganizationThemesSettingsDTO dto) throws BusinessException {
        themeService.changeOrgTheme(dto);
    }

    @GetMapping(value = "payments", produces = APPLICATION_JSON_VALUE)
    public LinkedHashMap<String, Map<String, String>> getOrganizationPaymentGateways(@RequestParam(value = "org_id") Long orgId,
                                                                     @RequestParam(value = "delivery", required = false) String deliveryService) {
        return orgService.getOrganizationPaymentGateways(orgId, deliveryService);
    }

    @PostMapping(value = "shipping/service", consumes = APPLICATION_JSON_VALUE)
    public void registerToShippingService(@RequestHeader (name = "User-Token", required = false) String userToken,
                                          @RequestBody ShippingServiceRegistration registration) {
        shippingMngService.registerToShippingService(registration);
    }

    @GetMapping(value = "shipping/service", produces = APPLICATION_JSON_VALUE)
    public List<ShippingServiceRegistration> listShippingServices(@RequestHeader (name = "User-Token", required = false) String userToken) {
        return shippingMngService.listShippingServices();
    }

    @GetMapping(value = "shipping/airway_bill", produces = APPLICATION_JSON_VALUE)
    public OrderConfirmResponseDTO getShippingAirwayBill(@RequestHeader (name = "User-Token", required = false) String userToken,
                                                         @RequestParam("order_id") Long orderId) {
        return shippingMngService.getShippingAirwayBill(orderId);
    }

    @GetMapping(value = "extra_attribute", produces = APPLICATION_JSON_VALUE)
    public List<ExtraAttributeDefinitionDTO> getOrgExtraAttribute(@RequestHeader (name = "User-Token", required = false) String userToken) {
        return orgService.getExtraAttributes();
    }

    @GetMapping(value = "promotions", produces = APPLICATION_JSON_VALUE)
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

    @PostMapping(value = "promotion", consumes = APPLICATION_JSON_VALUE)
    public Long addPromotion(@RequestHeader (name = "User-Token", required = false) String userToken,
                             @RequestBody PromotionDTO promotion) {
        return promotionsService.updatePromotion(promotion);
    }

    @DeleteMapping(value = "promotion")
    public void removePromotion(@RequestHeader (name = "User-Token", required = false) String userToken,
                                @RequestParam("id") Long promotionId) {
        promotionsService.removePromotion(promotionId);
    }

    @DeleteMapping(value = "shipping/service")
    public void unregisterFromShippingService(@RequestHeader (name = "User-Token", required = false) String userToken,
                                              @RequestParam("service_id") String serviceId){
        shippingMngService.unregisterFromShippingService(serviceId);
    }

    @DeleteMapping(value = "settings")
    public void deleteSetting(@RequestHeader (name = "User-Token", required = false) String userToken,
                              @RequestParam("name") String settingName){
        orgService.deleteSetting(settingName);
    }

    @PostMapping(value = "settings")
    public void updateSetting(@RequestHeader (name = "User-Token", required = false) String userToken,
                              @RequestBody SettingDTO setting){
        orgService.updateSetting(setting);
    }

    @PostMapping(value = "settings/cart_optimization/strategy", consumes = APPLICATION_JSON_VALUE)
    public void setCartOptimizationStrategy(@RequestHeader (name = "User-Token", required = false) String userToken,
                                           @RequestBody CartOptimizationSettingDTO setting){
        cartOptimizeService.setCartOptimizationStrategy(setting);
    }

    @GetMapping(value = "settings/cart_optimization/strategy", produces = APPLICATION_JSON_VALUE)
    public List<CartOptimizationSettingDTO> getCartOptimizationStrategy(@RequestHeader (name = "User-Token", required = false) String userToken){
        return cartOptimizeService.getCartOptimizationStrategy();
    }

    @DeleteMapping(value = "settings/cart_optimization/strategy")
    public void deleteCartOptimizationStrategy(@RequestHeader (name = "User-Token", required = false) String userToken,
                                               @RequestParam("strategy_name")String strategyName,
                                               @RequestParam(value = "shipping_service", required = false)String shippingService){
        cartOptimizeService.deleteCartOptimizationStrategy(strategyName, shippingService);
    }

    @GetMapping(value = "settings/cart_optimization/strategies", produces = APPLICATION_JSON_VALUE)
    public List<CartOptimizationStrategyDTO> listCartOptimizationStrategies(@RequestHeader (name = "User-Token", required = false) String userToken){
        return cartOptimizeService.listAllCartOptimizationStrategies();
    }

    @GetMapping(value = "shops")
    public List<ShopRepresentationObject> getAllShops(@RequestHeader (name = "User-Token", required = false) String userToken){
        return orgService.getOrganizationShops();
    }

    @GetMapping(value = "subscribed_users", produces = APPLICATION_JSON_VALUE)
    public List<String> getSubscribedUsers(@RequestHeader (name = "User-Token", required = false) String userToken){
        return orgService.getSubscribedUsers();
    }

    @DeleteMapping(value = "subscribed_users")
    public void removeSubscribedUser(@RequestHeader (name = "User-Token", required = false) String userToken,
                                     @RequestParam String email){
        orgService.removeSubscribedUser(email);
    }

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

    @PostMapping(value = "search/data/sync")
    @ResponseStatus(OK)
    public Mono<Void> syncSearchData(@RequestHeader (name = "User-Token", required = false) String userToken){
        return searchService.syncSearchData();
    }

    @PostMapping(value = "seo")
    public void addSeoKeywords(@RequestHeader (name = "User-Token", required = false) String userToken,
                               @RequestBody SeoKeywordsDTO seoKeywords){
        seoService.addSeoKeywords(seoKeywords);
    }

    @PostMapping(value = "sub_areas")
    public void updateSubAreas(@RequestHeader (name = "User-Token", required = false) String userToken,
                               @RequestBody SubAreasUpdateDTO subAreas){
        addressService.updateSubAreas(subAreas);
    }

    @DeleteMapping(value = "sub_areas")
    public void deleteSubAreas(@RequestHeader (name = "User-Token", required = false) String userToken,
                               @RequestParam("sub_areas") Set<Long> subAreas){
        addressService.deleteSubAreas(subAreas);
    }

    @GetMapping(value = "sub_areas")
    public List<SubAreasRepObj> getOrgSubAreas(@RequestHeader (name = "User-Token", required = false) String userToken,
                                               @RequestParam(value = "area_id", required = false) Long areaId,
                                               @RequestParam(value = "city_id", required = false) Long cityId,
                                               @RequestParam(value = "country_id", required = false) Long countryId){
        return addressService.getOrgSubAreas(areaId, cityId, countryId);
    }
}
