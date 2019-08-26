package com.nasnav.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.exceptions.BusinessException;
import com.nasnav.request.BundleSearchParam;
import com.nasnav.response.BundleResponse;
import com.nasnav.service.ProductService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/product")
@Api(description = "Products api")
public class ProductsController {
	

	@Autowired
	ProductService productService;
	
	
	@ApiOperation(value = "get bundles", nickname = "GetBundles", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Bundles fetched"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @GetMapping(value = "bundles", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BundleResponse getBundles( BundleSearchParam params)
            		throws BusinessException {
		return productService.getBundles(params);
    }
}
