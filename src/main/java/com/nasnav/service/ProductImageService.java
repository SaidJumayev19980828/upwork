package com.nasnav.service;

import java.util.List;

import javax.validation.Valid;

import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.ProductImageBulkUpdateDTO;
import com.nasnav.dto.ProductImageUpdateDTO;
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
	
}
