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

	Map<String,String> IMG_DATA_TO_COLUMN_MAPPING = MapBuilder.<String, String>map()
			.put("variantId", "variant_id")
			.put("barcode", "barcode")
			.put("productId", "product_id")
			.put("imagePath", "image_path")
			.getMap();


	public ByteArrayOutputStream generateImagesCsvTemplate(ImageCsvTemplateType type) throws IOException;
	ByteArrayOutputStream generateProductsCsv(Long shopId) throws InvocationTargetException, SQLException, IllegalAccessException, BusinessException;
	ByteArrayOutputStream generateProductsImagesCsv();
}
