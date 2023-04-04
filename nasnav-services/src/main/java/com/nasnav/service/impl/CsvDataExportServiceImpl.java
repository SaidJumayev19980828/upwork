package com.nasnav.service.impl;

import static com.nasnav.service.CsvExcelDataImportService.IMG_DATA_TO_COLUMN_MAPPING;
import static com.nasnav.service.CsvExcelDataImportService.PRODUCT_DATA_TO_COLUMN_MAPPING;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.nasnav.dto.ProductImageDTO;
import com.nasnav.dto.VariantWithNoImagesDTO;
import com.nasnav.service.AbstractCsvExcelDataExportService;
import com.nasnav.service.helpers.ProductCsvRowWriterProcessor;
import com.nasnav.service.model.importproduct.csv.CsvRow;
import com.univocity.parsers.common.fields.ColumnMapping;
import com.univocity.parsers.common.processor.BeanWriterProcessor;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("csv")
public class CsvDataExportServiceImpl extends AbstractCsvExcelDataExportService{

	protected ByteArrayOutputStream buildProductsFile(List<String> headers,
			   List<CsvRow> products) {
		BeanWriterProcessor<CsvRow> processor = createProductsRowProcessor();
		CsvWriterSettings settings = createWritterSettings(processor);

		return writeFileResult(headers, settings, products);
	}

	protected ByteArrayOutputStream buildImagesFile(List<String> headers,
												   List<ProductImageDTO> images) {
		BeanWriterProcessor<ProductImageDTO> processor = createProductImgsRowProcessor();
		CsvWriterSettings settings = createWritterSettings(processor);

		return writeFileResult(headers, settings, images);
	}

	private BeanWriterProcessor<CsvRow> createProductsRowProcessor() {
		ColumnMapping mapper = createCsvAttrToColMapping(PRODUCT_DATA_TO_COLUMN_MAPPING);

		BeanWriterProcessor<CsvRow> rowProcessor = new ProductCsvRowWriterProcessor(CsvRow.class);
		rowProcessor.setColumnMapper(mapper);
		rowProcessor.setStrictHeaderValidationEnabled(true);
		return rowProcessor;
	}

	private BeanWriterProcessor<ProductImageDTO> createProductImgsRowProcessor() {

		Map<String, String> imgDataToColumnMapping = new HashMap<>(IMG_DATA_TO_COLUMN_MAPPING);

		removeSpecialColumns(imgDataToColumnMapping);

		ColumnMapping mapper = createCsvAttrToColMapping(imgDataToColumnMapping);

		BeanWriterProcessor<ProductImageDTO> rowProcessor = new BeanWriterProcessor<>(ProductImageDTO.class);
		rowProcessor.setColumnMapper(mapper);
		rowProcessor.setStrictHeaderValidationEnabled(true);
		return rowProcessor;
	}

	private CsvWriterSettings createWritterSettings(BeanWriterProcessor<?> rowProcessor) {
		CsvWriterSettings settings = new CsvWriterSettings();
		settings.setRowWriterProcessor(rowProcessor);		
		settings.setMaxCharsPerColumn(-1);
		settings.setQuoteAllFields(true);
		return settings;
	}

	private ColumnMapping createCsvAttrToColMapping(Map<String,String> fields) {
		ColumnMapping mapping = new ColumnMapping();
		mapping.attributesToColumnNames(fields);
		return mapping;
	}

	protected ByteArrayOutputStream writeFileResult(List<String> headers, CsvWriterSettings settings, List<?> data) {
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

	@Override
	protected ByteArrayOutputStream buildProductWithNoImgsFile(List<String> headers,
															   List<VariantWithNoImagesDTO> variants) {
		BeanWriterProcessor<VariantWithNoImagesDTO> processor = createImgsTemplateRowProcessor();
		CsvWriterSettings settings = createWritterSettings(processor);

		return writeFileResult(headers, settings, variants);
	}

	private CsvWriterSettings createWritingSettings() {
		CsvWriterSettings settings = new CsvWriterSettings();
		return settings;
	}

	@Override
	protected ByteArrayOutputStream writeFileHeaders(List<String> headers) throws IOException {
		ByteArrayOutputStream csvResult = new ByteArrayOutputStream();
		Writer outputWriter = new OutputStreamWriter(csvResult);

		CsvWriter writer = new CsvWriter(outputWriter, createWritingSettings());

		writer.writeHeaders(headers);
		writer.close();
		csvResult.close();

		return csvResult;
	}
}
