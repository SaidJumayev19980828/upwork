package com.nasnav.controller;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.validation.Valid;

import com.nasnav.dto.*;
import com.nasnav.dto.request.product.CollectionItemDTO;
import com.nasnav.dto.request.product.ProductRateDTO;
import com.nasnav.dto.request.product.RelatedItemsDTO;
import com.nasnav.dto.response.navbox.ProductRateRepresentationObject;
import com.nasnav.enumerations.ImageCsvTemplateType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.request.BundleSearchParam;
import com.nasnav.response.*;
import com.nasnav.service.CsvDataExportService;
import com.nasnav.service.ProductImageService;
import com.nasnav.service.ProductService;
import com.nasnav.service.ReviewServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.enumerations.ImageFileTemplateType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.request.BundleSearchParam;
import com.nasnav.response.BundleResponse;
import com.nasnav.response.ProductImageDeleteResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.response.ProductUpdateResponse;
import com.nasnav.response.ProductsDeleteResponse;
import com.nasnav.response.VariantUpdateResponse;
import com.nasnav.service.CsvExcelDataExportService;
import com.nasnav.service.ProductImageService;
import com.nasnav.service.ProductService;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.*;

@RestController
@RequestMapping("/product")
@Tag(name = "Products api")
public class ProductsController {
	

	@Autowired
	private ProductService productService;
	
	@Autowired
	private ProductImageService productImgService;

    @Autowired
    @Qualifier("csv")
    private CsvExcelDataExportService csvDataExportService;

    @Autowired
    @Qualifier("excel")
    private CsvExcelDataExportService excelDataExportService;

    @Autowired
    private ReviewServiceImpl reviewService;
	
	@Operation(description =  "Create or update a product", summary = "product update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Product created or updated"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @PostMapping(value = "info",
            produces = APPLICATION_JSON_UTF8_VALUE,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public ProductUpdateResponse updateProduct(@RequestHeader(name = "User-Token", required = false) String token,
                                               @RequestBody String productJson) {
		return productService.updateProduct(productJson, false, false);
    }
	
	
	
	
	@Operation(description =  "deletes list of products", summary = "product delete")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Product Deleted"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @DeleteMapping(
            produces = APPLICATION_JSON_UTF8_VALUE)
    public ProductsDeleteResponse deleteProduct(@RequestHeader(name = "User-Token", required = false) String token,
                                                @RequestParam("product_id") List<Long> productIds,
                                                @RequestParam(value = "force_delete_collection_items", required = false, defaultValue = "false") Boolean forceDeleteCollectionItems){
		return productService.deleteProducts(productIds, forceDeleteCollectionItems);
    }
	
	
	
	
	
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Product image created or updated"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
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
            @ApiResponse(responseCode = " 200" ,description = "Product images fetched"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
	@ResponseStatus(HttpStatus.OK)
	@GetMapping(value = "images",
            produces = APPLICATION_JSON_UTF8_VALUE)
    public List<ProductImgDetailsDTO> getProductImages(
    		@RequestHeader (name = "User-Token", required = false) String userToken, @RequestParam("product_id") Long productId)
            throws BusinessException {

		return  productImgService.getProductImgs(productId);
    }




	
	@Operation(description =  "delete image for product", summary = "product image delete")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Product image deleted"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
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
	
	
	
	

	@Operation(description =  "delete image for product", summary = "product image delete")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Product image deleted"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
	@ResponseStatus(OK)
	@DeleteMapping(value = "image/all")
    public void deleteAllProductImages(@RequestHeader(name = "User-Token", required = false) String token
            , @RequestParam(name = "confirmed",defaultValue = "false", required = true) boolean isConfirmed)
            throws BusinessException {
        productImgService.deleteAllImages(isConfirmed);
    }
	
	
	
	
	
	@Operation(description =  "get bundles", summary = "GetBundles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Bundles fetched"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @GetMapping(value = "bundles", produces = APPLICATION_JSON_UTF8_VALUE)
    public BundleResponse getBundles( BundleSearchParam params)
            		throws BusinessException {
		return productService.getBundles(params);
    }
	
	
	
	
	@Operation(description =  "Create or update a bundle", summary = "bundle update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Bundle created or updated"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @PostMapping(value = "bundle",
            produces = APPLICATION_JSON_UTF8_VALUE,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public ProductUpdateResponse updateBundle(
            @RequestHeader(name = "User-Token", required = false) String token,
            @RequestBody String productJson) {
		return productService.updateProduct(productJson, true, false);
    }
	
	
	
	
	@Operation(description =  "deletes a bundle", summary = "bundle delete")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Bundle Deleted"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
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
	
	
	
	
	@Operation(description =  "Add or delete a bundle item", summary = "bundle item update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Bundle item added or deleted"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
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
	
	
	
	
	
	@Operation(description =  "update product variant", summary = "product variant save")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Product variant saved"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
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
            @ApiResponse(responseCode = " 200" ,description = "Images imported successfully"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
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
            @ApiResponse(responseCode = " 200" ,description = "Images imported successfully"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
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

	
	
	

    @Operation(description =  "Link list of products to list of tags", summary = "productsTagsLinking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Product tag created successfully"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "tag", consumes = APPLICATION_JSON_UTF8_VALUE)
    public void updateProductTags(@RequestHeader(name = "User-Token", required = false) String token,
                                  @RequestBody ProductTagDTO productTagDTO ) throws BusinessException {
        productService.updateProductTags(productTagDTO);
    }

    
    
    
    @Operation(description =  "Remove link between list of products to list and tags", summary = "removeProductsTagsLinking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Product tag Removed successfully"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
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
    		, @RequestParam(name="type", required = false) ImageFileTemplateType type) throws IOException {
        ByteArrayOutputStream s = csvDataExportService.generateImagesTemplate(type);
        return ResponseEntity
        		.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(CONTENT_DISPOSITION, "attachment; filename=Csv_Template.csv")
                .body(s.toString());
    }
    
    
    
    
    @Operation(description =  "Delete all products", summary = "deleteAllProducts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "All organization products deleted successfully"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @DeleteMapping("all")
    public void deleteAllProducts(@RequestHeader(name = "User-Token", required = false) String token
    		, @RequestParam(name = "confirmed",defaultValue = "false", required = true) boolean isConfirmed) throws BusinessException {
    	productService.deleteAllProducts(isConfirmed);
    }


    @Operation(description =  "Hide/show products list ", summary = "hide/showProduct")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Products hidden/shown successfully"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
    })
    @ResponseStatus(OK)
    @PostMapping(value = "hide")
    public void hideProducts(@RequestHeader(name = "User-Token", required = false) String token,
                             @RequestParam(required = false, defaultValue = "true") Boolean hide,
                             @RequestParam(name = "product_id", required = false) List<Long> productsIds) {
        productService.hideProducts(hide, productsIds);
    }


    @Operation(description =  "add new collection", summary = "addCollection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Collection added successfully"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
    })
    @ResponseStatus(OK)
    @PostMapping(value = "collection",
            produces = APPLICATION_JSON_UTF8_VALUE,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public ProductUpdateResponse addCollection(@RequestHeader(name = "User-Token", required = false) String token,
                                               @RequestBody String productJson ) {
        return productService.updateProduct(productJson, false,  true);
    }


    @Operation(description =  "delete empty collection", summary = "collection delete")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "collection Deleted"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @DeleteMapping(value = "collection")
    public void deleteCollection(@RequestHeader(name = "User-Token", required = false) String token, @RequestParam("id") List<Long> ids) {
        productService.deleteCollection(ids);
    }


    @Operation(description =  "Add or delete a collection item", summary = "addCollectionItem")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "collection item added"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @PostMapping(value = "collection/element", consumes = APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void updateCollection(@RequestHeader(name = "User-Token", required = false) String token,
                                 @RequestBody CollectionItemDTO element) {
        productService.updateCollection(element);
    }


    @Operation(description =  "get empty collections by organization id", summary = "getCollections")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "collections returned"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
    })
    @GetMapping(value = "empty_collections", produces = APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<ProductDetailsDTO> getCollections(@RequestHeader(name = "User-Token", required = false) String token) {
        return productService.getEmptyCollections();
    }


    @Operation(description =  "get empty products by organization id", summary = "getProducts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "products returned"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
    })
    @GetMapping(value = "empty_products", produces = APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<ProductDetailsDTO> getProducts(@RequestHeader(name = "User-Token", required = false) String token) {
        return productService.getProducts();
    }


    @Operation(description =  "Get information about a specific product", summary = "productInfo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "OK"),
            @ApiResponse(responseCode = " 404" ,description = "Product does not exist")
    })
    @GetMapping(produces=APPLICATION_JSON_VALUE)
    public ProductDetailsDTO getProduct(@RequestHeader(name = "User-Token", required = false) String token,
                                        @RequestParam(name = "product_id") Long productId,
                                        @RequestParam(name = "shop_id",required=false) Long shopId) throws BusinessException {
        var params = new ProductFetchDTO(productId);
        params.setShopId(shopId);
        params.setCheckVariants(false);
        params.setIncludeOutOfStock(true);
        params.setOnlyYeshteryProducts(false);
        return productService.getProduct(params);
    }


    @Operation(description =  "Add or delete related items", summary = "addRelatedItems")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "Related items added/removed"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insuffucient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @PostMapping(value = "related_products", consumes = APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void updateRelatedItems(@RequestHeader(name = "User-Token", required = false) String token,
                                   @RequestBody RelatedItemsDTO relatedItems) {
        productService.updateRelatedItems(relatedItems);
    }


    @Operation(description =  "Rate a product", summary = "productRate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "product rate submitted"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insufficient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
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

    @Operation(description =  "approve a product rating", summary = "approveProductRate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = " 200" ,description = "product rate approved"),
            @ApiResponse(responseCode = " 401" ,description = "Unauthorized (invalid User-Token)"),
            @ApiResponse(responseCode = " 403" ,description = "Insufficient Rights"),
            @ApiResponse(responseCode = " 406" ,description = "Invalid data"),
    })
    @PostMapping(value = "review/approve")
    @ResponseStatus(HttpStatus.OK)
    public void rateProduct(@RequestHeader(name = "User-Token", required = false) String token,
                            @RequestParam Long id) {
        reviewService.approveRate(id);
    }
}
