package com.nasnav.service;

import static com.nasnav.enumerations.ImageCsvTemplateType.EMPTY;
import static com.nasnav.enumerations.ImageCsvTemplateType.PRODUCTS_WITH_NO_IMGS;
import static com.nasnav.service.CsvDataImportService.IMG_CSV_BASE_HEADERS;
import static com.nasnav.service.CsvDataImportService.IMG_DATA_TO_COLUMN_MAPPING;
import static com.nasnav.service.CsvDataImportService.PRODUCT_DATA_TO_COLUMN_MAPPING;
import static java.util.Optional.ofNullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nasnav.dao.ProductImgsCustomRepository;
import com.nasnav.dto.VariantWithNoImagesDTO;
import com.nasnav.enumerations.ImageCsvTemplateType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.helpers.ProductCsvRowWriterProcessor;
import com.nasnav.service.model.importproduct.csv.CsvRow;
import com.univocity.parsers.common.fields.ColumnMapping;
import com.univocity.parsers.common.processor.BeanWriterProcessor;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

@Service
public class CsvDataExportServiceImpl implements CsvDataExportService {
	
	@Autowired
	private SecurityService security;
	
	@Autowired
	private DataExportService exportService;
	
	@Autowired
	private CsvDataImportService importService;
	
	@Autowired
	private ProductImgsCustomRepository productImgsCustomRepo;
	
	
	
	@Override
	public ByteArrayOutputStream generateImagesCsvTemplate(ImageCsvTemplateType type) throws IOException{
		ImageCsvTemplateType templateType = ofNullable(type).orElse(EMPTY);
		if(templateType.equals(PRODUCTS_WITH_NO_IMGS)) {
			return generateImagesCsvTemplateForProductsWithNoImgs();
		}			
		else {
			return generateEmptyImagesCsvTemplate();
		}
	}
	
	

	@Override
	public ByteArrayOutputStream generateProductsCsv(Long shopId) throws InvocationTargetException, SQLException, IllegalAccessException, BusinessException {
		Long orgId = security.getCurrentUserOrganizationId();

		List<String> headers = importService.getProductImportTemplateHeaders();

		List<CsvRow> products = exportService.exportProductsData(orgId, shopId);

		return buildProductsCsv(headers, products);
	}
	
	
	
	
	private ByteArrayOutputStream buildProductsCsv(List<String> headers,
			   List<CsvRow> products) {
		BeanWriterProcessor<CsvRow> processor = createProductsRowProcessor();
		CsvWriterSettings settings = createWritterSettings(processor);
		
		return writeCsvResult(headers, settings, products);
	}
	
	
	
	private BeanWriterProcessor<CsvRow> createProductsRowProcessor() {
		ColumnMapping mapper = createCsvAttrToColMapping(PRODUCT_DATA_TO_COLUMN_MAPPING);

		BeanWriterProcessor<CsvRow> rowProcessor = new ProductCsvRowWriterProcessor(CsvRow.class);
		rowProcessor.setColumnMapper(mapper);
		rowProcessor.setStrictHeaderValidationEnabled(true);
		return rowProcessor;
	}
	
	
	
	
	private CsvWriterSettings createWritterSettings(BeanWriterProcessor<?> rowProcessor) {
		CsvWriterSettings settings = new CsvWriterSettings();
		settings.setRowWriterProcessor(rowProcessor);		
		settings.setMaxCharsPerColumn(-1);
		return settings;
	}
	
	
	
	private ColumnMapping createCsvAttrToColMapping(Map<String,String> fields) {
		ColumnMapping mapping = new ColumnMapping();
		mapping.attributesToColumnNames(fields);
		return mapping;
	}

	
	
	
	private ByteArrayOutputStream writeCsvResult(List<String> headers, CsvWriterSettings settings, List<?> data) {
		ByteArrayOutputStream csvOutStream = new ByteArrayOutputStream();
		Writer outputWriter = new OutputStreamWriter(csvOutStream);
		CsvWriter writer = new CsvWriter(outputWriter, settings);

		writer.writeHeaders(headers.stream().toArray(String[]::new));
		writer.processRecordsAndClose(data);

		return csvOutStream;
	}
	
	
	
	
	
	private BeanWriterProcessor<VariantWithNoImagesDTO> createImgsTemplateRowProcessor() {
		
		ColumnMapping mapper = createCsvAttrToColMapping(IMG_DATA_TO_COLUMN_MAPPING);
		
		BeanWriterProcessor<VariantWithNoImagesDTO> rowProcessor = new BeanWriterProcessor<>(VariantWithNoImagesDTO.class);
		rowProcessor.setColumnMapper(mapper);
		rowProcessor.setStrictHeaderValidationEnabled(true);
		return rowProcessor;
	}
	
	
	
	
	private ByteArrayOutputStream generateImagesCsvTemplateForProductsWithNoImgs() {
		List<String> headers = new ArrayList<>();
		headers.addAll(IMG_CSV_BASE_HEADERS);
		headers.add("product_name");
		headers.add("product_id");
		
		Long orgId = security.getCurrentUserOrganizationId();
		List<VariantWithNoImagesDTO> variants = productImgsCustomRepo.getProductsWithNoImages(orgId);
		
		return buildProductWithNoImgsCsv(headers, variants);
	}




	private ByteArrayOutputStream buildProductWithNoImgsCsv(List<String> headers,
			List<VariantWithNoImagesDTO> variants) {
		BeanWriterProcessor<VariantWithNoImagesDTO> processor = createImgsTemplateRowProcessor();
		CsvWriterSettings settings = createWritterSettings(processor);

		return writeCsvResult(headers, settings, variants);
	}







	private ByteArrayOutputStream generateEmptyImagesCsvTemplate() throws IOException {
		return writeCsvHeaders(IMG_CSV_BASE_HEADERS);
	}
	
	
	private CsvWriterSettings createWritingSettings() {
		CsvWriterSettings settings = new CsvWriterSettings();
		return settings;
	}
	
	
	

	private ByteArrayOutputStream writeCsvHeaders(List<String> headers) throws IOException {
		ByteArrayOutputStream csvResult = new ByteArrayOutputStream();
		Writer outputWriter = new OutputStreamWriter(csvResult);

		CsvWriter writer = new CsvWriter(outputWriter, createWritingSettings());

		writer.writeHeaders(headers);
		writer.close();
		csvResult.close();

		return csvResult;
	}
}
