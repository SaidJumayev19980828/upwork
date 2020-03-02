package com.nasnav.service;

import javax.validation.Valid;

import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.enumerations.ImageCsvTemplateType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.ProductListImportResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public interface CsvDataImportService {
	public ProductListImportResponse importProductListFromCSV(
			@Valid MultipartFile file,
			@Valid ProductListImportDTO importMetaData) throws BusinessException ;

	public ByteArrayOutputStream generateProductsCsvTemplate() throws IOException;
	public ByteArrayOutputStream generateImagesCsvTemplate(ImageCsvTemplateType type) throws IOException;
	List<String> getProductImportTemplateHeaders();
}
