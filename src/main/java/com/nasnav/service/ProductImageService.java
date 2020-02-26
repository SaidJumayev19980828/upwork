package com.nasnav.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.ProductImageBulkUpdateDTO;
import com.nasnav.dto.ProductImageUpdateDTO;
import com.nasnav.dto.ProductImgDetailsDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.ProductImageDeleteResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.service.model.ImportedImage;

public interface ProductImageService {
	
	static final int PRODUCT_IMAGE = 7;

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
	
}
