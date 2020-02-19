package com.nasnav.service;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.ProductImageBulkUpdateDTO;
import com.nasnav.dto.ProductImageUpdateDTO;
import com.nasnav.dto.ProductImgDetailsDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.ProductImageDeleteResponse;
import com.nasnav.response.ProductImageUpdateResponse;

public interface ProductImageService {
	
	public ProductImageUpdateResponse updateProductImage(MultipartFile file, ProductImageUpdateDTO imgMetaData) throws BusinessException;
	
	public ProductImageDeleteResponse deleteImage(Long imgId) throws BusinessException;
	
	public List<ProductImageUpdateResponse> updateProductImageBulk(
			@Valid MultipartFile zip
			,@Valid MultipartFile csv
			,@Valid ProductImageBulkUpdateDTO metaData) throws BusinessException;

	String getProductCoverImage(Long productId);

	public List<ProductImgDetailsDTO> getProductImgs(Long productId) throws BusinessException;

	Map<Long,String> getProductsCoverImages(List<Long> productIds);

	public List<ProductImageUpdateResponse> updateProductImageBulkViaUrl(MultipartFile csv,
			@Valid ProductImageBulkUpdateDTO metaData)  throws BusinessException;
	
}
