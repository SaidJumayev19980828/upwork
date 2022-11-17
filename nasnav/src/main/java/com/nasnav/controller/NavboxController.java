package com.nasnav.controller;

import com.nasnav.dto.*;
import com.nasnav.dto.request.SearchParameters;
import com.nasnav.dto.response.navbox.ProductRateRepresentationObject;
import com.nasnav.dto.response.navbox.SearchResult;
import com.nasnav.dto.response.navbox.VariantsResponse;
import com.nasnav.enumerations.SeoEntityType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.request.LocationShopsParam;
import com.nasnav.request.ProductSearchParam;
import com.nasnav.request.SitemapParams;
import com.nasnav.service.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.nasnav.commons.utils.EntityUtils.allIsNull;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/navbox")
public class NavboxController {

	@Autowired
	private BrandService brandService;
	@Autowired
	private ShopService shopService;
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private FileService fileService;
	@Autowired
	private ProductService productService;
	@Autowired
	private ReviewServiceImpl reviewService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private AddressService addressService;
	@Autowired
	private SearchService searchService;
	@Autowired
	private SeoService seoService;

	@Autowired
	private PromotionsServiceImpl promotionsService;

	@GetMapping(value = "/brand", produces = APPLICATION_JSON_VALUE)
	public Organization_BrandRepresentationObject getBrandById(@RequestParam(name = "brand_id") Long brandId) {
		return brandService.getBrandById(brandId, false);
	}

	@GetMapping(value = "/organization", produces = APPLICATION_JSON_VALUE)
	public OrganizationRepresentationObject getOrganizationByName(
			@RequestParam(name = "p_name", required = false) String organizationName,
			@RequestParam(name = "org_id", required = false) Long organizationId,
			@RequestParam(name = "url", required = false) String url) throws BusinessException {

		if (allIsNull(organizationName, organizationId, url))
			throw new BusinessException("Provide org_id or p_name or url request params", "", BAD_REQUEST);

		if (organizationName != null)
			return organizationService.getOrganizationByName(organizationName, 0);

		if (url != null) {
			Pair domain = organizationService.getOrganizationAndSubdirsByUrl(url, 0);
			OrganizationRepresentationObject orgObj = organizationService.getOrganizationById(domain.getFirst(), 0);
			orgObj.setSubDir(domain.getSecond());
			return orgObj;
		}
		return organizationService.getOrganizationById(organizationId, 0);
	}

	@GetMapping(value = "/shops", produces = APPLICATION_JSON_VALUE)
	public List<ShopRepresentationObject> getShopsByOrganization(@RequestParam(name = "org_id") Long orgId) {
		return shopService.getOrganizationShops(orgId, false);
	}

	@GetMapping(value = "/shop", produces = APPLICATION_JSON_VALUE)
	public ShopRepresentationObject getShopById(@RequestParam(name = "shop_id") Long shopId) {
		return shopService.getShopById(shopId);
	}

	@GetMapping(value = "/products", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getProducts(ProductSearchParam productSearchParam) throws BusinessException {
		ProductsResponse productsResponse = productService.getProducts(productSearchParam);
		if (productsResponse == null)
			return new ResponseEntity<>(NO_CONTENT);
		return new ResponseEntity<>(productsResponse, OK);
	}

	@Operation(description =  "Get list of products (POST version)", summary = "productList")
	@PostMapping("/products")
	public ResponseEntity<?> getProductsWithFeaturesFilter(@RequestBody ProductSearchParam productSearchParam) throws BusinessException {
		ProductsResponse productsResponse = productService.getProducts(productSearchParam);
		if (productsResponse == null)
			return new ResponseEntity<>(NO_CONTENT);
		return new ResponseEntity<>(productsResponse, OK);
	}

	@GetMapping(value = "/filters", produces = APPLICATION_JSON_VALUE)
	public ProductsFiltersResponse getProductsFilters(ProductSearchParam productSearchParam) throws BusinessException {
		return productService.getProductAvailableFilters(productSearchParam);
	}

	@GetMapping(value="/product",produces=APPLICATION_JSON_VALUE)
	public ProductDetailsDTO getProduct(@RequestParam(name = "product_id") Long productId,
										@RequestParam(name = "shop_id",required=false) Long shopId,
										@RequestParam(value = "include_out_of_stock", required = false, defaultValue = "false") Boolean includeOutOfStock)
			throws BusinessException {
		var params = new ProductFetchDTO(productId);
		params.setShopId(shopId);
		params.setCheckVariants(true);
		params.setIncludeOutOfStock(includeOutOfStock);
		params.setOnlyYeshteryProducts(false);
		return productService.getProduct(params);
	}

	@GetMapping(value = "collection", produces = APPLICATION_JSON_VALUE)
	public ProductDetailsDTO getCollectionById(@RequestParam Long id) {
		return productService.getCollection(id);
	}

	@GetMapping(value = "variants", produces = APPLICATION_JSON_VALUE)
	public VariantsResponse getVariants(@RequestParam("org_id") Long orgId,
										@RequestParam(required = false, defaultValue = "") String name,
										@RequestParam(required = false, defaultValue = "0") Integer start,
										@RequestParam(required = false, defaultValue = "10") Integer count) {
		return productService.getVariants(orgId, name, start, count);
	}

	@GetMapping(value="/attributes", produces = APPLICATION_JSON_VALUE)
	public List<ExtraAttributesRepresentationObject> getOrganizationAttributes(@RequestParam(name = "org_id", required = false) Long organizationId) {
		return organizationService.getOrganizationExtraAttributesById(organizationId);
	}

	@GetMapping(value="/categories", produces = APPLICATION_JSON_VALUE)
	public List<CategoryRepresentationObject> getCategories(@RequestParam(name = "org_id", required = false) Long organizationId,
										   					@RequestParam(name = "category_id", required = false) Long categoryId)  {
		return categoryService.getCategories(organizationId, categoryId);
	}

	@GetMapping(value ="/tagstree",produces=MediaType.APPLICATION_JSON_VALUE)
	public List<TagsTreeNodeDTO> getTagsTree(@RequestParam(name = "org_id", required = false) Long organizationId) throws BusinessException {
		return categoryService.getOrganizationTagsTree(organizationId);
	}

	@GetMapping(value = "/tags", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<TagsRepresentationObject> getTags(@RequestParam(name = "org_id") Long organizationId,
									 @RequestParam(value = "category_name", required = false) String categoryName) {
		return categoryService.getOrganizationTags(organizationId, categoryName);
	}

	@GetMapping(value = "/tag", produces = MediaType.APPLICATION_JSON_VALUE)
	public TagsRepresentationObject getTags(@RequestParam(name = "tag_id") Long tagId) throws BusinessException {
		return categoryService.getTagById(tagId);
	}

	@GetMapping(value="/location_shops",produces=MediaType.APPLICATION_JSON_VALUE)
	public List<ShopRepresentationObject> getLocationShops(@RequestParam(name = "name", required = false) String name,
														   @RequestParam(name = "org_id") Long orgId,
														   @RequestParam(value = "area_id", required = false) Long areaId,
														   @RequestParam(value = "city_id", required = false) Long cityId,
														   @RequestParam(required = false) Double minLongitude,
														   @RequestParam(required = false) Double maxLongitude,
														   @RequestParam(required = false) Double minLatitude,
														   @RequestParam(required = false) Double maxLatitude,
														   @RequestParam(required = false) Double longitude,
														   @RequestParam(required = false) Double latitude,
														   @RequestParam(required = false) Double radius,
														   @RequestParam(required = false, defaultValue = "true") Boolean searchInTags,
														   @RequestParam(value = "product_type", required = false) Integer[] productType,
														   @RequestParam(value = "count", required = false, defaultValue = "999999") Long count) {
		LocationShopsParam param = new LocationShopsParam(name, orgId, areaId, cityId, minLongitude, minLatitude, maxLongitude, maxLatitude,
				longitude, latitude, radius, false, searchInTags, productType, count);
		return shopService.getLocationShops(param);
	}

	@GetMapping(value = "/location_shops_cities", produces = APPLICATION_JSON_VALUE)
	public Set<CityIdAndName> getLocationShopsCities(@RequestParam(value = "name", required = false) String name,
													 @RequestParam(name = "org_id", required = false) Long orgId,
													 @RequestParam(value = "area_id", required = false) Long areaId,
													 @RequestParam(value = "city_id", required = false) Long cityId,
													 @RequestParam(required = false) Double minLongitude,
													 @RequestParam(required = false) Double maxLongitude,
													 @RequestParam(required = false) Double minLatitude,
													 @RequestParam(required = false) Double maxLatitude,
													 @RequestParam(required = false) Double longitude,
													 @RequestParam(required = false) Double latitude,
													 @RequestParam(required = false) Double radius,
													 @RequestParam(required = false, defaultValue = "true") Boolean searchInTags,
													 @RequestParam(value = "product_type", required = false) Integer[] productType,
													 @RequestParam(value = "count", required = false, defaultValue = "999999") Long count) {
		LocationShopsParam param = new LocationShopsParam(name, orgId, areaId, cityId, minLongitude, minLatitude, maxLongitude, maxLatitude,
				longitude, latitude, radius, false, searchInTags.booleanValue(), productType, count);
		return shopService.getLocationShopsCities(param);
	}

	@GetMapping(value="/orgid",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getOrganizationByDomain(@RequestParam(name = "url") String url) {
		Pair domain = organizationService.getOrganizationAndSubdirsByUrl(url, 0);
		return new ResponseEntity<>("{\"id\":" + domain.getFirst() + ", \"sub_dir\":" + domain.getSecond() + "}", HttpStatus.OK);
	}

	@GetMapping(value="/countries", produces=MediaType.APPLICATION_JSON_VALUE)
	public Map<String, CountriesRepObj> getCountries(
			@RequestParam(value = "hide_empty_cities", required = false, defaultValue = "true") Boolean hideEmptyCities
			,@RequestParam(value = "org_id", required = false) Long orgId) {

		return addressService.getCountries(hideEmptyCities, orgId);
	}

	@GetMapping(value="/related_products", produces=MediaType.APPLICATION_JSON_VALUE)
	public List<ProductRepresentationObject> getRelatedProducts(@RequestParam("product_id") Long productId) {
		return productService.getRelatedProducts(productId);
	}

	@GetMapping(value = "organization/sitemap", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<?> getOrgSiteMap(@RequestHeader(name = "User-Token", required = false) String userToken,
										   SitemapParams params) throws IOException {
		return organizationService.getOrgSiteMap(userToken, params);
	}

	@GetMapping(value="/search", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<SearchResult> search(SearchParameters params) {
		return searchService.search(params, false);
	}

	@GetMapping(value="/seo", produces=MediaType.APPLICATION_JSON_VALUE)
	public List<SeoKeywordsDTO> getSeoKeywords(
			@RequestParam(value = "org_id")Long orgId,
			@RequestParam(value = "type", required = false)SeoEntityType type,
			@RequestParam(value = "id", required = false)Long entityId) {
		return seoService.getSeoKeywords(orgId, entityId, type);
	}

	@GetMapping(value="/review", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ProductRateRepresentationObject> getVariantRatings(@RequestParam(value = "variant_id", required = false)Long variantId,
																   @RequestParam(value = "product_id", required = false)Long productId) {
		if (productId != null)
			return reviewService.getProductRatings(productId);
		return reviewService.getVariantRatings(variantId);
	}

	@GetMapping(value = "/applicable_promotions_list", produces = APPLICATION_JSON_VALUE)
	public ItemsPromotionsDTO getPromotionsList(
			@RequestParam(value = "product_ids", required = false, defaultValue = "") Set<Long> productIds,
			@RequestParam(value = "brand_ids", required = false, defaultValue = "") Set<Long> brandIds,
			@RequestParam(value = "tag_ids", required = false, defaultValue = "") Set<Long> tagIds,
			@RequestParam(value = "promotions_per_item", required = false, defaultValue = "1") Long promotionsPerItem) {
		return promotionsService.getPromotionsListFromProductsAndBrandsAndTagsLists(
				productIds,
				brandIds,
				tagIds,
				promotionsPerItem);
	}
}