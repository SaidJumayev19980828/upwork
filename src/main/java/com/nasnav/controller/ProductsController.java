package com.nasnav.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.ProductImageUpdateDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.ProductImageDeleteResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.response.ProductUpdateResponse;
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
	
	
	@ApiOperation(value = "Create or update a prodcut", nickname = "product update", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Product created or updated"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @PostMapping(value = "info",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ProductUpdateResponse updateProduct(           
            @RequestBody String productJson)
            		throws BusinessException {
		return productService.updateProduct(productJson);
    }
	
	
	
	
	@ApiOperation(value = "deletes a prodcut", nickname = "product delete", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Product Deleted"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @DeleteMapping(value = "info",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ProductUpdateResponse deleteProduct(           
            @RequestParam("product_id") Long productId)
            		throws BusinessException {
		return productService.deleteProduct(productId);
    }
	
	
	
	
	
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Product image created or updated"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
	@ResponseStatus(HttpStatus.OK)
	@PostMapping(value = "image",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductImageUpdateResponse updateProductImage(
            @RequestPart("image") @Valid MultipartFile file,
            @RequestPart("properties") @Valid ProductImageUpdateDTO imgMetaData)
            		throws BusinessException {

		return  productService.updateProductImage(file, imgMetaData);
    }
	
	
	@ApiOperation(value = "delete image for product", nickname = "product image delete", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Product image deleted"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
	@ResponseStatus(HttpStatus.OK)
	@DeleteMapping(value = "image",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ProductImageDeleteResponse deleteProductImage(@RequestParam("image_id") @Valid Long imageId)
            		throws BusinessException {
		return  productService.deleteImage(imageId);
    }
	
}
