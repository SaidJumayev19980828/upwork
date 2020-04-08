package com.nasnav.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.nasnav.dto.ProductImageBulkUpdateDTO;
import com.nasnav.dto.ProductImageUpdateDTO;
import com.nasnav.dto.ProductImageUpdateIdentifier;
import com.nasnav.dto.ProductImgDetailsDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.ProductImageDeleteResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.service.model.ImportedImage;

import reactor.core.publisher.Flux;

public interface ProductImageService {
	
	static final int PRODUCT_IMAGE = 7;
	public String NO_IMG_FOUND_URL = "no_img_found.jpg";

	ProductImageUpdateResponse updateProductImage(MultipartFile file, ProductImageUpdateDTO imgMetaData) throws BusinessException;
	
	ProductImageDeleteResponse deleteImage(Long imgId) throws BusinessException;
	
	List<ProductImageUpdateResponse> updateProductImageBulk(
			@Valid MultipartFile zip
			,@Valid MultipartFile csv
			,@Valid ProductImageBulkUpdateDTO metaData) throws BusinessException;

	String getProductCoverImage(Long productId);

	List<ProductImgDetailsDTO> getProductImgs(Long productId) throws BusinessException;

	Map<Long,String> getProductsCoverImages(List<Long> productIds);

	List<ProductImageUpdateResponse> updateProductImageBulkViaUrl(MultipartFile csv,
			@Valid ProductImageBulkUpdateDTO metaData)  throws BusinessException;
	
	List<ProductImageUpdateResponse> saveImgsBulk(Set<ImportedImage> importedImgs) throws BusinessException;
	
	List<ProductImageUpdateResponse> saveImgsBulk(Set<ImportedImage> importedImgs, boolean deleteOldImages) throws BusinessException;

	Flux<ImportedImage> readImgsFromUrls(Map<String, List<ProductImageUpdateIdentifier>> fileIdentifiersMap,
			ProductImageBulkUpdateDTO metaData, WebClient client);
	
}
