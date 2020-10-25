package com.nasnav.controller;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.nasnav.dto.*;
import com.nasnav.dto.response.navbox.VariantsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nasnav.exceptions.BusinessException;
import com.nasnav.request.ProductSearchParam;
import com.nasnav.service.AddressService;
import com.nasnav.service.BrandService;
import com.nasnav.service.CategoryService;
import com.nasnav.service.OrganizationService;
import com.nasnav.service.ProductService;
import com.nasnav.service.ShopService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/navbox")
@Api(description = "Methods for accessing public information about shops and products.")
public class NavboxController {

	@Autowired
	private BrandService brandService;

	@Autowired
	private ShopService shopService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private ProductService productService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private AddressService addressService;

	@ApiOperation(value = "Get information about brand by its ID", nickname = "brandInfo")
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 404, message = "No brand data for the supplied ID found"), })
	@GetMapping(value = "/brand", produces = APPLICATION_JSON_UTF8_VALUE)
	public Organization_BrandRepresentationObject getBrandById(@RequestParam(name = "brand_id") Long brandId) {
		return brandService.getBrandById(brandId);
	}

	
	
	
	
	@ResponseStatus(value = HttpStatus.OK)
	@GetMapping(value = "/organization", produces = APPLICATION_JSON_UTF8_VALUE)
	@ApiOperation(value = "Get organization's info by name", notes = "Searches organization by either org_id or p_name", response = OrganizationRepresentationObject.class)
	@ApiResponses({ @ApiResponse(code = 200, message = "Success", response = OrganizationRepresentationObject.class),
			@ApiResponse(code = 404, message = "Not found. No data for the supplied parameter", response = OrganizationRepresentationObject.class),
			@ApiResponse(code = 400, message = "Missing parameter. Either org_id or p_name is required", response = OrganizationRepresentationObject.class) })
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

	
	
	

	@ApiOperation(value = "Get selected organization's shops", nickname = "orgShops")
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 404, message = "There are no shops matching this org_id"), })
	@GetMapping("/shops")
	public List<ShopRepresentationObject> getShopsByOrganization(@RequestParam(name = "org_id") Long orgId) {
		return shopService.getOrganizationShops(orgId, false);
	}

	
	
	
	
	@ApiOperation(value = "Get specific shop's info", nickname = "shopInfo")
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 404, message = "No shop matching the supplied ID found"), })
	@GetMapping("/shop")
	public ShopRepresentationObject getShopById(@RequestParam(name = "shop_id") Long shopId) {

		return shopService.getShopById(shopId);
	}


	
	
	
	@ApiOperation(value = "Get list of products", nickname = "productList")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 204, message = "Empty product list"),
			@io.swagger.annotations.ApiResponse(code = 400, message = "Invalid query parameters"), })
	@GetMapping("/products")
	public ResponseEntity<?> getProducts(ProductSearchParam productSearchParam) throws BusinessException, InvocationTargetException, IllegalAccessException {

		ProductsResponse productsResponse = productService.getProducts(productSearchParam);

		if (productsResponse == null)
			return new ResponseEntity<>(NO_CONTENT);

		return new ResponseEntity<>(productsResponse, OK);
	}




	@ApiOperation(value = "Get products available filters", nickname = "productFilters")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 400, message = "Invalid query parameters"), })
	@GetMapping("/filters")
	public ProductsFiltersResponse getProductsFilters(ProductSearchParam productSearchParam) throws BusinessException, InvocationTargetException, IllegalAccessException, SQLException {

		ProductsFiltersResponse filtersResponse = productService.getProductAvailableFilters(productSearchParam);

		return filtersResponse;
	}

	
	
	@ApiOperation(value = "Get information about a specific product", nickname = "productInfo")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 404, message = "Product does not exist")
			})
	@GetMapping(value="/product",produces=APPLICATION_JSON_VALUE)
	public ProductDetailsDTO getProduct(@RequestParam(name = "product_id") Long productId,
										@RequestParam(name = "shop_id",required=false) Long shopId) throws BusinessException {

		return productService.getProduct(productId, shopId, true);
	}



	@ApiOperation(value = "get collection by id", nickname = "getCollection", code = 201)
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "collection returned"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
			@io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
	})
	@GetMapping(value = "collection", produces = APPLICATION_JSON_UTF8_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public ProductDetailsDTO getCollectionById(@RequestParam Long id) {
		return productService.getCollection(id);
	}


	@ApiOperation(value = "get variants by organization id", nickname = "getVariants", code = 201)
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "Variants returned"),
			@io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
			@io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
	})
	@GetMapping(value = "variants", produces = APPLICATION_JSON_UTF8_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public VariantsResponse getVariants(@RequestParam("org_id") Long orgId,
										@RequestParam(required = false, defaultValue = "") String name,
										@RequestParam(required = false, defaultValue = "0") Integer start,
										@RequestParam(required = false, defaultValue = "10") Integer count) {

		return productService.getVariants(orgId, name, start, count);
	}

	
	
	@ApiOperation(value = "Get information about a specific Organization's extra attributes", nickname = "organizationInfo")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 204, message = "Attributes does not exist")
	})
	@GetMapping(value="/attributes",produces=APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getOrganizationAttributes(@RequestParam(name = "org_id", required = false) Long organizationId) throws BusinessException {
		List<ExtraAttributesRepresentationObject> response = organizationService.getOrganizationExtraAttributesById(organizationId);
		return response.size() == 0 ? new ResponseEntity<>(NO_CONTENT) : new ResponseEntity<>(response, HttpStatus.OK);
	}

	
	
	
	
	@ApiOperation(value = "Get information about categories", nickname = "categories")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "invalid search parameter")
	})
	@GetMapping(value="/categories",produces=APPLICATION_JSON_VALUE)
	public List<CategoryRepresentationObject> getCategories(@RequestParam(name = "org_id", required = false) Long organizationId,
										   @RequestParam(name = "category_id", required = false) Long categoryId) throws BusinessException {
		return categoryService.getCategories(organizationId, categoryId);
	}

	
	
	

	@ApiOperation(value = "Get information about organization tags tree", nickname = "tagsTree")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "invalid search parameter")
	})
	@GetMapping(value="/tagstree",produces=MediaType.APPLICATION_JSON_VALUE)
	public List<TagsTreeNodeDTO> getTagsTree(@RequestParam(name = "org_id", required = false) Long organizationId) throws BusinessException {
		return categoryService.getOrganizationTagsTree(organizationId);
	}

	
	
	
	
	@ApiOperation(value = "Get information about all organiaztion tags", nickname = "tags")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "invalid search parameter")
	})
	@GetMapping(value="/tags",produces=MediaType.APPLICATION_JSON_VALUE)
	public List<TagsRepresentationObject> getTags(@RequestParam(name = "org_id") Long organizationId,
									 @RequestParam(value = "category_name", required = false) String categoryName) throws BusinessException {
		return categoryService.getOrganizationTags(organizationId, categoryName);
	}



	@ApiOperation(value = "Get organization tag by its id", nickname = "tag")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "invalid search parameter")
	})
	@GetMapping(value="/tag",produces=MediaType.APPLICATION_JSON_VALUE)
	public TagsRepresentationObject getTags(@RequestParam(name = "tag_id") Long tagId) throws BusinessException {
		return categoryService.getTagById(tagId);
	}
	

	@ApiOperation(value = "Get list of nearby shops by location", nickname = "locationShops")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "invalid search parameter")
	})
	@GetMapping(value="/location_shops",produces=MediaType.APPLICATION_JSON_VALUE)
	public List<ShopRepresentationObject> getLocationShops(@RequestParam(name = "org_id", required = false) Long orgId,
														   @RequestParam(name = "long", required = false) Double longitude,
														   @RequestParam(name = "lat", required = false) Double lattitude,
														   @RequestParam(name = "radius", required = false) Double radius,
														   @RequestParam(name = "name", required = false) String name) {
		return shopService.getLocationShops(orgId, name);
	}


	
	

	@ApiOperation(value = "Identify Organization by its domain", nickname = "orgId")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "invalid search parameter")
	})
	@GetMapping(value="/orgid",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getOrganizationByDomain(@RequestParam(name = "url") String url) throws BusinessException {

		Pair domain = organizationService.getOrganizationAndSubdirsByUrl(url);

		return new ResponseEntity<>("{\"id\":" + domain.getFirst() + ", \"sub_dir\":" + domain.getSecond() + "}", HttpStatus.OK);
	}



	@ApiOperation(value = "Get all countries, cities and areas", nickname = "countires")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK")
	})
	@GetMapping(value="/countries", produces=MediaType.APPLICATION_JSON_VALUE)
	public Map<String, CountriesRepObj> getCountries(
			@RequestParam(value = "hide_empty_cities", required = false, defaultValue = "false") Boolean hideEmptyCities) {

		return addressService.getCountries(hideEmptyCities);
	}


	@ApiOperation(value = "Get related products", nickname = "relatedProducts")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK")
	})
	@GetMapping(value="/related_products", produces=MediaType.APPLICATION_JSON_VALUE)
	public List<ProductRepresentationObject> getRelatedProducts(@RequestParam("product_id") Long productId) {
		return productService.getRelatedProducts(productId);
	}




	@ApiOperation(value = "Get organization domain", nickname = "getOrgDomain", code = 201)
	@ApiResponses(value = {@io.swagger.annotations.ApiResponse(code = 200, message = "OK")})
	@GetMapping(value = "organization/sitemap", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> getOrgSiteMap(@RequestParam String url,
									  @RequestParam(value = "include_products", required = false) boolean includeProducts,
									  @RequestParam(value = "include_collections", required = false) boolean includeCollections,
									  @RequestParam(value = "include_brands", required = false) boolean includeBrands,
									  @RequestParam(value = "include_tags", required = false) boolean includeTags,
									  @RequestParam(value = "include_tags_tree", required = false) boolean includeTagsTree) throws IOException {
		ByteArrayOutputStream s =  organizationService.getOrgSiteMap(url, includeProducts, includeCollections, includeBrands,
																	 includeTags, includeTagsTree);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("text/plain"))
				.header(CONTENT_DISPOSITION, "attachment; filename=sitemap.txt")
				.body(s.toString());
	}
}