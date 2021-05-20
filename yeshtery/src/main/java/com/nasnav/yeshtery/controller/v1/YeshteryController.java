package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.*;
import com.nasnav.dto.request.SearchParameters;
import com.nasnav.dto.response.ProductsPositionDTO;
import com.nasnav.dto.response.navbox.SearchResult;
import com.nasnav.dto.response.navbox.VariantsResponse;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.request.ProductSearchParam;
import com.nasnav.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping(value = "/location_shops", produces = APPLICATION_JSON_VALUE)
    public List<ShopRepresentationObject> getLocationShops(@RequestParam(value = "name", required = false, defaultValue = "") String name) {
        return shopService.getLocationShops(name, null);
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

    @GetMapping("products")
    public ProductsResponse getProducts(ProductSearchParam productSearchParam) throws BusinessException, InvocationTargetException, IllegalAccessException {
        productSearchParam.setYeshtery_products(true);
        return productService.getProducts(productSearchParam);
    }

    @GetMapping(value="countries", produces=MediaType.APPLICATION_JSON_VALUE)
    public Map<String, CountriesRepObj> getCountries(@RequestParam(value = "hide_empty_cities", required = false, defaultValue = "true") Boolean hideEmptyCities) {
        return addressService.getCountries(hideEmptyCities, null);
    }

    @GetMapping( path="files/**")
    public void downloadFile(HttpServletRequest request, HttpServletResponse resp,
                             @RequestParam(required = false) Integer height,
                             @RequestParam(required = false) Integer width,
                             @RequestParam(required = false) String type) throws ServletException, IOException {
        String url = request.getRequestURI().replaceFirst("/v1/yeshtery/files", "");
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
}
