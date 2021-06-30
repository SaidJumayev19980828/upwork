package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.*;
import com.nasnav.dto.request.SearchParameters;
import com.nasnav.dto.response.ProductsPositionDTO;
import com.nasnav.dto.response.navbox.SearchResult;
import com.nasnav.enumerations.SeoEntityType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.request.LocationShopsParam;
import com.nasnav.dto.response.navbox.VariantsResponse;
import com.nasnav.service.CategoryService;
import com.nasnav.service.ProductService;
import com.nasnav.service.SearchService;
import com.nasnav.service.ShopService;
import com.nasnav.dto.response.CategoryDto;
import com.nasnav.request.ProductSearchParam;
import com.nasnav.service.*;
import com.nasnav.yeshtery.persistence.YeshteryRecommendationRatingData;
import com.nasnav.yeshtery.persistence.YeshteryRecommendationSellingData;
import com.nasnav.yeshtery.service.YeshteryRecommendationService;
import com.nasnav.yeshtery.services.SeoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/v1/yeshtery")
@Tag(name = "Yeshtery Controller")
@CrossOrigin("*")
public class YeshteryController {

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
    private ShopThreeSixtyService shop360Svc;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SeoService seoService;

    @Autowired
    private YeshteryRecommendationService recommendationService;

    @GetMapping(value = "/location_shops", produces = APPLICATION_JSON_VALUE)
    public List<ShopRepresentationObject> getLocationShops(@RequestParam(value = "name", required = false, defaultValue = "") String name,
                                                           @RequestParam(name = "org_id", required = false) Long orgId,
                                                           @RequestParam(value = "area_id", required = false) Long areaId,
                                                           @RequestParam(required = false) Double minLongitude,
                                                           @RequestParam(required = false) Double maxLongitude,
                                                           @RequestParam(required = false) Double minLatitude,
                                                           @RequestParam(required = false) Double maxLatitude,
                                                           @RequestParam(required = false) Double longitude,
                                                           @RequestParam(required = false) Double latitude,
                                                           @RequestParam(required = false) Double radius,
                                                           @RequestParam(value = "search_in_tags", required = false, defaultValue = "true") Boolean searchInTags,
                                                           @RequestParam(value = "product_type", required = false) Integer[] productType) {
        LocationShopsParam param = new LocationShopsParam(name, orgId, areaId, minLongitude, minLatitude, maxLongitude, maxLatitude,
                longitude, latitude, radius, true, searchInTags.booleanValue(), productType);
        return shopService.getLocationShops(param);
    }



    @GetMapping(value = "/related_products", produces = APPLICATION_JSON_VALUE)
    public List<ProductRepresentationObject> getRelatedProducts(@RequestParam("product_id") Long productId) {
        return productService.getRelatedProducts(productId);
    }

    @GetMapping(value = "/brand", produces = APPLICATION_JSON_VALUE)
    public Organization_BrandRepresentationObject getBrandById(@RequestParam(name = "brand_id") Long brandId) {
        return brandService.getBrandById(brandId);
    }

    @GetMapping(value = "/brands", produces = APPLICATION_JSON_VALUE)
    public PageImpl<Organization_BrandRepresentationObject> getYeshteryBrands(@RequestParam(required = false, defaultValue = "0") Integer start,
                                                                              @RequestParam(required = false, defaultValue = "10") Integer count) {
        return brandService.getYeshteryBrands(start, count);
    }

    @GetMapping(value = "variants", produces = APPLICATION_JSON_VALUE)
    public VariantsResponse getVariants(@RequestParam(required = false, defaultValue = "") String name,
                                        @RequestParam(required = false, defaultValue = "0") Integer start,
                                        @RequestParam(required = false, defaultValue = "10") Integer count) {
        return productService.getVariantsForYeshtery(name, start, count);
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
    public ProductsResponse getProducts(ProductSearchParam productSearchParam) throws BusinessException, InvocationTargetException, IllegalAccessException {
        productSearchParam.setYeshtery_products(true);
        return productService.getProducts(productSearchParam);
    }

    @GetMapping(value="countries", produces=MediaType.APPLICATION_JSON_VALUE)
    public Map<String, CountriesRepObj> getCountries(@RequestParam(value = "hide_empty_cities", required = false, defaultValue = "true") Boolean hideEmptyCities) {
        return addressService.getCountries(hideEmptyCities, null);
    }

    @GetMapping( path="files/{path}")
    public void downloadFile(HttpServletRequest request, HttpServletResponse resp,
                             @PathVariable String url,
                             @RequestParam(required = false) Integer height,
                             @RequestParam(required = false) Integer width,
                             @RequestParam(required = false) String type) throws ServletException, IOException {
        String resourceInternalUrl;
        if (height != null || width != null) {
            resourceInternalUrl = fileService.getResizedImageInternalUrl(url, width, height, type);
        } else {
            resourceInternalUrl = fileService.getResourceInternalUrl(url);
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



    @GetMapping(value = "/360view/json_data", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getShop360JsonInfo(@RequestParam("shop_id") Long shopId,
                                     @RequestParam String type,
                                     @RequestParam(defaultValue = "true") Boolean published) {
        return shop360Svc.getShop360JsonInfo(shopId, type, published);
    }

    @GetMapping(value = "/360view/sections", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map getShop360Sections(@RequestParam("shop_id") Long shopId) {
        Map<String, List> res = new HashMap<>();
        res.put("floors", shop360Svc.getSections(shopId));
        return res;
    }

    @GetMapping(value = "/360view/shops", produces = MediaType.APPLICATION_JSON_VALUE)
    public ShopThreeSixtyDTO getShop360Shops(@RequestParam("shop_id") Long shopId) {
        return shop360Svc.getThreeSixtyShops(shopId);
    }

    @GetMapping(value = "/360view/products_positions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductsPositionDTO getShop360ProductsPositions(@RequestParam("shop_id") Long shopId,
                                                           @RequestParam(defaultValue = "2") short published,
                                                           @RequestParam(value = "scene_id", required = false) Long sceneId,
                                                           @RequestParam(value = "section_id", required = false) Long sectionId,
                                                           @RequestParam(value = "floor_id", required = false) Long floorId) {
        return shop360Svc.getProductsPositions(shopId, published, sceneId, sectionId, floorId);
    }

    @GetMapping(value = "/360view/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public LinkedHashMap getShop360products(@RequestParam("shop_id") Long shopId,
                                            @RequestParam(required = false) String name,
                                            @RequestParam(required = false, defaultValue = "5") Integer count,
                                            @RequestParam(value = "product_type", required = false) Integer productType,
                                            @RequestParam(value = "has_360", required = false, defaultValue = "false") boolean has360,
                                            @RequestParam(value = "published", required = false) Short published,
                                            @RequestParam(value = "include_out_of_stock", required = false, defaultValue = "false") Boolean includeOutOfStock)
            throws BusinessException {
        return shop360Svc.getShop360Products(shopId, name, count, productType, published, has360, includeOutOfStock);
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

    @Operation(description =  "return recommend product rating", summary = "getRecommendProductRating")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK")
    })
    @GetMapping(value="/recommend/rating", produces=MediaType.APPLICATION_JSON_VALUE)
    public List<YeshteryRecommendationRatingData> getRecommendProductRating(@RequestParam(value = "orgid", required = false, defaultValue = "-1")Long orgId) {
        return recommendationService.getListOfTopRatingProduct(orgId);
    }

    @Operation(description =  "return recommend product rating by tag", summary = "getRecommendProductRatingByTag")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK")
    })
    @GetMapping(value="/recommend/rating/tag", produces=MediaType.APPLICATION_JSON_VALUE)
    public List<YeshteryRecommendationRatingData> getRecommendProductRatingByTagAPI(@RequestParam(value = "tagid", required = true)Long tagId,
                                                                                    @RequestParam(value = "orgid", required = false, defaultValue = "-1")Long orgId) {
        return recommendationService.getListOfTopRatingProductByTag(tagId, orgId);
    }

    @Operation(description =  "return recommend product selling", summary = "getRecommendProductSelling")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK")
    })
    @GetMapping(value="/recommend/selling", produces=MediaType.APPLICATION_JSON_VALUE)
    public List<YeshteryRecommendationSellingData> getRecommendProductSellingAPI(@RequestParam(value = "orgid", required = false, defaultValue = "-1")Long orgId) {
        return recommendationService.getListOfTopSellerProduct(orgId);
    }

    @Operation(description =  "return recommend product selling by tag", summary = "getRecommendProductSellingByTag")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK")
    })
    @GetMapping(value="/recommend/selling/tag", produces=MediaType.APPLICATION_JSON_VALUE)
    public List<YeshteryRecommendationSellingData> getRecommendProductSellingByTagAPI(@RequestParam(value = "tagid", required = true)Long tagId,
                                                                                      @RequestParam(value = "orgid", required = false, defaultValue = "-1")Long orgId) {
        return recommendationService.getListOfTopSellerProductByTag(tagId, orgId);
    }

    @Operation(description =  "return recommend product selling by shop", summary = "getRecommendProductSellingByShop")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK")
    })
    @GetMapping(value="/recommend/selling/shop", produces=MediaType.APPLICATION_JSON_VALUE)
    public List<YeshteryRecommendationSellingData> getRecommendProductSellingByShopAPI(@RequestParam(value = "shopid", required = true)Long shopId,
                                                                                       @RequestParam(value = "orgid", required = false, defaultValue = "-1")Long orgId) {
        return recommendationService.getListOfTopSellerProductByShop(shopId, orgId);
    }

    @Operation(description =  "return recommend product selling by shop & tag", summary = "getRecommendProductSellingByShopTag")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK")
    })
    @GetMapping(value="/recommend/selling/shoptag", produces=MediaType.APPLICATION_JSON_VALUE)
    public List<YeshteryRecommendationSellingData> getRecommendProductSellingByShopTagAPI(@RequestParam(value = "tagid", required = true)Long tagId,
                                                                                          @RequestParam(value = "shopid", required = true)Long shopId,
                                                                                          @RequestParam(value = "orgid", required = false, defaultValue = "-1")Long orgId) {
        return recommendationService.getListOfTopSellerProductByShopTag(shopId, tagId, orgId);
    }

    @Operation(description =  "return recommend similarity products", summary = "getListOfSimilarityProducts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK")
    })
    @GetMapping(value="/recommend/similarityproducts", produces=MediaType.APPLICATION_JSON_VALUE)
    public List<ProductEntity> getListOfSimilarityProductsAPI(@RequestParam(required = true, value = "itemcounts") Integer recommendedItemsCount,
                                                           @RequestParam(required = true, value = "userid") Integer userId) {
        return recommendationService.getListOfSimilarityProducts(recommendedItemsCount, userId);
    }

    @Operation(description =  "return recommend similarity item orders", summary = "getListOfUserSimilarityItemOrders")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK")
    })
    @GetMapping(value="/recommend/similarityitemorders", produces=MediaType.APPLICATION_JSON_VALUE)
    public List<ProductEntity> getListOfUserSimilarityItemOrdersAPI(@RequestParam(required = true, value = "itemcounts") Integer recommendedItemsCount,
                                                                 @RequestParam(required = true, value = "userid") Integer userId) {
        return recommendationService.getListOfUserSimilarityItemOrders(recommendedItemsCount, userId);
    }
}
