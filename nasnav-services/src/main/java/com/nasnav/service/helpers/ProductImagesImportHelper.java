package com.nasnav.service.helpers;

import com.nasnav.dto.*;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.service.model.*;
import com.sun.istack.logging.Logger;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import lombok.RequiredArgsConstructor;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import javax.validation.Valid;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.nasnav.commons.utils.EntityUtils.firstExistingValueOf;
import static com.nasnav.constatnts.EntityConstants.Operation.CREATE;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.*;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.service.CsvExcelDataImportService.*;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.logging.Level.SEVERE;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.*;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductImagesImportHelper {
	public static final int PRODUCT_IMAGE = 7;

	private Logger logger = Logger.getLogger(ProductImagesImportHelper.class);

	private final CachingHelper cachingHelper;

	public List<ImportedImage> extractImgsToImport(@Valid MultipartFile zip, @Valid MultipartFile csv,
			@Valid ProductImageBulkUpdateDTO metaData) {
		List<ImportedImage> imgs = new ArrayList<>();
		List<String> errors = new ArrayList<>();
		Map<String, List<VariantIdentifier>> fileIdentifiersMap = createFileToVariantIdsMap(csv);

		try (ZipInputStream stream = new ZipInputStream(zip.getInputStream())) {
			imgs = readZipStream(stream, metaData, fileIdentifiersMap, errors);
		} catch (Exception e) {
			logger.log(SEVERE, e.getMessage(), e);
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, P$IMG$0005);
		}

		if (!errors.isEmpty()) {
			String errorsJson = getErrorMsgAsJson(errors);
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, P$IMG$0007, errorsJson);
		}

		return imgs;
	}

	private String getErrorMsgAsJson(List<String> errors) {
		JSONArray errorsJson = new JSONArray(errors);

		JSONObject main = new JSONObject();
		main.put("msg", ERR_CSV_PARSE_FAILURE);
		main.put("errors", errorsJson);

		return errorsJson.toString();
	}

	private List<ImportedImage> readZipStream(ZipInputStream stream, ProductImageBulkUpdateDTO metaData,
			Map<String, List<VariantIdentifier>> fileIdentifiersMap, List<String> errors) throws IOException {

		List<ImportedImage> imgs = new ArrayList<>();

		ZipEntry zipEntry = stream.getNextEntry();

		while (zipEntry != null) {
			readImgsFromZipEntry(zipEntry, stream, metaData, fileIdentifiersMap, errors)
					.forEach(imgs::add);

			zipEntry = stream.getNextEntry();
		}

		stream.closeEntry();

		return imgs;
	}

	/**
	 * read a single image in the zip file, and return one or more "ImportedImage"
	 * based on the barcode,
	 * as a single barcode can belong to both a product and a product variant.
	 */
	private List<ImportedImage> readImgsFromZipEntry(ZipEntry zipEntry, ZipInputStream stream,
			ProductImageBulkUpdateDTO metaData, Map<String, List<VariantIdentifier>> fileIdentifiersMap,
			List<String> errors) {
		List<ImportedImage> imgsFromEntry = new ArrayList<>();

		if (zipEntry.isDirectory()) {
			return new ArrayList<>();
		}

		try {
			MultipartFile imgMultipartFile = readZipEntryAsMultipartFile(stream, zipEntry);
			List<ProductImageUpdateDTO> imgsMetaData = createImportedImagesMetaData(zipEntry, fileIdentifiersMap, metaData);

			imgsFromEntry = imgsMetaData
					.stream()
					.map(meta -> new ImportedImage(imgMultipartFile, meta, zipEntry.getName()))
					.collect(toList());

		} catch (Exception e) {
			logger.log(SEVERE, e.getMessage(), e);
			errors.add(createZipEntryErrorMsg(zipEntry, e));
		}
		return imgsFromEntry;
	}

	private String createZipEntryErrorMsg(ZipEntry zipEntry, Exception e) {
		return String.format(ERR_IMPORTING_IMG_FILE, zipEntry.getName(), e.getMessage());
	}

	private MultipartFile readZipEntryAsMultipartFile(ZipInputStream stream, ZipEntry zipEntry) throws IOException {
		String fileName = zipEntry.getName();
		FileItem fileItem = createFileItem(fileName);
		readIntoFileItem(stream, fileItem);

		return new CommonsMultipartFile(fileItem);
	}

	private void readIntoFileItem(ZipInputStream stream, FileItem fileItem) throws IOException {
		byte[] buffer = new byte[1024 * 100];
		OutputStream fos = fileItem.getOutputStream();
		int len;
		while ((len = stream.read(buffer)) > 0) {
			fos.write(buffer, 0, len);
		}
	}

	public FileItem createFileItem(String fileName) {
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(1024 * 1024);
		return factory.createItem("image", "image/jpeg", false, fileName);
	}

	/**
	 * a single imported image is identified by barcode, which can be for a product
	 * record ,or a
	 * product variant record.
	 * So, we return metadata considering all cases:
	 * - barcode exists for product only
	 * - barcode exists for variant only
	 * - barcode exists for both a product and a variant
	 */
	private List<ProductImageUpdateDTO> createImportedImagesMetaData(ZipEntry zipEntry,
			Map<String, List<VariantIdentifier>> fileIdentifiersMap,
			ProductImageBulkUpdateDTO metaData) {

		List<VariantIdentifier> identifiers = getVariantIdentifiersForCompressedFile(zipEntry, fileIdentifiersMap);
		VariantCache cache = cachingHelper.createVariantCache(identifiers);

		List<List<ProductImageUpdateDTO>> metaDataLists = new ArrayList<>();
		for (VariantIdentifier identifier : identifiers) {
			List<ProductImageUpdateDTO> imgsMetaData = createImportedImagesMetaData(metaData, identifier, cache);
			metaDataLists.add(imgsMetaData);
		}

		return metaDataLists
				.stream()
				.flatMap(List::stream)
				.collect(toList());
	}

	private List<ProductImageUpdateDTO> createImportedImagesMetaData(ProductImageBulkUpdateDTO metaData,
			VariantIdentifier identifier, VariantCache cache) {
		Optional<VariantBasicData> variant = getProductVariant(identifier, cache, metaData);

		return variant
				.map(v -> createImgMetaData(metaData, v))
				.map(Arrays::asList)
				.orElse(emptyList());
	}

	public ProductImageUpdateDTO createImgMetaData(ProductImageBulkUpdateDTO metaData, VariantBasicData variant) {
		Long variantId = variant.getVariantId();
		Long productId = variant.getProductId();

		ProductImageUpdateDTO imgMetaData = new ProductImageUpdateDTO();
		imgMetaData.setOperation(CREATE);
		imgMetaData.setPriority(metaData.getPriority());
		imgMetaData.setType(metaData.getType());
		imgMetaData.setProductId(productId);

		if (!Objects.equals(metaData.getType(), PRODUCT_IMAGE)) {
			imgMetaData.setVariantId(variantId);
		}

		return imgMetaData;
	}

	public Optional<VariantBasicData> getProductVariant(VariantIdentifier identifier, VariantCache cache,
			ProductImageBulkUpdateDTO metaData) {
		Optional<VariantBasicData> variant = getVariantFromCache(identifier, cache);

		Boolean isIgnoreErrors = ofNullable(metaData.isIgnoreErrors()).orElse(false);
		if (!isIgnoreErrors.booleanValue() && !variant.isPresent()) {
			throw new RuntimeBusinessException(
					format(ERR_NO_VARIANT_FOUND, identifier.getVariantId(), identifier.getExternalId(), identifier.getBarcode()),
					"INVALID PARAM: csv", NOT_ACCEPTABLE);
		}
		return variant;
	}

	private Optional<VariantBasicData> getVariantFromCache(VariantIdentifier identifier, VariantCache cache) {
		String variantId = identifier.getVariantId();
		String externalId = identifier.getExternalId();
		String barcode = identifier.getBarcode();
		return firstExistingValueOf(
				cache.getIdToVariantMap().get(variantId), cache.getExternalIdToVariantMap().get(externalId),
				cache.getBarcodeToVariantMap().get(barcode));
	}

	private List<VariantIdentifier> getVariantIdentifiersForCompressedFile(ZipEntry zipEntry,
			Map<String, List<VariantIdentifier>> fileToIdentifiersMap) {
		String fileName = zipEntry.getName();
		List<VariantIdentifier> identifiers = fileToIdentifiersMap.get(fileName);

		if (identifiers == null) {
			String barcode = getBarcodeFromImgName(fileName);
			identifiers = new ArrayList<>();
			identifiers.add(new VariantIdentifier(barcode));
		}
		return identifiers;
	}

	public Map<String, List<VariantIdentifier>> createFileToVariantIdsMap(MultipartFile csv) {
		if (csv == null || csv.isEmpty())
			return new HashMap<>();

		List<Record> csvRecords = getCsvRecords(csv);
		Map<String, List<VariantIdentifier>> identifiersMap = new HashMap<>();
		String path;
		VariantIdentifier identifier;
		for (Record row : csvRecords) {
			path = normalizeZipPath(row.getString(IMG_CSV_HEADER_IMAGE_FILE));
			identifier = new VariantIdentifier(
					row.getString(IMG_CSV_HEADER_VARIANT_ID), row.getString(IMG_CSV_HEADER_EXTERNAL_ID),
					row.getString(IMG_CSV_HEADER_BARCODE));
			identifiersMap.computeIfAbsent(path, key -> new ArrayList<>()).add(identifier);
		}
		return identifiersMap;
	}

	private List<Record> getCsvRecords(MultipartFile csv) {
		List<Record> allRecords = new ArrayList<>();

		CsvParserSettings settings = new CsvParserSettings();
		settings.setLineSeparatorDetectionEnabled(true);
		settings.setHeaderExtractionEnabled(true);
		CsvParser parser = new CsvParser(settings);

		try {
			allRecords = parser.parseAllRecords(csv.getInputStream());
		} catch (Exception e) {
			logger.log(SEVERE, e.getMessage(), e);
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, P$IMG$0006);
		}

		return allRecords;
	}

	private String getBarcodeFromImgName(String fileName) {
		return com.google.common.io.Files.getNameWithoutExtension(fileName);
	}

	private String normalizeZipPath(String path) {
		return ofNullable(path)
				.map(p -> p.startsWith("/") ? p.replaceFirst("/", "") : p)
				.orElse("");
	}
}
