package com.nasnav.controller;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

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

import com.nasnav.dto.CategoryRepresentationObject;
import com.nasnav.dto.ExtraAttributesRepresentationObject;
import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.dto.Organization_BrandRepresentationObject;
import com.nasnav.dto.Pair;
import com.nasnav.dto.ProductDetailsDTO;
import com.nasnav.dto.ProductsFiltersResponse;
import com.nasnav.dto.ProductsResponse;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.dto.TagsRepresentationObject;
import com.nasnav.dto.TagsTreeNodeDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.request.ProductSearchParam;
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

	@ApiOperation(value = "Get information about brand by its ID", nickname = "brandInfo")
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 404, message = "No brand data for the supplied ID found"), })
	@GetMapping(value = "/brand")
	public Organization_BrandRepresentationObject getBrandById(@RequestParam(name = "brand_id") Long brandId) throws BusinessException {

		Organization_BrandRepresentationObject brandRepresentationObject = brandService.getBrandById(brandId);

		if (brandRepresentationObject == null)
			throw new BusinessException("Brand not found", "", NOT_FOUND);

		return brandRepresentationObject;
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
	public List<ShopRepresentationObject> getShopsByOrganization(@RequestParam(name = "org_id") Long orgId)
			throws BusinessException {
		return shopService.getOrganizationShops(orgId);
	}

	
	
	
	
	@ApiOperation(value = "Get specific shop's info", nickname = "shopInfo")
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 404, message = "No shop matching the supplied ID found"), })
	@GetMapping("/shop")
	public ShopRepresentationObject getShopById(@RequestParam(name = "shop_id") Long shopId) throws BusinessException {

		return shopService.getShopById(shopId);
	}


	
	
	
	@ApiOperation(value = "Get list of products", nickname = "productList")
	@ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 204, message = "Empty product list"),
			@io.swagger.annotations.ApiResponse(code = 400, message = "Invalid query parameters"), })
	@GetMapping("/products")
	public ResponseEntity<?> getProducts(ProductSearchParam productSearchParam) throws BusinessException, InvocationTargetException, IllegalAccessException, SQLException {

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

		return productService.getProduct(productId, shopId);
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

		Pair domain = organizationService.getOrganizationAndSubdirsByUrl(url);

		return new ResponseEntity<>("{\"id\":" + domain.getFirst() + ", \"sub_dir\":" + domain.getSecond() + "}", HttpStatus.OK);
	}
}