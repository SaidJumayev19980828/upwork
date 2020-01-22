package com.nasnav.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.BundleElementUpdateDTO;
import com.nasnav.dto.ProductImageBulkUpdateDTO;
import com.nasnav.dto.ProductImageUpdateDTO;
import com.nasnav.dto.ProductImgDetailsDTO;
import com.nasnav.dto.VariantUpdateDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.request.BundleSearchParam;
import com.nasnav.response.BundleResponse;
import com.nasnav.response.ProductImageDeleteResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.response.ProductUpdateResponse;
import com.nasnav.response.VariantUpdateResponse;
import com.nasnav.service.CsvDataImportService;
import com.nasnav.service.ProductImageService;
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
	
	@Autowired
	ProductImageService productImgService;

    @Autowired
    CsvDataImportService csvDataImportService;
	
	@ApiOperation(value = "Create or update a product", nickname = "product update", code = 201)
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
		return productService.updateProduct(productJson, false);
    }
	
	
	
	
	@ApiOperation(value = "deletes a product", nickname = "product delete", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Product Deleted"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @DeleteMapping(
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

		return  productImgService.updateProductImage(file, imgMetaData);
    }
    
    
    

    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Product images fetched"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
	@ResponseStatus(HttpStatus.OK)
	@GetMapping(value = "images",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<ProductImgDetailsDTO> getProductImages(
    		@RequestHeader (value = "User-Token") String userToken, @RequestParam("product_id") Long productId)
            throws BusinessException {

		return  productImgService.getProductImgs(productId);
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
		return  productImgService.deleteImage(imageId);
    }
	
	
	
	
	
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
	
	
	
	
	@ApiOperation(value = "Create or update a bundle", nickname = "bundle update", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Bundle created or updated"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @PostMapping(value = "bundle",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ProductUpdateResponse updateBundle(           
            @RequestBody String productJson)
            		throws BusinessException {
		return productService.updateProduct(productJson, true);
    }
	
	
	
	
	@ApiOperation(value = "deletes a bundle", nickname = "bundle delete", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Bundle Deleted"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @DeleteMapping(value = "bundle",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ProductUpdateResponse deleteBundle(           
            @RequestParam("product_id") Long productId)
            		throws BusinessException {
		return productService.deleteBundle(productId);
    }
	
	
	
	
	@ApiOperation(value = "Add or delete a bundle item", nickname = "bundle item update", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Bundle item added or deleted"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @PostMapping(value = "bundle/element",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseStatus(HttpStatus.OK)
    public void updateBundleElement(           
            @RequestBody BundleElementUpdateDTO element)
            		throws BusinessException {
		productService.updateBundleElement(element);
    }
	
	
	
	
	
	@ApiOperation(value = "update product variant", nickname = "product variant save", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Product variant saved"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
	@ResponseStatus(HttpStatus.OK)
	@PostMapping(value = "variant",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
            ,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public VariantUpdateResponse updateProductVariant(@RequestBody VariantUpdateDTO variant)
            		throws BusinessException {
		return  productService.updateVariant(variant);
    }
	
	
	
	
	
	
	@ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Images imported successfully"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
	@ResponseStatus(HttpStatus.OK)
	@PostMapping(value = "image/bulk",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<ProductImageUpdateResponse> importProductImagesBulk(
            @RequestPart("imgs_zip") @Valid MultipartFile zip,
            @RequestPart(name="imgs_barcode_csv", required=false )  MultipartFile csv,
            @RequestPart("properties") @Valid ProductImageBulkUpdateDTO metaData)
            		throws BusinessException {

		return  productImgService.updateProductImageBulk(zip, csv, metaData);
    }


    @ApiOperation(value = "Link list of products to list of tags", nickname = "productsTagsLinking", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Product tag created successfully"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "tag", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity updateProductTags(@RequestHeader("User-Token") String token,
                                            @RequestBody ProductTagDTO productTagDTO ) throws BusinessException {
        productService.updateProductTags(productTagDTO);
        return new ResponseEntity("{\"Message\":\"Linking products with tags created successfully\"}".toString(), HttpStatus.OK);
    }

    @ApiOperation(value = "Remove link between list of products to list and tags", nickname = "removeProductsTagsLinking", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Product tag Removed successfully"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = "tag", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity deleteProductTags(@RequestHeader("User-Token") String token,
                                            @RequestBody ProductTagDTO productTagDTO ) throws BusinessException {
        productService.deleteProductTags(productTagDTO);
        return new ResponseEntity("{\"Message\":\"Remove Linking between products and tags completed successfully\"}".toString(), HttpStatus.OK);
    }





    @GetMapping(value = "/image/bulk/template")
    @ResponseBody
    public ResponseEntity<String> generateCsvTemplate(@RequestHeader("User-Token") String token) throws IOException {
        ByteArrayOutputStream s = csvDataImportService.generateImagesCsvTemplate();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Csv_Template.csv")
                .body(s.toString());
    }
}
