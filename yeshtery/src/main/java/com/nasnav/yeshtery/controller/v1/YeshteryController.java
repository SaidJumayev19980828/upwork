package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.*;
import com.nasnav.dto.request.SearchParameters;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.dto.response.CategoryDto;
import com.nasnav.dto.response.ProductsPositionDTO;
import com.nasnav.dto.response.YeshteryOrganizationDTO;
import com.nasnav.dto.response.navbox.*;
import com.nasnav.dto.response.navbox.ProductRateRepresentationObject;
import com.nasnav.dto.response.navbox.SearchResult;
import com.nasnav.enumerations.SeoEntityType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.request.LocationShopsParam;
import com.nasnav.request.ProductSearchParam;
import com.nasnav.service.*;
import com.nasnav.persistence.YeshteryRecommendationRatingData;
import com.nasnav.persistence.YeshteryRecommendationSellingData;
import com.nasnav.yeshtery.YeshteryConstants;
import com.nasnav.yeshtery.services.interfaces.YeshteryRecommendationService;
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

    static final String API_PATH = YeshteryConstants.API_PATH +"/";

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
    private OrganizationService organizationService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ReviewService reviewService;

    @Autowired
    private SeoService seoService;

    @Autowired
    private YeshteryRecommendationService recommendationService;


    @Autowired
    private CartService cartService;

    @Autowired
    private ShippingManagementService shippingService;

    @Autowired
    private WishlistService wishlistService;


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
                                                                              @RequestParam(value = "brand_id", required = false) Set<Long> brands) {
        return brandService.getYeshteryBrands(start, count, brands);
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
        return shop360Svc.getThreeSixtyShops(shopId, true);
    }

    @GetMapping(value = "/360view/shop", produces = MediaType.APPLICATION_JSON_VALUE)
    public ShopRepresentationObject getShopById(@RequestParam("shop_id") Long shopId) {
        return shopService.getShopById(shopId);
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

    @Operation(description =  "return recommend product rating by tag & org", summary = "getRecommendProductRating")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK")
    })
    @GetMapping(value="/recommend/rating", produces=MediaType.APPLICATION_JSON_VALUE)
    public List<YeshteryRecommendationRatingData> getRecommendProductRating(@RequestParam(value = "orgid", required = false, defaultValue = "-1")Long orgId,
                                                                            @RequestParam(value = "tagid", required = false, defaultValue = "-1")Long tagId) {
        return recommendationService.getListOfTopRatingProduct(orgId, tagId);
    }

    @Operation(description =  "return recommend product selling by shop & tag & org", summary = "getRecommendProductSelling")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK")
    })
    @GetMapping(value="/recommend/selling", produces=MediaType.APPLICATION_JSON_VALUE)
    public List<YeshteryRecommendationSellingData> getRecommendProductSellingByShopTagAPI(@RequestParam(value = "tagid" , required = false, defaultValue = "-1")Long tagId,
                                                                                          @RequestParam(value = "shopid", required = false, defaultValue = "-1")Long shopId,
                                                                                          @RequestParam(value = "orgid" , required = false, defaultValue = "-1")Long orgId) {
        return recommendationService.getListOfTopSellerProduct(shopId, tagId, orgId);
    }

    @Operation(description =  "return recommend similarity products", summary = "getListOfSimilarityAPI")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK")
    })
    @GetMapping(value="/recommend/similarity", produces=MediaType.APPLICATION_JSON_VALUE)
    public List<ProductEntity> getListOfSimilarityAPI(@RequestParam(required = true, value = "itemcounts") Integer recommendedItemsCount,
                                                      @RequestParam(required = true, value = "userid") Integer userId) {
        return recommendationService.getListOfSimilarity(recommendedItemsCount, userId);
    }

    @GetMapping(value="/cart",produces= APPLICATION_JSON_VALUE)
    public Cart getYeshteryCart(
            @RequestParam(value = "promo", required = false, defaultValue = "") String promoCode) {
        return cartService.getCart(promoCode);
    }

    @PostMapping(value = "/cart/item", consumes = APPLICATION_JSON_VALUE, produces= APPLICATION_JSON_VALUE)
    public Cart addCartItem(@RequestBody CartItem item,
                            @RequestParam(value = "promo", required = false, defaultValue = "") String promoCode) {
        return cartService.addCartItem(item, promoCode);
    }

    @DeleteMapping(value = "/cart/item", produces=APPLICATION_JSON_VALUE)
    public Cart deleteCartItem(@RequestParam("item_id") Long itemId,
                               @RequestParam("stock_id") Long stockId,
                               @RequestParam(value = "promo", required = false, defaultValue = "") String promoCode) {
        return cartService.deleteYeshteryCartItem(itemId, promoCode, stockId);
    }

    @PostMapping(value = "/cart/checkout", consumes = APPLICATION_JSON_VALUE, produces= APPLICATION_JSON_VALUE)
    public Order checkoutCart(@RequestBody CartCheckoutDTO dto) {
        return cartService.checkoutYeshteryCart(dto);
    }


    @GetMapping(path = "/shipping/offers", produces= APPLICATION_JSON_VALUE)
    public List<ShippingOfferDTO> getShippingOffers(
            @RequestParam("customer_address") Long customerAddress) {
        return shippingService.getYeshteryShippingOffers(customerAddress);
    }

    @GetMapping(value = "/wishlist")
    public Wishlist getWishlist() {
        return wishlistService.getYeshteryWishlist();
    }

    @PostMapping(value = "/wishlist/item", consumes = APPLICATION_JSON_VALUE, produces= APPLICATION_JSON_VALUE)
    public Wishlist addWishlistItem(@RequestBody WishlistItem item) {
        return wishlistService.addYeshteryWishlistItem(item);
    }

    @DeleteMapping(value = "/wishlist/item", produces=APPLICATION_JSON_VALUE)
    public Wishlist deleteWishlistItem(@RequestHeader(name = "User-Token", required = false) String userToken, @RequestParam("item_id") Long itemId) {
        return wishlistService.deleteYeshteryWishlistItem(itemId);
    }

    @PostMapping(value = "/wishlist/item/into_cart", consumes = APPLICATION_JSON_VALUE, produces= APPLICATION_JSON_VALUE)
    public Cart moveWishlistItemIntoCart(@RequestBody WishlistItemQuantity items) {
        return wishlistService.moveYeshteryWishlistItemsToCart(items);
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
}
