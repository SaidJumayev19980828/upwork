package com.nasnav.service;

import static java.util.Arrays.asList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

import javax.validation.Valid;

import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.enumerations.ImageCsvTemplateType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.service.model.importproduct.context.ImportProductContext;

public interface CsvDataImportService {
	String IMG_CSV_HEADER_VARIANT_ID = "variant_id";
	String IMG_CSV_HEADER_EXTERNAL_ID = "external_id";
	String IMG_CSV_HEADER_BARCODE = "barcode";
	String IMG_CSV_HEADER_IMAGE_FILE = "image_file";
	
	List<String> IMG_CSV_BASE_HEADERS = 
			asList(
				IMG_CSV_HEADER_VARIANT_ID
				, IMG_CSV_HEADER_EXTERNAL_ID
				, IMG_CSV_HEADER_BARCODE
				, IMG_CSV_HEADER_IMAGE_FILE);
	
	
	public ImportProductContext importProductListFromCSV(
			@Valid MultipartFile file,
			@Valid ProductListImportDTO importMetaData) throws BusinessException, ImportProductException ;

	public ByteArrayOutputStream generateProductsCsvTemplate() throws IOException;
	public ByteArrayOutputStream generateImagesCsvTemplate(ImageCsvTemplateType type) throws IOException;
	List<String> getProductImportTemplateHeaders();

	public ByteArrayOutputStream generateProductsCsv() throws InvocationTargetException, SQLException, IllegalAccessException, BusinessException;
}
