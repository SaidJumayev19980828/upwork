package com.nasnav.controller;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.nasnav.dto.*;
import com.nasnav.dto.request.product.CollectionItemDTO;
import com.nasnav.dto.request.product.ProductRateDTO;
import com.nasnav.dto.request.product.RelatedItemsDTO;
import com.nasnav.dto.response.navbox.ProductRateRepresentationObject;
import com.nasnav.enumerations.ImageFileTemplateType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.AddonStocksEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.request.BundleSearchParam;
import com.nasnav.service.ProductImageService;
import com.nasnav.service.ProductService;
import com.nasnav.service.ReviewService;
import com.nasnav.service.StockService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.response.AddonStockResponse;
import com.nasnav.response.BundleResponse;
import com.nasnav.response.ProductImageDeleteResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.response.ProductUpdateResponse;
import com.nasnav.response.ProductsDeleteResponse;
import com.nasnav.response.VariantUpdateResponse;
import com.nasnav.service.AddonService;
import com.nasnav.service.CsvExcelDataExportService;
import com.nasnav.service.ImagesBulkService;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductsController {
    private final ImagesBulkService imagesBulkService;
    
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
    private ReviewService reviewService;
    @Autowired
    private StockService stockService;
    
    
    @PostMapping(value = "info", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ProductUpdateResponse updateProduct(@RequestHeader(name = "User-Token", required = false) String token,
                                               @RequestBody String productJson) {
		return productService.updateProduct(productJson, false, false);
    }

    @DeleteMapping(produces = APPLICATION_JSON_VALUE)
    public ProductsDeleteResponse deleteProduct(@RequestHeader(name = "User-Token", required = false) String token,
                                                @RequestParam("product_id") List<Long> productIds,
                                                @RequestParam(value = "force_delete_collection_items", required = false, defaultValue = "false") Boolean forceDeleteCollectionItems){
		return productService.deleteProducts(productIds, forceDeleteCollectionItems);
    }

	@DeleteMapping("variant")
    public void deleteVariants(@RequestHeader(name = "User-Token", required = false) String token,
                               @RequestParam("variant_id") List<Long> variantIds,
                               @RequestParam(value = "force_delete_collection_items", required = false, defaultValue = "false") Boolean forceDelete){
	    productService.deleteVariants(variantIds, forceDelete);
    }

	@PostMapping(value = "image", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
    public ProductImageUpdateResponse updateProductImage(@RequestHeader(name = "User-Token", required = false) String token,
                                                         @RequestPart(value = "image", required = false) @Valid MultipartFile file,
                                                         @RequestPart("properties") @Valid ProductImageUpdateDTO imgMetaData) throws BusinessException {
		return  productImgService.updateProductImage(file, imgMetaData);
    }

	@GetMapping(value = "images", produces = APPLICATION_JSON_VALUE)
    public List<ProductImgDetailsDTO> getProductImages(@RequestHeader (name = "User-Token", required = false) String userToken,
                                                       @RequestParam("product_id") Long productId) throws BusinessException {
        return  productImgService.getProductImgs(productId);
    }

	@DeleteMapping(value = "image", produces = APPLICATION_JSON_VALUE)
    public ProductImageDeleteResponse deleteProductImage(@RequestHeader(name = "User-Token", required = false) String token,
    		                                             @RequestParam(value = "image_id", required = false) @Valid Long imageId,
                                                         @RequestParam(name = "product_id", required = false) Long productId,
                                                         @RequestParam(name = "brand_id", required = false) Long brandId) throws BusinessException {
		return productImgService.deleteImage(imageId, productId, brandId);
    }

	@DeleteMapping(value = "image/all")
    public void deleteAllProductImages(@RequestHeader(name = "User-Token", required = false) String token,
                                       @RequestParam(name = "confirmed",defaultValue = "false") boolean isConfirmed) throws BusinessException {
        productImgService.deleteAllImages(isConfirmed);
    }

    @GetMapping(value = "bundles", produces = APPLICATION_JSON_VALUE)
    public BundleResponse getBundles(BundleSearchParam params) throws BusinessException {
		return productService.getBundles(params);
    }

    @PostMapping(value = "bundle", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ProductUpdateResponse updateBundle(@RequestHeader(name = "User-Token", required = false) String token,
                                              @RequestBody String productJson) {
		return productService.updateProduct(productJson, true, false);
    }

    @DeleteMapping(value = "bundle", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ProductsDeleteResponse deleteBundle(@RequestHeader(name = "User-Token", required = false) String token,
                                               @RequestParam("product_id") Long productId) throws BusinessException {
		return productService.deleteBundle(productId);
    }

    @PostMapping(value = "bundle/element", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public void updateBundleElement(@RequestHeader(name = "User-Token", required = false) String token,
                                    @RequestBody BundleElementUpdateDTO element) throws BusinessException {
		productService.updateBundleElement(element);
    }

	@ResponseStatus(HttpStatus.OK)
	@PostMapping(value = "variant", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public VariantUpdateResponse updateProductVariant(@RequestHeader(name = "User-Token", required = false) String token,
                                                      @RequestBody VariantUpdateDTO variant) throws BusinessException {
		return  productService.updateVariant(variant);
    }

    @DeleteMapping("variant_feature")
    public void deleteAllProducts(@RequestHeader(name = "User-Token", required = false) String token,
                                  @RequestParam(name = "variant_id", required = false) Long variantId,
                                  @RequestParam(name = "feature_id") Integer featureId)  {
        productService.deleteVariantFeatureValue(variantId, featureId);
    }

    @DeleteMapping("variant_extra_attribute")
    public void deleteAllProducts(@RequestHeader(name = "User-Token", required = false) String token,
                                  @RequestParam(name = "variant_id") Long variantId,
                                  @RequestParam(name = "extra_attribute_id") Integer extraAttributeId,
                                  @RequestParam(name = "extra_attribute_value_id", required = false) Long extraAttributeValueId)  {
        productService.deleteVariantExtraAttribute(variantId, extraAttributeId, extraAttributeValueId);
    }

	@PostMapping(value = "image/bulk", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
    public List<ProductImageUpdateResponse> importProductImagesBulk(
            @RequestHeader(name = "User-Token", required = false) String userToken,
            @RequestPart("imgs_zip") @Valid MultipartFile zip,
            @RequestPart(name = "imgs_barcode_csv", required = false) MultipartFile csv,
            @RequestPart("properties") @Valid ProductImageBulkUpdateDTO metaData) throws BusinessException {
        return imagesBulkService.updateImagesBulk(zip, csv, metaData);
    }

	@PostMapping(value = "image/bulk/url", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
    public List<ProductImageUpdateResponse> importProductImagesBulkViaUrl(@RequestHeader (name = "User-Token", required = false) String userToken,
                                                                          @RequestPart(name="imgs_barcode_csv", required=false )  MultipartFile csv,
                                                                          @RequestPart("properties") @Valid ProductImageBulkUpdateDTO metaData) throws BusinessException {
        return productImgService.updateProductImageBulkViaUrl(csv, metaData);
    }

    @PostMapping(value = "tag", consumes = APPLICATION_JSON_VALUE)
    public void updateProductTags(@RequestHeader(name = "User-Token", required = false) String token,
                                  @RequestBody ProductTagDTO productTagDTO ) throws BusinessException {
        productService.updateProductTags(productTagDTO);
    }

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

    @DeleteMapping("all")
    public void deleteAllProducts(@RequestHeader(name = "User-Token", required = false) String token,
                                  @RequestParam(name = "confirmed",defaultValue = "false") boolean isConfirmed) throws BusinessException {
    	productService.deleteAllProducts(isConfirmed);
    }

    @PostMapping(value = "hide")
    public void hideProducts(@RequestHeader(name = "User-Token", required = false) String token,
                             @RequestParam(required = false, defaultValue = "true") Boolean hide,
                             @RequestParam(name = "product_id", required = false) List<Long> productsIds) {
        productService.hideProducts(hide, productsIds);
    }

    @PostMapping(value = "collection", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ProductUpdateResponse addCollection(@RequestHeader(name = "User-Token", required = false) String token,
                                               @RequestBody String productJson ) {
        return productService.updateProduct(productJson, false,  true);
    }

    @DeleteMapping(value = "collection")
    public void deleteCollection(@RequestHeader(name = "User-Token", required = false) String token,
                                 @RequestParam("id") List<Long> ids) {
        productService.deleteCollection(ids);
    }

    @PostMapping(value = "collection/element", consumes = APPLICATION_JSON_VALUE)
    public void updateCollection(@RequestHeader(name = "User-Token", required = false) String token,
                                 @RequestBody CollectionItemDTO element) {
        productService.updateCollection(element);
    }

    @GetMapping(value = "empty_collections", produces = APPLICATION_JSON_VALUE)
    public List<ProductDetailsDTO> getCollections(@RequestHeader(name = "User-Token", required = false) String token) {
        return productService.getEmptyCollections();
    }

    @GetMapping(value = "empty_products", produces = APPLICATION_JSON_VALUE)
    public List<ProductDetailsDTO> getProducts(@RequestHeader(name = "User-Token", required = false) String token) {
        return productService.getEmptyProducts();
    }

    @GetMapping(produces=APPLICATION_JSON_VALUE)
    public ProductDetailsDTO getProduct(@RequestHeader(name = "User-Token", required = false) String token,
            @RequestParam(name = "product_id") Long productId,
            @RequestParam(name = "shop_id", required = false) Long shopId) throws BusinessException {
        return productService.getProduct(productId, shopId, true, false, false);
    }

    @PostMapping(value = "related_products", consumes = APPLICATION_JSON_VALUE)
    public void updateRelatedItems(@RequestHeader(name = "User-Token", required = false) String token,
                                   @RequestBody RelatedItemsDTO relatedItems) {
        productService.updateRelatedItems(relatedItems);
    }

    @PostMapping(value = "review", consumes = APPLICATION_JSON_VALUE)
    public void rateProduct(@RequestHeader(name = "User-Token", required = false) String token,
                            @RequestBody ProductRateDTO dto) {
        reviewService.rateProduct(dto);
    }

    @GetMapping(value="/review", produces=MediaType.APPLICATION_JSON_VALUE)
    public List<ProductRateRepresentationObject> getVariantRatings(@RequestHeader(name = "User-Token", required = false) String token) {
        return reviewService.getProductsRatings();
    }

    @PostMapping(value = "review/approve")
    public void rateProduct(@RequestHeader(name = "User-Token", required = false) String token,
                            @RequestParam Long id) {
        reviewService.approveRate(id);
    }
    
    
    @PostMapping(value = "v2/add", produces = APPLICATION_JSON_VALUE, consumes =MULTIPART_FORM_DATA_VALUE)
    public ProductUpdateResponse createProductV2(@RequestHeader(name = "User-Token", required = false) String token,
    		 @RequestPart("product") @Valid NewProductFlowDTO productJson,  @RequestPart(value = "cover", required = true) @Valid MultipartFile cover, 
    		@RequestPart(value = "imgs", required = false) @Valid MultipartFile [] imgs ) throws BusinessException, JsonMappingException, JsonProcessingException {
		
    	return productService.updateProductV2(productJson,cover,imgs);
    }
   
    
    
	@PostMapping(value = "v2/variant", produces = APPLICATION_JSON_VALUE, consumes =MULTIPART_FORM_DATA_VALUE)
    public VariantUpdateResponse updateProductVariantV2(@RequestHeader(name = "User-Token", required = false) String token,
    		  @RequestPart("var") @Valid VariantUpdateDTO variant, @RequestPart(value = "imgs", required = false) @Valid MultipartFile []imgs) throws BusinessException {
		return  productService.updateVariantV2(variant, imgs);
    }
	
	   @GetMapping(value = "v2/productdata",produces=APPLICATION_JSON_VALUE)
	    public ProductDetailsDTO getProductData(@RequestHeader(name = "User-Token", required = false) String token,
	                                        @RequestParam(name = "product_id") Long productId) throws BusinessException {
	        var params = new ProductFetchDTO(productId);
	       
	        params.setCheckVariants(false);
	        params.setIncludeOutOfStock(true);
	        params.setOnlyYeshteryProducts(false);
	        return productService.getProductData(params);
	    }
	   @PostMapping(value = "v2/stock", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	    public Long updateStock(@RequestHeader(name = "User-Token", required = false) String userToken,
	                                           @RequestBody ProductStocksDTO productStocksDTO) throws BusinessException {
	        return stockService.updateStocks(productStocksDTO);
	    }
	   @GetMapping(value = "v2/stock", produces = APPLICATION_JSON_VALUE)
	    public Map<Long, List<StocksEntity>>getProductStocks(@RequestHeader(name = "User-Token", required = false) String userToken,
	    		  @RequestParam(name = "product_id") Long productId) throws BusinessException {
	        return stockService.getProductStocks(productId);
	    }
	   
   
}
