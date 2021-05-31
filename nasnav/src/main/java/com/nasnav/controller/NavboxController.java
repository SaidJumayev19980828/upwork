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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/navbox")
@Tag(name = "Methods for accessing public information about shops and products.")
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

	@Operation(description =  "Get information about brand by its ID", summary = "brandInfo")
	@ApiResponses(value = { @ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 404" ,description = "No brand data for the supplied ID found"), })
	@GetMapping(value = "/brand", produces = APPLICATION_JSON_UTF8_VALUE)
	public Organization_BrandRepresentationObject getBrandById(@RequestParam(name = "brand_id") Long brandId) {
		return brandService.getBrandById(brandId);
	}

	
	
	
	
	@ResponseStatus(value = HttpStatus.OK)
	@GetMapping(value = "/organization", produces = APPLICATION_JSON_UTF8_VALUE)
	@Operation(summary =  "Get organization's info by name", description = "Searches organization by either org_id or p_name")
	@ApiResponses({ @ApiResponse(responseCode ="200" ,description = "Success"),
			@ApiResponse(responseCode ="404" ,description = "Not found. No data for the supplied parameter"),
			@ApiResponse(responseCode ="400" ,description = "Missing parameter. Either org_id or p_name is required") })
	public @ResponseBody OrganizationRepresentationObject getOrganizationByName(
			@RequestParam(name = "p_name", required = false) String organizationName,
			@RequestParam(name = "org_id", required = false) Long organizationId,
			@RequestParam(name = "url", required = false) String url) throws BusinessException {

		if (organizationName == null && organizationId == null && url == null)
			throw new BusinessException("Provide org_id or p_name or url request params", "", BAD_REQUEST);

		if (organizationName != null)
			return organizationService.getOrganizationByName(organizationName);

		if (url != null) {
			Pair domain = organizationService.getOrganizationAndSubdirsByUrl(url);
			OrganizationRepresentationObject orgObj = organizationService.getOrganizationById(domain.getFirst());
			orgObj.setSubDir(domain.getSecond());
			return orgObj;
		}
		return organizationService.getOrganizationById(organizationId);
	}

	
	
	

	@Operation(description =  "Get selected organization's shops", summary = "orgShops")
	@ApiResponses(value = { @ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 404" ,description = "There are no shops matching this org_id"), })
	@GetMapping("/shops")
	public List<ShopRepresentationObject> getShopsByOrganization(@RequestParam(name = "org_id") Long orgId) {
		return shopService.getOrganizationShops(orgId, false);
	}

	
	
	
	
	@Operation(description =  "Get specific shop's info", summary = "shopInfo")
	@ApiResponses(value = { @ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 404" ,description = "No shop matching the supplied ID found"), })
	@GetMapping("/shop")
	public ShopRepresentationObject getShopById(@RequestParam(name = "shop_id") Long shopId) {

		return shopService.getShopById(shopId);
	}


	
	
	
	@Operation(description =  "Get list of products", summary = "productList")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 204" ,description = "Empty product list"),
			@ApiResponse(responseCode = " 400" ,description = "Invalid query parameters"), })
	@GetMapping("/products")
	public ResponseEntity<?> getProducts(ProductSearchParam productSearchParam) throws BusinessException, InvocationTargetException, IllegalAccessException {

		ProductsResponse productsResponse = productService.getProducts(productSearchParam);

		if (productsResponse == null)
			return new ResponseEntity<>(NO_CONTENT);

		return new ResponseEntity<>(productsResponse, OK);
	}




	@Operation(description =  "Get products available filters", summary = "productFilters")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 400" ,description = "Invalid query parameters"), })
	@GetMapping("/filters")
	public ProductsFiltersResponse getProductsFilters(ProductSearchParam productSearchParam) throws BusinessException, InvocationTargetException, IllegalAccessException, SQLException {

		ProductsFiltersResponse filtersResponse = productService.getProductAvailableFilters(productSearchParam);

		return filtersResponse;
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
		params.setOnlyYeshteryProducts(false);
		return productService.getProduct(params);
	}



	@Operation(description =  "get collection by id", summary = "getCollection")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "collection returned"),
			@ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
			@ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
	})
	@GetMapping(value = "collection", produces = APPLICATION_JSON_UTF8_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public ProductDetailsDTO getCollectionById(@RequestParam Long id) {
		return productService.getCollection(id);
	}


	@Operation(description =  "get variants by organization id", summary = "getVariants")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "Variants returned"),
			@ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
			@ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
	})
	@GetMapping(value = "variants", produces = APPLICATION_JSON_UTF8_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public VariantsResponse getVariants(@RequestParam("org_id") Long orgId,
										@RequestParam(required = false, defaultValue = "") String name,
										@RequestParam(required = false, defaultValue = "0") Integer start,
										@RequestParam(required = false, defaultValue = "10") Integer count) {

		return productService.getVariants(orgId, name, start, count);
	}

	
	
	@Operation(description =  "Get information about a specific Organization's extra attributes", summary = "organizationInfo")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 204" ,description = "Attributes does not exist")
	})
	@GetMapping(value="/attributes",produces=APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getOrganizationAttributes(@RequestParam(name = "org_id", required = false) Long organizationId) throws BusinessException {
		List<ExtraAttributesRepresentationObject> response = organizationService.getOrganizationExtraAttributesById(organizationId);
		return response.size() == 0 ? new ResponseEntity<>(NO_CONTENT) : new ResponseEntity<>(response, HttpStatus.OK);
	}

	
	
	
	
	@Operation(description =  "Get information about categories", summary = "categories")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 406" ,description = "invalid search parameter")
	})
	@GetMapping(value="/categories",produces=APPLICATION_JSON_VALUE)
	public List<CategoryRepresentationObject> getCategories(@RequestParam(name = "org_id", required = false) Long organizationId,
										   @RequestParam(name = "category_id", required = false) Long categoryId) throws BusinessException {
		return categoryService.getCategories(organizationId, categoryId);
	}

	
	
	

	@Operation(description =  "Get information about organization tags tree", summary = "tagsTree")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 406" ,description = "invalid search parameter")
	})
	@GetMapping(value="/tagstree",produces=MediaType.APPLICATION_JSON_VALUE)
	public List<TagsTreeNodeDTO> getTagsTree(@RequestParam(name = "org_id", required = false) Long organizationId) throws BusinessException {
		return categoryService.getOrganizationTagsTree(organizationId);
	}

	
	
	
	
	@Operation(description =  "Get information about all organiaztion tags", summary = "tags")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 406" ,description = "invalid search parameter")
	})
	@GetMapping(value="/tags",produces=MediaType.APPLICATION_JSON_VALUE)
	public List<TagsRepresentationObject> getTags(@RequestParam(name = "org_id") Long organizationId,
									 @RequestParam(value = "category_name", required = false) String categoryName) throws BusinessException {
		return categoryService.getOrganizationTags(organizationId, categoryName);
	}



	@Operation(description =  "Get organization tag by its id", summary = "tag")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 406" ,description = "invalid search parameter")
	})
	@GetMapping(value="/tag",produces=MediaType.APPLICATION_JSON_VALUE)
	public TagsRepresentationObject getTags(@RequestParam(name = "tag_id") Long tagId) throws BusinessException {
		return categoryService.getTagById(tagId);
	}
	

	@Operation(description =  "Get list of nearby shops by location", summary = "locationShops")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 406" ,description = "invalid search parameter")
	})
	@GetMapping(value="/location_shops",produces=MediaType.APPLICATION_JSON_VALUE)
	public List<ShopRepresentationObject> getLocationShops(@RequestParam(name = "name", required = false) String name,
														   @RequestParam(name = "org_id") Long orgId,
														   @RequestParam(value = "area_id", required = false) Long areaId,
														   @RequestParam(required = false) Double longitude,
														   @RequestParam(required = false) Double latitude,
														   @RequestParam(required = false) Double radius,
														   @RequestParam(required = false, defaultValue = "true") Boolean searchInTags,
														   @RequestParam(value = "product_type", required = false) Integer[] productType) {
		LocationShopsParam param = new LocationShopsParam(name, orgId, areaId, longitude, latitude, radius, false, searchInTags.booleanValue(), productType);
		return shopService.getLocationShops(param);
	}


	
	

	@Operation(description =  "Identify Organization by its domain", summary = "orgId")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK"),
			@ApiResponse(responseCode = " 406" ,description = "invalid search parameter")
	})
	@GetMapping(value="/orgid",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getOrganizationByDomain(@RequestParam(name = "url") String url) throws BusinessException {

		Pair domain = organizationService.getOrganizationAndSubdirsByUrl(url);

		return new ResponseEntity<>("{\"id\":" + domain.getFirst() + ", \"sub_dir\":" + domain.getSecond() + "}", HttpStatus.OK);
	}



	@Operation(description =  "Get all countries, cities and areas", summary = "countires")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK")
	})
	@GetMapping(value="/countries", produces=MediaType.APPLICATION_JSON_VALUE)
	public Map<String, CountriesRepObj> getCountries(
			@RequestParam(value = "hide_empty_cities", required = false, defaultValue = "true") Boolean hideEmptyCities
			,@RequestParam(value = "org_id", required = false) Long orgId) {

		return addressService.getCountries(hideEmptyCities, orgId);
	}


	@Operation(description =  "Get related products", summary = "relatedProducts")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK")
	})
	@GetMapping(value="/related_products", produces=MediaType.APPLICATION_JSON_VALUE)
	public List<ProductRepresentationObject> getRelatedProducts(@RequestParam("product_id") Long productId) {
		return productService.getRelatedProducts(productId);
	}




	@Operation(description =  "Get organization sitemap", summary = "getOrgSitemap")
	@ApiResponses(value = {@ApiResponse(responseCode = " 200" ,description = "OK")})
	@GetMapping(value = "organization/sitemap", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<?> getOrgSiteMap(
			@RequestHeader(name = "User-Token", required = false) String userToken, SitemapParams params) throws IOException {
		return organizationService.getOrgSiteMap(userToken, params);
	}




	@Operation(description =  "search the data", summary = "search")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK")
	})
	@GetMapping(value="/search", produces=MediaType.APPLICATION_JSON_VALUE)
	public Mono<SearchResult> search(SearchParameters params) {
		return searchService.search(params, false);
	}




	@Operation(description =  "return seo keywords", summary = "getSeo")
	@ApiResponses(value = {
			@ApiResponse(responseCode = " 200" ,description = "OK")
	})
	@GetMapping(value="/seo", produces=MediaType.APPLICATION_JSON_VALUE)
	public List<SeoKeywordsDTO> getSeoKeywords(
			@RequestParam(value = "org_id", required = true)Long orgId,
			@RequestParam(value = "type", required = false)SeoEntityType type,
			@RequestParam(value = "id", required = false)Long entityId) {
		return seoService.getSeoKeywords(orgId, entityId, type);
	}


	@GetMapping(value="/review", produces=MediaType.APPLICATION_JSON_VALUE)
	public List<ProductRateRepresentationObject> getVariantRatings(@RequestParam(value = "variant_id")Long variantId) {
		return reviewService.getProductRatings(variantId);
	}
}