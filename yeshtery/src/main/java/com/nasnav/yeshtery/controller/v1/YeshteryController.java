package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.*;
import com.nasnav.dto.request.SearchParameters;
import com.nasnav.dto.response.navbox.SearchResult;
import com.nasnav.enumerations.SeoEntityType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.request.LocationShopsParam;
import com.nasnav.service.CategoryService;
import com.nasnav.service.ProductService;
import com.nasnav.service.SearchService;
import com.nasnav.service.ShopService;
import com.nasnav.dto.response.CategoryDto;
import com.nasnav.yeshtery.services.SeoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

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
    private SearchService searchService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SeoService seoService;

    @GetMapping(value = "/location_shops", produces = APPLICATION_JSON_VALUE)
    public List<ShopRepresentationObject> getLocationShops(@RequestParam(value = "name", required = false, defaultValue = "") String name,
                                                           @RequestParam(name = "org_id", required = false) Long orgId,
                                                           @RequestParam(value = "area_id", required = false) Long areaId,
                                                           @RequestParam(required = false) Double longitude,
                                                           @RequestParam(required = false) Double latitude,
                                                           @RequestParam(required = false) Double radius,
                                                           @RequestParam(value = "search_in_tags", required = false, defaultValue = "true") Boolean searchInTags,
                                                           @RequestParam(value = "product_type", required = false) Integer[] productType) {
        LocationShopsParam param = new LocationShopsParam(name, orgId, areaId, longitude, latitude, radius, true, searchInTags.booleanValue(), productType);
        return shopService.getLocationShops(param);
    }



    @GetMapping(value = "/related_products", produces = APPLICATION_JSON_VALUE)
    public List<ProductRepresentationObject> getRelatedProducts(@RequestParam("product_id") Long productId) {
        return productService.getRelatedProducts(productId);
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
}
