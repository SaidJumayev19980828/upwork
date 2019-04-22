package com.nasnav.controller;

import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.dto.ProductSortOptions;
import com.nasnav.dto.ProductsResponse;
import com.nasnav.service.OrganizationService;
import com.nasnav.service.ProductService;
import com.nasnav.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nasnav.dto.Organization_BrandRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.BrandService;

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

	@ApiOperation(value = "Get information about brand by its ID", nickname = "brandInfo")
	@ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
			@io.swagger.annotations.ApiResponse(code = 404, message = "No brand data for the supplied ID found"), })
	@GetMapping(value = "/brand")
	public ResponseEntity<?> getBrandById(@RequestParam(name = "brand_id") Long brandId) throws BusinessException {

		Organization_BrandRepresentationObject brandRepresentationObject = brandService.getBrandById(brandId);

		if (brandRepresentationObject == null) {
			throw new BusinessException("Brand not found", null, HttpStatus.NOT_FOUND);
		}
		JSONObject response = new JSONObject();
		response.put("name", brandRepresentationObject.getName());
		response.put("p_name", brandRepresentationObject.getPname());
		response.put("logo", brandRepresentationObject.getLogoUrl());
		response.put("banner", brandRepresentationObject.getBannerImage());

		return new ResponseEntity<>(response.toString(), HttpStatus.OK);
	}

	@ResponseStatus(value = HttpStatus.OK)
	@GetMapping(value = "/organization", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ApiOperation(value = "Get organization's info by name", notes = "Searches organization by either org_id or p_name", response = OrganizationRepresentationObject.class)
	@ApiResponses({ @ApiResponse(code = 200, message = "Success", response = OrganizationRepresentationObject.class),
			@ApiResponse(code = 404, message = "Not found. No data for the supplied parameter", response = OrganizationRepresentationObject.class),
			@ApiResponse(code = 400, message = "Missing parameter. Either org_id or p_name is required", response = OrganizationRepresentationObject.class) })
	public @ResponseBody OrganizationRepresentationObject getOrganizationByName(
			@RequestParam(name = "p_name", required = false) String organizationName,
			@RequestParam(name = "org_id", required = false) Long organizationId) throws BusinessException {

		if (organizationName == null && organizationId == null)
			throw new BusinessException("Provide org_id or p_name request params", null, HttpStatus.BAD_REQUEST);

		if (organizationName != null)
			return organizationService.getOrganizationByName(organizationName);

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

	@GetMapping("/products")
	public ResponseEntity<?> getProducts(@RequestParam(name = "org_id", required = false) Long organizationId,
			@RequestParam(name = "shop_id", required = false) Long shopId,
			@RequestParam(name = "category_id", required = false) Long categoryId,
			@RequestParam(name = "start", required = false) Integer start,
			@RequestParam(name = "count", required = false) Integer count,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "order", required = false) String order) throws BusinessException {

		if (sort != null && ProductSortOptions.getProductSortOptions(sort) == null)
			throw new BusinessException("Sort is limited to id, name, pname, price", null, HttpStatus.BAD_REQUEST);

		if (order != null && !order.equals("asc") && !order.equals("desc"))
			throw new BusinessException("Order is limited to asc and desc only", null, HttpStatus.BAD_REQUEST);

		if (start != null && start < 0)
			throw new BusinessException("Start can be zero or more", null, HttpStatus.BAD_REQUEST);

		if (count != null && count < 1)
			throw new BusinessException("Start can be One or more", null, HttpStatus.BAD_REQUEST);

		ProductsResponse productsResponse = null;
		if (organizationId != null) {

			productsResponse = productService.getProductsResponseByOrganizationId(organizationId, categoryId, start, count, sort,
					order);

		} else if (shopId != null) {

			productsResponse = productService.getProductsResponseByShopId(shopId, categoryId, start, count,
					sort, order);
		} else {
			throw new BusinessException("Shop Id or Organization Id shall be provided", null, HttpStatus.BAD_REQUEST);
		}

		if (productsResponse == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<>(productsResponse, HttpStatus.OK);
	}

}
