package com.nasnav.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Map;

import com.nasnav.commons.utils.MapBuilder;
import com.nasnav.enumerations.ImageCsvTemplateType;
import com.nasnav.exceptions.BusinessException;

public interface CsvDataExportService {

	public ByteArrayOutputStream generateImagesCsvTemplate(ImageCsvTemplateType type) throws IOException;
	ByteArrayOutputStream generateProductsCsv(Long shopId) throws InvocationTargetException, SQLException, IllegalAccessException, BusinessException;
	ByteArrayOutputStream generateProductsImagesCsv();

}
