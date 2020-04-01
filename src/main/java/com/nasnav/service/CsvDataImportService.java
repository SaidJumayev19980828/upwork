package com.nasnav.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.validation.Valid;

import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.enumerations.ImageCsvTemplateType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.service.model.ImportProductContext;

public interface CsvDataImportService {
	public ImportProductContext importProductListFromCSV(
			@Valid MultipartFile file,
			@Valid ProductListImportDTO importMetaData) throws BusinessException, ImportProductException ;

	public ByteArrayOutputStream generateProductsCsvTemplate() throws IOException;
	public ByteArrayOutputStream generateImagesCsvTemplate(ImageCsvTemplateType type) throws IOException;
	List<String> getProductImportTemplateHeaders();
}
