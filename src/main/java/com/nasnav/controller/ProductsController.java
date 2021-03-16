package com.nasnav.controller;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.validation.Valid;

import com.nasnav.dto.*;
import com.nasnav.dto.request.product.CollectionItemDTO;
import com.nasnav.dto.request.product.ProductRateDTO;
import com.nasnav.dto.request.product.RelatedItemsDTO;
import com.nasnav.dto.response.navbox.ProductRateRepresentationObject;
import com.nasnav.service.ReviewServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.nasnav.enumerations.ImageCsvTemplateType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.request.BundleSearchParam;
import com.nasnav.response.BundleResponse;
import com.nasnav.response.ProductImageDeleteResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.response.ProductUpdateResponse;
import com.nasnav.response.ProductsDeleteResponse;
import com.nasnav.response.VariantUpdateResponse;
import com.nasnav.service.CsvDataExportService;
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
	private ProductService productService;
	
	@Autowired
	private ProductImageService productImgService;

    @Autowired
    private CsvDataExportService csvDataExportService;

    @Autowired
    private ReviewServiceImpl reviewService;
	
	@ApiOperation(value = "Create or update a product", nickname = "product update", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Product created or updated"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @PostMapping(value = "info",
            produces = APPLICATION_JSON_UTF8_VALUE,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public ProductUpdateResponse updateProduct(@RequestHeader(name = "User-Token", required = false) String token,
                                               @RequestBody String productJson) {
		return productService.updateProduct(productJson, false, false);
    }
	
	
	
	
	@ApiOperation(value = "deletes list of products", nickname = "product delete", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Product Deleted"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @DeleteMapping(
            produces = APPLICATION_JSON_UTF8_VALUE)
    public ProductsDeleteResponse deleteProduct(@RequestHeader(name = "User-Token", required = false) String token,
                                                @RequestParam("product_id") List<Long> productIds,
                                                @RequestParam(value = "force_delete_collection_items", required = false, defaultValue = "false") Boolean forceDeleteCollectionItems){
		return productService.deleteProducts(productIds, forceDeleteCollectionItems);
    }
	
	
	
	
	
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Product image created or updated"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
	@ResponseStatus(HttpStatus.OK)
	@PostMapping(value = "image",
            produces = APPLICATION_JSON_UTF8_VALUE,
            consumes = MULTIPART_FORM_DATA_VALUE)
    public ProductImageUpdateResponse updateProductImage(
            @RequestHeader(name = "User-Token", required = false) String token,
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
            produces = APPLICATION_JSON_UTF8_VALUE)
    public List<ProductImgDetailsDTO> getProductImages(
    		@RequestHeader (name = "User-Token", required = false) String userToken, @RequestParam("product_id") Long productId)
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
            produces = APPLICATION_JSON_UTF8_VALUE)
    public ProductImageDeleteResponse deleteProductImage(@RequestHeader(name = "User-Token", required = false) String token,
    		                                             @RequestParam(value = "image_id", required = false) @Valid Long imageId,
                                                         @RequestParam(name = "productId", required = false) Long productId)
            		throws BusinessException {
		return  productImgService.deleteImage(imageId, productId);
    }
	
	
	
	

	@ApiOperation(value = "delete image for product", nickname = "product image delete", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Product image deleted"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
	@ResponseStatus(OK)
	@DeleteMapping(value = "image/all")
    public void deleteAllProductImages(@RequestHeader(name = "User-Token", required = false) String token
            , @RequestParam(name = "confirmed",defaultValue = "false", required = true) boolean isConfirmed)
            throws BusinessException {
        productImgService.deleteAllImages(isConfirmed);
    }
	
	
	
	
	
	@ApiOperation(value = "get bundles", nickname = "GetBundles", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Bundles fetched"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @GetMapping(value = "bundles", produces = APPLICATION_JSON_UTF8_VALUE)
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
            produces = APPLICATION_JSON_UTF8_VALUE,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public ProductUpdateResponse updateBundle(
            @RequestHeader(name = "User-Token", required = false) String token,
            @RequestBody String productJson) {
		return productService.updateProduct(productJson, true, false);
    }
	
	
	
	
	@ApiOperation(value = "deletes a bundle", nickname = "bundle delete", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Bundle Deleted"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @DeleteMapping(value = "bundle",
            produces = APPLICATION_JSON_UTF8_VALUE,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public ProductsDeleteResponse deleteBundle(
            @RequestHeader(name = "User-Token", required = false) String token,
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
            produces = APPLICATION_JSON_UTF8_VALUE,
            consumes = APPLICATION_JSON_UTF8_VALUE)
	@ResponseStatus(HttpStatus.OK)
    public void updateBundleElement(
            @RequestHeader(name = "User-Token", required = false) String token,
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
            produces = APPLICATION_JSON_UTF8_VALUE
            ,consumes = APPLICATION_JSON_UTF8_VALUE)
    public VariantUpdateResponse updateProductVariant(@RequestHeader(name = "User-Token", required = false) String token,
                                                      @RequestBody VariantUpdateDTO variant)
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
            produces = APPLICATION_JSON_UTF8_VALUE,
            consumes = MULTIPART_FORM_DATA_VALUE)
    public List<ProductImageUpdateResponse> importProductImagesBulk(
    		@RequestHeader (name = "User-Token", required = false) String userToken,
            @RequestPart("imgs_zip") @Valid MultipartFile zip,
            @RequestPart(name="imgs_barcode_csv", required=false )  MultipartFile csv,
            @RequestPart("properties") @Valid ProductImageBulkUpdateDTO metaData)
            		throws BusinessException {
        if(nonNull(metaData.getFeatureId())){
            SwatchImageBulkUpdateDTO swatchMetaData = new SwatchImageBulkUpdateDTO(metaData);
            productImgService.updateSwatchImagesBulk(zip, csv, swatchMetaData);
            return emptyList();
        }
		return  productImgService.updateProductImageBulk(zip, csv, metaData);
    }
	
	
	
	
	
	@ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Images imported successfully"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
	@ResponseStatus(HttpStatus.OK)
	@PostMapping(value = "image/bulk/url",
            produces = APPLICATION_JSON_UTF8_VALUE,
            consumes = MULTIPART_FORM_DATA_VALUE)
    public List<ProductImageUpdateResponse> importProductImagesBulkViaUrl(
    		@RequestHeader (name = "User-Token", required = false) String userToken,
            @RequestPart(name="imgs_barcode_csv", required=false )  MultipartFile csv,
            @RequestPart("properties") @Valid ProductImageBulkUpdateDTO metaData)
            		throws BusinessException {

		return  productImgService.updateProductImageBulkViaUrl(csv, metaData);
    }

	
	
	

    @ApiOperation(value = "Link list of products to list of tags", nickname = "productsTagsLinking", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Product tag created successfully"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "tag", consumes = APPLICATION_JSON_UTF8_VALUE)
    public void updateProductTags(@RequestHeader(name = "User-Token", required = false) String token,
                                  @RequestBody ProductTagDTO productTagDTO ) throws BusinessException {
        productService.updateProductTags(productTagDTO);
    }

    
    
    
    @ApiOperation(value = "Remove link between list of products to list and tags", nickname = "removeProductsTagsLinking", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Product tag Removed successfully"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @ResponseStatus(OK)
    @DeleteMapping(value = "tag")
    public void deleteProductTags(@RequestHeader(name = "User-Token", required = false) String token,
                                  @RequestParam("products_ids") List<Long> productIds,
                                  @RequestParam("tags_ids") List<Long> tagIds) throws BusinessException {
        productService.deleteProductTags(productIds, tagIds);
    }





    @GetMapping(value = "/image/bulk/template")
    @ResponseBody
    public ResponseEntity<String> generateCsvTemplate(@RequestHeader(name = "User-Token", required = false) String token
    		, @RequestParam(name="type", required = false) ImageCsvTemplateType type) throws IOException {
        ByteArrayOutputStream s = csvDataExportService.generateImagesCsvTemplate(type);
        return ResponseEntity
        		.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(CONTENT_DISPOSITION, "attachment; filename=Csv_Template.csv")
                .body(s.toString());
    }
    
    
    
    
    @ApiOperation(value = "Delete all products", nickname = "deleteAllProducts", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "All organization products deleted successfully"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @DeleteMapping("all")
    public void deleteAllProducts(@RequestHeader(name = "User-Token", required = false) String token
    		, @RequestParam(name = "confirmed",defaultValue = "false", required = true) boolean isConfirmed) throws BusinessException {
    	productService.deleteAllProducts(isConfirmed);
    }


    @ApiOperation(value = "Hide/show products list ", nickname = "hide/showProduct", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Products hidden/shown successfully"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
    })
    @ResponseStatus(OK)
    @PostMapping(value = "hide")
    public void hideProducts(@RequestHeader(name = "User-Token", required = false) String token,
                             @RequestParam(required = false, defaultValue = "true") Boolean hide,
                             @RequestParam(name = "product_id", required = false) List<Long> productsIds) {
        productService.hideProducts(hide, productsIds);
    }


    @ApiOperation(value = "add new collection", nickname = "addCollection", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Collection added successfully"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
    })
    @ResponseStatus(OK)
    @PostMapping(value = "collection",
            produces = APPLICATION_JSON_UTF8_VALUE,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public ProductUpdateResponse addCollection(@RequestHeader(name = "User-Token", required = false) String token,
                                               @RequestBody String productJson ) {
        return productService.updateProduct(productJson, false,  true);
    }


    @ApiOperation(value = "delete empty collection", nickname = "collection delete", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "collection Deleted"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @DeleteMapping(value = "collection")
    public void deleteCollection(@RequestHeader(name = "User-Token", required = false) String token, @RequestParam("id") List<Long> ids) {
        productService.deleteCollection(ids);
    }


    @ApiOperation(value = "Add or delete a collection item", nickname = "addCollectionItem", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "collection item added"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @PostMapping(value = "collection/element", consumes = APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void updateCollection(@RequestHeader(name = "User-Token", required = false) String token,
                                 @RequestBody CollectionItemDTO element) {
        productService.updateCollection(element);
    }


    @ApiOperation(value = "get empty collections by organization id", nickname = "getCollections", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "collections returned"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
    })
    @GetMapping(value = "empty_collections", produces = APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<ProductDetailsDTO> getCollections(@RequestHeader(name = "User-Token", required = false) String token) {
        return productService.getEmptyCollections();
    }


    @ApiOperation(value = "get empty products by organization id", nickname = "getProducts", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "products returned"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
    })
    @GetMapping(value = "empty_products", produces = APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<ProductDetailsDTO> getProducts(@RequestHeader(name = "User-Token", required = false) String token) {
        return productService.getProducts();
    }


    @ApiOperation(value = "Get information about a specific product", nickname = "productInfo")
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Product does not exist")
    })
    @GetMapping(produces=APPLICATION_JSON_VALUE)
    public ProductDetailsDTO getProduct(@RequestHeader(name = "User-Token", required = false) String token,
                                        @RequestParam(name = "product_id") Long productId,
                                        @RequestParam(name = "shop_id",required=false) Long shopId) throws BusinessException {

        return productService.getProduct(productId, shopId, false, true);
    }


    @ApiOperation(value = "Add or delete related items", nickname = "addRelatedItems", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Related items added/removed"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insuffucient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @PostMapping(value = "related_products", consumes = APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void updateRelatedItems(@RequestHeader(name = "User-Token", required = false) String token,
                                   @RequestBody RelatedItemsDTO relatedItems) {
        productService.updateRelatedItems(relatedItems);
    }


    @ApiOperation(value = "Rate a product", nickname = "productRate", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "product rate submitted"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insufficient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @PostMapping(value = "review", consumes = APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void rateProduct(@RequestHeader(name = "User-Token", required = false) String token,
                            @RequestBody ProductRateDTO dto) {
        reviewService.rateProduct(dto);
    }

    @GetMapping(value="/review", produces=MediaType.APPLICATION_JSON_VALUE)
    public List<ProductRateRepresentationObject> getVariantRatings(@RequestHeader(name = "User-Token", required = false) String token) {
        return reviewService.getProductsRatings();
    }

    @ApiOperation(value = "approve a product rating", nickname = "approveProductRate", code = 201)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "product rate approved"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized (invalid User-Token)"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "Insufficient Rights"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid data"),
    })
    @PostMapping(value = "review/approve")
    @ResponseStatus(HttpStatus.OK)
    public void rateProduct(@RequestHeader(name = "User-Token", required = false) String token,
                            @RequestParam Long id) {
        reviewService.approveRate(id);
    }
}
