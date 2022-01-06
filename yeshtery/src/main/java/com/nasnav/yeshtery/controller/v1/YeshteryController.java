package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.*;
import com.nasnav.dto.request.SearchParameters;
import com.nasnav.dto.request.product.ProductRateDTO;
import com.nasnav.dto.response.CategoryDto;
import com.nasnav.dto.response.YeshteryOrganizationDTO;
import com.nasnav.dto.response.navbox.*;
import com.nasnav.dto.response.navbox.ProductRateRepresentationObject;
import com.nasnav.dto.response.navbox.SearchResult;
import com.nasnav.enumerations.SeoEntityType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.request.LocationShopsParam;
import com.nasnav.request.ProductSearchParam;
import com.nasnav.service.*;
import com.nasnav.yeshtery.YeshteryConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static com.nasnav.commons.utils.EntityUtils.allIsNull;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequestMapping(YeshteryController.API_PATH)
@Tag(name = "Yeshtery Controller")
@CrossOrigin("*")
@EnableJpaRepositories
public class YeshteryController {

    static final String API_PATH = YeshteryConstants.API_PATH +"/yeshtery";

    @Autowired
    private ShopService shopService;
    @Autowired
    private ProductService productService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private FileService fileService;
    @Autowired
    private SearchService searchService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private OrganizationService orgService;
    @Autowired
    private SeoService seoService;

    @GetMapping(value = "/location_shops", produces = APPLICATION_JSON_VALUE)
    public List<ShopRepresentationObject> getLocationShops(@RequestParam(value = "name", required = false) String name,
                                                           @RequestParam(name = "org_id", required = false) Long orgId,
                                                           @RequestParam(value = "area_id", required = false) Long areaId,
                                                           @RequestParam(required = false) Double minLongitude,
                                                           @RequestParam(required = false) Double maxLongitude,
                                                           @RequestParam(required = false) Double minLatitude,
                                                           @RequestParam(required = false) Double maxLatitude,
                                                           @RequestParam(required = false) Double longitude,
                                                           @RequestParam(required = false) Double latitude,
                                                           @RequestParam(required = false) Double radius,
                                                           @RequestParam(required = false, defaultValue = "true") Boolean searchInTags,
                                                           @RequestParam(value = "product_type", required = false) Integer[] productType) {
        LocationShopsParam param = new LocationShopsParam(name, orgId, areaId, minLongitude, minLatitude, maxLongitude, maxLatitude,
                longitude, latitude, radius, true, searchInTags.booleanValue(), productType);
        return shopService.getLocationShops(param);
    }



    @GetMapping(value = "/related_products", produces = APPLICATION_JSON_VALUE)
    public List<ProductRepresentationObject> getRelatedProducts(@RequestParam("product_id") Long productId) {
        return productService.getRelatedProducts(productId);
    }

    @GetMapping(value = "/organizations", produces = APPLICATION_JSON_VALUE)
    public List<YeshteryOrganizationDTO> getRelatedProducts(@RequestParam(value = "category_id", required = false) List<Long> categoryIds) {
        return organizationService.getYeshteryOrganizations(categoryIds);
    }

    @GetMapping(value = "/brand", produces = APPLICATION_JSON_VALUE)
    public Organization_BrandRepresentationObject getBrandById(@RequestParam(name = "brand_id") Long brandId) {
        return brandService.getBrandById(brandId, true);
    }

    @GetMapping(value = "/brands", produces = APPLICATION_JSON_VALUE)
    public PageImpl<Organization_BrandRepresentationObject> getYeshteryBrands(@RequestParam(required = false, defaultValue = "0") Integer start,
                                                                              @RequestParam(required = false, defaultValue = "10") Integer count,
                                                                              @RequestParam(value = "org_id", required = false) Long orgId,
                                                                              @RequestParam(value = "brand_id", required = false) Set<Long> brands) {
        return brandService.getYeshteryBrands(start, count, orgId, brands);
    }

    @GetMapping(value = "variants", produces = APPLICATION_JSON_VALUE)
    public VariantsResponse getVariants(@RequestParam(required = false, defaultValue = "") String name,                                                                                                                                                                                                  
                                        @RequestParam(required = false, defaultValue = "0") Integer start,
                                        @RequestParam(required = false, defaultValue = "10") Integer count) {
        return productService.getVariantsForYeshtery(name, start, count);
    }

    @GetMapping(value="/review", produces = APPLICATION_JSON_VALUE)
    public List<ProductRateRepresentationObject> getVariantRatings(@RequestParam(value = "variant_id")Long variantId) {
        return reviewService.getYeshteryVariantRatings(variantId);
    }

    @GetMapping(value = "collection", produces = APPLICATION_JSON_VALUE)
    public ProductDetailsDTO getCollectionById(@RequestParam Long id) {
        return productService.getCollection(id);
    }

    @Operation(description =  "Get information about a specific product", summary = "productInfo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK"),
            @ApiResponse(responseCode = " 404" ,description = "Product does not exist")
    })
    @GetMapping(value="/product",produces=APPLICATION_JSON_VALUE)
    public ProductDetailsDTO getProduct(@RequestParam(name = "product_id") Long productId,
                                        @RequestParam(name = "shop_id",required=false) Long shopId,
                                        @RequestParam(value = "include_out_of_stock", required = false, defaultValue = "false") Boolean includeOutOfStock)
            throws BusinessException {
        var params = new ProductFetchDTO(productId);
        params.setShopId(shopId);
        params.setCheckVariants(true);
        params.setIncludeOutOfStock(includeOutOfStock);
        params.setOnlyYeshteryProducts(true);
        return productService.getProduct(params);
    }

    @GetMapping("products")
    public ProductsResponse getProducts(ProductSearchParam productSearchParam) throws BusinessException {
        productSearchParam.setYeshtery_products(true);
        return productService.getProducts(productSearchParam);
    }

    @GetMapping(value = "filters", produces = APPLICATION_JSON_VALUE)
    public ProductsFiltersResponse getProductsFilters(ProductSearchParam productSearchParam) throws BusinessException {
        productSearchParam.setYeshtery_products(true);
        return productService.getProductAvailableFilters(productSearchParam);
    }

    @GetMapping(value="countries", produces=MediaType.APPLICATION_JSON_VALUE)
    public Map<String, CountriesRepObj> getCountries(@RequestParam(value = "hide_empty_cities", required = false, defaultValue = "true") Boolean hideEmptyCities) {
        return addressService.getCountries(hideEmptyCities, null);
    }

    @GetMapping( path="files/{orgId}/{url}")
    public void downloadFile(HttpServletRequest request, HttpServletResponse resp,
                             @PathVariable Long orgId,
                             @PathVariable String url,
                             @RequestParam(required = false) Integer height,
                             @RequestParam(required = false) Integer width,
                             @RequestParam(required = false) String type) throws ServletException, IOException {
        String resourceInternalUrl;
        String modUrl = "/"+orgId+"/"+url;
        if (height != null || width != null) {
            resourceInternalUrl = fileService.getResizedImageInternalUrl(modUrl, width, height, type);
        } else {
            resourceInternalUrl = fileService.getResourceInternalUrl(modUrl);
        }
        resp.setStatus(HttpStatus.OK.value());

        RequestDispatcher dispatcher = request.getRequestDispatcher(resourceInternalUrl);
        dispatcher.forward(request, resp);
    }

    @GetMapping( path="files/{url}")
    public void downloadFile(HttpServletRequest request, HttpServletResponse resp,
                             @PathVariable String url,
                             @RequestParam(required = false) Integer height,
                             @RequestParam(required = false) Integer width,
                             @RequestParam(required = false) String type) throws ServletException, IOException {
        String resourceInternalUrl;
        String modUrl = "/"+url;
        if (height != null || width != null) {
            resourceInternalUrl = fileService.getResizedImageInternalUrl(modUrl, width, height, type);
        } else {
            resourceInternalUrl = fileService.getResourceInternalUrl(modUrl);
        }
        resp.setStatus(HttpStatus.OK.value());

        RequestDispatcher dispatcher = request.getRequestDispatcher(resourceInternalUrl);
        dispatcher.forward(request, resp);
    }

    @Operation(description =  "search the data", summary = "search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK")
    })
    @GetMapping(value="/search", produces= MediaType.APPLICATION_JSON_VALUE)
    public Mono<SearchResult> search(SearchParameters params) {
        return searchService.search(params, true);
    }



    @Operation(description =  "get categories tree", summary = "getCategories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK")
    })
    @GetMapping(value="/categories", produces= MediaType.APPLICATION_JSON_VALUE)
    public List<CategoryDto> getCategories() {
        return categoryService.getCategoriesTree();
    }

    @GetMapping(value = "/shop", produces = MediaType.APPLICATION_JSON_VALUE)
    public ShopRepresentationObject getShopById(@RequestParam("shop_id") Long shopId) {
        return shopService.getShopById(shopId);
    }

    @GetMapping(value = "/shops", produces = APPLICATION_JSON_VALUE)
    public List<ShopRepresentationObject> getShopsByOrganization(@RequestParam(name = "org_id") Long orgId) {
        return shopService.getOrganizationShops(orgId, false);
    }

    @Operation(description =  "return seo keywords", summary = "getSeo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK")
    })
    @GetMapping(value="/seo", produces=MediaType.APPLICATION_JSON_VALUE)
    public List<SeoKeywordsDTO> getSeoKeywords(
            @RequestParam(value = "type", required = true) SeoEntityType type,
            @RequestParam(value = "id", required = true)Long entityId) {
        return seoService.getSeoKeywords(entityId, type);
    }

    @GetMapping(value = "/organization")
    public OrganizationRepresentationObject getOrgInfo(@RequestParam(name = "p_name", required = false) String organizationName,
                                                       @RequestParam(name = "org_id", required = false) Long organizationId,
                                                       @RequestParam(name = "url", required = false) String url) throws BusinessException {
        if (allIsNull(organizationName, organizationId, url))
            throw new BusinessException("Provide org_id or p_name or url request params", "", BAD_REQUEST);

        if (organizationName != null)
            return organizationService.getOrganizationByName(organizationName, 1);

        if (url != null) {
            Pair domain = organizationService.getOrganizationAndSubdirsByUrl(url, 1);
            OrganizationRepresentationObject orgObj = organizationService.getOrganizationById(domain.getFirst(), 1);
            orgObj.setSubDir(domain.getSecond());
            return orgObj;
        }
        return organizationService.getOrganizationById(organizationId, 1);
    }

    @GetMapping(value ="/tagstree", produces = APPLICATION_JSON_VALUE)
    public List<TagsTreeNodeDTO> getTagsTree(@RequestParam(name = "org_id") Long organizationId) throws BusinessException {
        return categoryService.getOrganizationTagsTree(organizationId);
    }

    @GetMapping(value = "/tags", produces = APPLICATION_JSON_VALUE)
    public List<TagsRepresentationObject> getTags(@RequestParam(value = "category_name", required = false) String categoryName) {
        return categoryService.getYeshteryOrganizationsTags(categoryName);
    }

    @GetMapping(value = "payments", produces = APPLICATION_JSON_VALUE)
    public LinkedHashMap<String, Map<String, String>> getOrganizationPaymentGateways(@RequestParam(value = "org_id") Long orgId,
                                                                                     @RequestParam(value = "delivery", required = false) String deliveryService) {
        return orgService.getOrganizationPaymentGateways(orgId, deliveryService);
    }

    @PostMapping(value = "review", consumes = APPLICATION_JSON_VALUE)
    public void rateProduct(@RequestHeader(name = "User-Token", required = false) String token,
                            @RequestBody ProductRateDTO dto) {
        reviewService.rateProduct(dto);
    }
}
