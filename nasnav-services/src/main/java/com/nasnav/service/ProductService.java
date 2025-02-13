package com.nasnav.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.nasnav.dto.*;
import com.nasnav.dto.request.product.CollectionItemDTO;
import com.nasnav.dto.request.product.RelatedItemsDTO;
import com.nasnav.dto.response.navbox.VariantsResponse;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.request.BundleSearchParam;
import com.nasnav.request.ProductSearchParam;
import com.nasnav.response.BundleResponse;
import com.nasnav.response.ProductUpdateResponse;
import com.nasnav.response.ProductsDeleteResponse;
import com.nasnav.response.VariantUpdateResponse;
import com.nasnav.service.model.ProductTagPair;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProductService {
  ProductDetailsDTO getProduct(Long productId, Long orgId, Long shopId, boolean includeOutOfStock, boolean checkVariants,
                               boolean getOnlyYeshteryProducts) throws BusinessException;

  ProductDetailsDTO getProduct(ProductFetchDTO productFetchDTO);

  List<ProductDetailsDTO> getProducts(List<Long> ids) throws BusinessException;

  Map<String, String> parseVariantFeatures(ProductVariantsEntity variant, Integer returnedName);

  ProductsResponse getProducts(ProductSearchParam requestParams) throws BusinessException;
  List<ProductDetailsDTO> get360Products() throws BusinessException;

  ProductsFiltersResponse getProductAvailableFilters(ProductSearchParam param) throws BusinessException;

  ProductUpdateResponse updateProduct(String productJson, Boolean isBundle, Boolean isCollection);

  List<Long> updateProductBatch(List<String> productJsonList, Boolean isBundle, Boolean isCollection);

  ProductsDeleteResponse deleteProducts(List<Long> productIds, Boolean forceDeleteCollectionItems);

  void deleteVariants(List<Long> variantIds, Boolean forceDeleteCollectionItems);

  ProductsDeleteResponse deleteBundle(Long bundleId) throws BusinessException;

  BundleResponse getBundles(BundleSearchParam params) throws BusinessException;

  void updateBundleElement(BundleElementUpdateDTO element) throws BusinessException;

  VariantUpdateResponse updateVariant(VariantUpdateDTO variant) throws BusinessException;

  List<Long> updateVariantBatch(List<? extends VariantUpdateDTO> variants) throws BusinessException;

  boolean updateProductTags(ProductTagDTO productTagDTO) throws BusinessException;

  void addTagsToProducts(Set<ProductTagPair> newProductTags);

  boolean deleteProductTags(List<Long> productIds, List<Long> tagIds) throws BusinessException;

  void deleteAllProducts(boolean isConfirmed) throws BusinessException;

  void hideProducts(Boolean hide, List<Long> productsIds);

  void deleteAllTagsForProducts(List<Long> products);

  void updateCollection(CollectionItemDTO elements);

  ProductDetailsDTO getCollection(Long id);

  List<ProductDetailsDTO> getEmptyCollections();

  List<ProductDetailsDTO> getEmptyProducts();

  VariantsResponse getVariants(Long orgId, String name, Integer start, Integer count);

  VariantsResponse getVariantsForYeshtery(String name, Integer start, Integer count);

  void deleteVariantFeatureValue(Long variantId, Integer featureId);

  void deleteVariantExtraAttribute(Long variantId, Integer extraAttributeId, Long extraAttributeValueId);

  void updateRelatedItems(RelatedItemsDTO relatedItems);

  List<ProductRepresentationObject> getRelatedProducts(Long productId);

  void deleteCollection(List<Long> ids);

  List<Long> getVariantsWithFeature(ProductFeaturesEntity feature);



  public VariantUpdateResponse updateVariantV2(
          String variant,
          MultipartFile[] imgs,
          Integer[] uploadedImagePriorities,
          List<Map<String, Long>> updatedImages,
          Long[] deletedImages
  ) throws BusinessException, JsonProcessingException;
  ProductDetailsDTO getProductData(ProductFetchDTO params) throws BusinessException;

  ProductsResponse getOutOfStockProducts(ProductSearchParam requestParams) throws BusinessException;

  public ProductUpdateResponse updateProductVersion2(
          NewProductFlowDTO productJson,
          MultipartFile[] imgs,
          Integer[] uploadedImagePriorities,
          List<Map<String, Long>> updatedImages,
          Long[] deletedImages
  ) throws BusinessException, JsonMappingException, JsonProcessingException;


  ProductDetailsDTO toProductDetailsDTO(ProductEntity product, boolean b);
}