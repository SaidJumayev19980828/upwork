package com.nasnav.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.ProductImageBulkUpdateDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.ProductImageUpdateResponse;

public interface ImagesBulkService {

	List<ProductImageUpdateResponse> updateImagesBulk(MultipartFile zip, MultipartFile csv,
			ProductImageBulkUpdateDTO metaData) throws BusinessException;

}