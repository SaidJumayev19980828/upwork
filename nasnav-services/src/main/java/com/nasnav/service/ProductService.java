package com.nasnav.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.BundleElementUpdateDTO;
import com.nasnav.dto.ProductDetailsDTO;
import com.nasnav.dto.ProductFetchDTO;
import com.nasnav.dto.ProductImageUpdateDTO;
import com.nasnav.dto.ProductRepresentationObject;
import com.nasnav.dto.ProductTagDTO;
import com.nasnav.dto.ProductsFiltersResponse;
import com.nasnav.dto.ProductsResponse;
import com.nasnav.dto.VariantUpdateDTO;
import com.nasnav.dto.request.product.CollectionItemDTO;
import com.nasnav.dto.request.product.RelatedItemsDTO;
import com.nasnav.dto.response.navbox.VariantsResponse;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.request.BundleSearchParam;
import com.nasnav.request.ProductSearchParam;
import com.nasnav.response.BundleResponse;
import com.nasnav.response.ProductImageDeleteResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.response.ProductUpdateResponse;
import com.nasnav.response.ProductsDeleteResponse;
import com.nasnav.response.VariantUpdateResponse;
import com.nasnav.service.model.ProductTagPair;

public interface ProductService {

  ProductDetailsDTO getProduct(Long productId, Long shopId, boolean includeOutOfStock, boolean checkVariants,
      boolean getOnlyYeshteryProducts) throws BusinessException;

  ProductDetailsDTO getProduct(ProductFetchDTO productFetchDTO) throws BusinessException;

  Map<String, String> parseVariantFeatures(ProductVariantsEntity variant, Integer returnedName);

  ProductsResponse getProducts(ProductSearchParam requestParams) throws BusinessException;

  ProductsFiltersResponse getProductAvailableFilters(ProductSearchParam param) throws BusinessException;

  ProductUpdateResponse updateProduct(String productJson, Boolean isBundle, Boolean isCollection);

  List<Long> updateProductBatch(List<String> productJsonList, Boolean isBundle, Boolean isCollection);

  ProductsDeleteResponse deleteProducts(List<Long> productIds, Boolean forceDeleteCollectionItems);

  void deleteVariants(List<Long> variantIds, Boolean forceDeleteCollectionItems);

  ProductsDeleteResponse deleteBundle(Long bundleId) throws BusinessException;

  ProductImageUpdateResponse updateProductImage(MultipartFile file, ProductImageUpdateDTO imgMetaData)
      throws BusinessException;

  ProductImageDeleteResponse deleteImage(Long imgId) throws BusinessException;

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

}