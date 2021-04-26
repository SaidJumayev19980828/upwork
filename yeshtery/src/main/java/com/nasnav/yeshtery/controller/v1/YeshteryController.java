package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.*;
import com.nasnav.dto.request.SearchParameters;
import com.nasnav.dto.response.navbox.SearchResult;
import com.nasnav.dto.response.navbox.VariantsResponse;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.BrandService;
import com.nasnav.service.ProductService;
import com.nasnav.service.SearchService;
import com.nasnav.service.ShopService;
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
    private BrandService brandService;

    @Autowired
    private SearchService searchService;

    @GetMapping(value = "/location_shops", produces = APPLICATION_JSON_VALUE)
    public List<ShopRepresentationObject> getLocationShops(@RequestParam(value = "name", required = false, defaultValue = "") String name) {
        return shopService.getLocationShops(name);
    }



    @GetMapping(value = "/related_products", produces = APPLICATION_JSON_VALUE)
    public List<ProductRepresentationObject> getRelatedProducts(@RequestParam("product_id") Long productId) {
        return productService.getRelatedProducts(productId);
    }

    @GetMapping(value = "/brand", produces = APPLICATION_JSON_VALUE)
    public Organization_BrandRepresentationObject getBrandById(@RequestParam(name = "brand_id") Long brandId) {
        return brandService.getBrandById(brandId);
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



    @Operation(description =  "search the data", summary = "search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK")
    })
    @GetMapping(value="/search", produces= MediaType.APPLICATION_JSON_VALUE)
    public Mono<SearchResult> search(SearchParameters params) {
        return searchService.search(params, true);
    }
}
