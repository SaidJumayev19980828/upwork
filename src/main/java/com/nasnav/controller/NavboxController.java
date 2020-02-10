package com.nasnav.controller;

import com.nasnav.dto.*;
import com.nasnav.request.ProductSearchParam;
import com.nasnav.service.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.exceptions.BusinessException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

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

	@ApiOperation(value = "Get information about brand by its ID", nickname = "brandInfo")
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 404, message = "No brand data for the supplied ID found"), })
	@GetMapping(value = "/brand")
	public ResponseEntity<?> getBrandById(@RequestParam(name = "brand_id") Long brandId) throws BusinessException {

		Organization_BrandRepresentationObject brandRepresentationObject = brandService.getBrandById(brandId);

		if (brandRepresentationObject == null)
			throw new BusinessException("Brand not found", null, HttpStatus.NOT_FOUND);

		return new ResponseEntity<>(brandRepresentationObject, HttpStatus.OK);
	}

	@ResponseStatus(value = HttpStatus.OK)
	@GetMapping(value = "/organization", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ApiOperation(value = "Get organization's info by name", notes = "Searches organization by either org_id or p_name", response = OrganizationRepresentationObject.class)
	@ApiResponses({ @ApiResponse(code = 200, message = "Success", response = OrganizationRepresentationObject.class),
			@ApiResponse(code = 404, message = "Not found. No data for the supplied parameter", response = OrganizationRepresentationObject.class),
			@ApiResponse(code = 400, message = "Missing parameter. Either org_id or p_name is required", response = OrganizationRepresentationObject.class) })
	public @ResponseBody OrganizationRepresentationObject getOrganizationByName(
			@RequestParam(name = "p_name", required = false) String organizationName,
			@RequestParam(name = "org_id", required = false) Long organizationId,
			@RequestParam(name = "url", required = false) String url) throws BusinessException {

		if (organizationName == null && organizationId == null && url == null)
			throw new BusinessException("Provide org_id or p_name or url request params", null, HttpStatus.BAD_REQUEST);

		if (organizationName != null)
			return organizationService.getOrganizationByName(organizationName);

		if (url != null) {
			Long orgId = organizationService.getOrganizationByDomain(url);
			return organizationService.getOrganizationById(orgId);
		}
		return organizationService.getOrganizationById(organizationId);
	}


	@ApiOperation(value = "Get selected organization's shops", nickname = "orgShops")
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 404, message = "There are no shops matching this org_id"), })
	@GetMapping("/shops")
	public ResponseEntity<?> getShopsByOrganization(@RequestParam(name = "org_id") Long orgId)
			throws BusinessException {

		return new ResponseEntity<>(shopService.getOrganizationShops(orgId), HttpStatus.OK);
	}

	@ApiOperation(value = "Get specific shop's info", nickname = "shopInfo")
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 404, message = "No shop matching the supplied ID found"), })
	@GetMapping("/shop")
	public ResponseEntity<?> getShopById(@RequestParam(name = "shop_id") Long shopId) throws BusinessException {

		return new ResponseEntity<>(shopService.getShopById(shopId), HttpStatus.OK);
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
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);

		return new ResponseEntity<>(productsResponse, HttpStatus.OK);
	}


	@ApiOperation(value = "Get information about a specific product", nickname = "productInfo")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 404, message = "Product does not exist")
			})
	@GetMapping(value="/product",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getProduct(@RequestParam(name = "product_id") Long productId,
										@RequestParam(name = "shop_id",required=false) Long shopId) throws BusinessException {

		ProductDetailsDTO response = productService.getProduct(productId, shopId);
		return response == null ? new ResponseEntity<>(HttpStatus.NOT_FOUND) : new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ApiOperation(value = "Get information about a specific Organization's extra attributes", nickname = "organizationInfo")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 204, message = "Attributes does not exist")
	})
	@GetMapping(value="/attributes",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getOrganizationAttributes(@RequestParam(name = "org_id", required = false) Long organizationId) throws BusinessException {
		List<ExtraAttributesRepresentationObject> response = organizationService.getOrganizationExtraAttributesById(organizationId);
		return response.size() == 0 ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ApiOperation(value = "Get information about categories", nickname = "categories")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "invalid search parameter")
	})
	@GetMapping(value="/categories",produces=MediaType.APPLICATION_JSON_VALUE)
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
	public ResponseEntity<?> getTagsTree(@RequestParam(name = "org_id", required = false) Long organizationId) throws BusinessException {
		List<TagsRepresentationObject> response = categoryService.getOrganizationTagsTree(organizationId);
		return response.isEmpty() ? new ResponseEntity<>(response, HttpStatus.NO_CONTENT): new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ApiOperation(value = "Get information about all organiaztion tags", nickname = "tags")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "invalid search parameter")
	})
	@GetMapping(value="/tags",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getTags(@RequestParam(name = "org_id", required = false) Long organizationId) throws BusinessException {
		List<TagsRepresentationObject> response = categoryService.getOrganizationTags(organizationId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}



	@ApiOperation(value = "Get list of nearby shops by location", nickname = "locationShops")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "invalid search parameter")
	})
	@GetMapping(value="/location_shops",produces=MediaType.APPLICATION_JSON_VALUE)
	public List<ShopRepresentationObject> getLocationShops(@RequestParam(name = "org_id", required = false) Long organizationId,
											  @RequestParam(name = "long", required = false) Double longitude,
											  @RequestParam(name = "lat", required = false) Double lattitude,
											  @RequestParam(name = "radius", required = false) Double radius,
											  @RequestParam(name = "name", required = false) String name) throws BusinessException {
		return shopService.getLocationShops(organizationId, longitude, lattitude, radius, name);
	}



	@ApiOperation(value = "Identify Organization by its domain", nickname = "orgId")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 406, message = "invalid search parameter")
	})
	@GetMapping(value="/orgid",produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getOrganizationByDomain(@RequestParam(name = "url") String url) throws BusinessException {

		Long orgId = organizationService.getOrganizationByDomain(url);

		return new ResponseEntity<>("{\"id\":"+orgId+"}", HttpStatus.OK);
	}
}