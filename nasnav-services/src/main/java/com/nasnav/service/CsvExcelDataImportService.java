package com.nasnav.service;

import static java.util.Arrays.asList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import javax.validation.Valid;

import com.google.common.collect.ImmutableMap;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.commons.utils.MapBuilder;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.service.model.importproduct.context.ImportProductContext;

public interface CsvExcelDataImportService {
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

	Map<String,String> PRODUCT_DATA_TO_COLUMN_MAPPING = MapBuilder.<String, String>map()
			.put("name", "product_name")
			.put("barcode", "barcode")
			.put("tags", "tags")
			.put("brand", "brand")
			.put("price", "price")
			.put("quantity", "quantity")
			.put("description", "description")
			.put("variantId", "variant_id")
			.put("externalId", "external_id")
			.put("productGroupKey", "product_group_key")
			.put("discount", "discount")
			.put("sku", "sku")
			.put("productCode", "product_code")
			.put("unit", "unit")
			.put("weight", "weight")
			.getMap();

	public static final List<String> ORDER_DATA_COLUMN = List.of(
			"orderId",
			"userName",
			"shopName",
			"shipping",
			"shippingService",
			"paymentOperator",
			"subtotal",
			"total",
			"currency",
			"notes",
			"status",
			"totalQuantity",
			"paymentStatus",
			"shippingStatus",
			"discount",
			"creationDate",
			"flatNumber",
			"buildingNumber",
			"phoneNumber",
			"area",
			"city",
			"country",
			"subArea",
			"addressLine1",
			"addressLine2"
	);

	Map<String,String> ORDER_DATA_TO_COLUMN_MAPPING = ImmutableMap.<String, String>builder()
			.put("orderId", "orderId")
			.put("userName", "userName")
			.put("shopName", "shopName")
			.put("shipping", "shipping")
			.put("shippingService", "shippingService")
			.put("paymentOperator", "paymentOperator")
			.put("subtotal", "subtotal")
			.put("total", "total")
			.put("currency", "currency")
			.put("notes", "notes")
			.put("status", "status")
			.put("totalQuantity", "totalQuantity")
			.put("paymentStatus", "paymentStatus")
			.put("shippingStatus", "shippingStatus")
			.put("discount", "discount")
			.put("creationDate", "creationDate")
			.put("flatNumber", "flatNumber")
			.put("buildingNumber", "buildingNumber")
			.put("phoneNumber", "phoneNumber")
			.put("area", "area")
			.put("city", "city")
			.put("country", "country")
			.put("subArea", "subArea")
			.put("addressLine1", "addressLine1")
			.put("addressLine2", "addressLine2")
		.build();

	Map<String,String> IMG_DATA_TO_COLUMN_MAPPING = ImmutableMap.<String, String>builder()
			.put("variantId", "variant_id")
			.put("externalId", "external_id")
			.put("barcode", "barcode")
			.put("productName", "product_name")
			.put("productId", "product_id")
			.put("imagePath", "image_path")
			.build();

	Map<String, String> PRODUCT_DATA_SPECIAL_MAPPING = MapBuilder.<String, String>map()
			.put("variant_id","variantId")
			.put("product_name","name")
			.put("external_id","externalId")
			.put("product_group_key","productGroupKey")
			.put("product_code","productCode")
			.getMap();
	Set<String> CSV_BASE_HEADERS = new HashSet<String>(PRODUCT_DATA_TO_COLUMN_MAPPING.values());

	ImportProductContext importProductList(
			@Valid MultipartFile file,
			@Valid ProductListImportDTO importMetaData) throws BusinessException, ImportProductException ;

	ByteArrayOutputStream generateProductsTemplate(Boolean addExcelDataValidation) throws IOException;

	List<String> getProductImportTemplateHeaders();

	List<String> getProductImportTemplateHeadersWithoutExtraAttributes();

	boolean isFileSupported(MultipartFile file);
}
