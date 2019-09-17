package com.nasnav.service;

import javax.validation.Valid;

import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.ProductListImportResponse;

public interface DataImportService {
	public ProductListImportResponse importProductListFromCSV(
			@Valid MultipartFile file,
			@Valid ProductListImportDTO importMetaData) throws BusinessException ;
}
