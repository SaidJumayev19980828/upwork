package com.nasnav.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import com.nasnav.enumerations.ImageFileTemplateType;
import com.nasnav.exceptions.BusinessException;

public interface CsvExcelDataExportService {

	ByteArrayOutputStream generateImagesTemplate(ImageFileTemplateType type) throws IOException;
	ByteArrayOutputStream generateProductsFile(Long shopId) throws InvocationTargetException, SQLException, IllegalAccessException, BusinessException, IOException;
	ByteArrayOutputStream generateProductsImagesFile() throws IOException;

}
