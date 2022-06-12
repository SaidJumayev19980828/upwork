package com.nasnav.service;

import com.nasnav.dto.*;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.ProductImagesEntity;
import com.nasnav.response.ProductImageDeleteResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.service.model.ImportedImage;
import com.nasnav.service.model.VariantIdentifier;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ProductImageService {
	
	static final int PRODUCT_IMAGE = 7;
	public String NO_IMG_FOUND_URL = "no_img_found.jpg";

	ProductImageUpdateResponse updateProductImage(MultipartFile file, ProductImageUpdateDTO imgMetaData) throws BusinessException;
	
	ProductImageDeleteResponse deleteImage(Long imgId, Long productId, Long brandId) throws BusinessException;
	
	List<ProductImageUpdateResponse> updateProductImageBulk(
			@Valid MultipartFile zip
			,@Valid MultipartFile csv
			,@Valid ProductImageBulkUpdateDTO metaData) throws BusinessException;

	String getProductCoverImage(Long productId);

	List<ProductImgDetailsDTO> getProductImgs(Long productId) throws BusinessException;

	Map<Long,List<ProductImagesEntity>> getProductsImageList(List<Long> productIds);

	List<ProductImageUpdateResponse> updateProductImageBulkViaUrl(MultipartFile csv,
			@Valid ProductImageBulkUpdateDTO metaData)  throws BusinessException;
	
	List<ProductImageUpdateResponse> saveImgsBulk(Set<ImportedImage> importedImgs) throws BusinessException;
	
	List<ProductImageUpdateResponse> saveImgsBulk(Set<ImportedImage> importedImgs, boolean deleteOldImages) throws BusinessException;

	Flux<ImportedImage> readImgsFromUrls(Map<String, List<VariantIdentifier>> imgToProductsMapping,
			ProductImageBulkUpdateDTO metaData, WebClient client);

	void deleteAllImages(boolean isConfirmed) throws BusinessException;

	List<ProductImageDTO> getProductsAndVariantsImages(List<Long> productsIdList, List<Long> variantsIdList);

	Map<Long,String> getProductsImagesMap(List<Long> productsIdList, List<Long> variantsIdList);

	Map<Long,String> getProductsImagesMap(Map<Long,List<ProductImageDTO>> productImages);

	Map<Long,List<ProductImageDTO>> getProductsAllImagesMap(List<Long> productsIdList, List<Long> variantsIdList);
	
	Map<Long, Optional<String>> getVariantsCoverImages(List<Long> variantIds);

	void saveSwatchImagesBulk(Set<ImportedSwatchImage> importedImgs, SwatchImageBulkUpdateDTO metaData);

	void updateSwatchImagesBulk(@Valid MultipartFile zip
			,@Valid MultipartFile csv
			,@Valid SwatchImageBulkUpdateDTO metaData);
}
