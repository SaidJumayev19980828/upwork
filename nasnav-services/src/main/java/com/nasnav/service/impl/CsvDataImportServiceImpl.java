package com.nasnav.service.impl;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.commons.utils.FilesUtils;
import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.service.AbstractCsvExcelDataImportService;
import com.nasnav.service.helpers.ProductCsvRowProcessor;
import com.nasnav.service.model.importproduct.context.ImportProductContext;
import com.nasnav.service.model.importproduct.csv.CsvRow;
import com.univocity.parsers.common.DataProcessingException;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.RowProcessorErrorHandler;
import com.univocity.parsers.common.fields.ColumnMapping;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import lombok.Data;

import javax.validation.Valid;

@Service
@Qualifier("csv")
public class CsvDataImportServiceImpl extends AbstractCsvExcelDataImportService {

	private Logger logger = Logger.getLogger(getClass());

	@Override
	@Transactional
	public ImportProductContext importProductList(@Valid MultipartFile file,
												  @Valid ProductListImportDTO importMetaData) throws BusinessException, ImportProductException {
		validateProductImportMetaData(importMetaData);
		validateProductImporFile(file);

		ProductImportMetadata importMetadata = getImportMetaData(importMetaData);
		ImportProductContext initialContext = new ImportProductContext(emptyList(), importMetadata);
		
		List<CsvRow> rows = parseCsvFile(file, importMetaData, initialContext);
		
		List<ProductImportDTO> productsData = 
				rows
				.stream()
				.map(CsvRow::toProductImportDto)
				.collect(toList());

		ImportProductContext importResult = dataImportService.importProducts(productsData, importMetadata);
		if (importResult != null && importResult.isSuccess()) {
			return importResult;
		} else {
			throw new ImportProductException(importResult);
		}
	}



	private List<CsvRow> parseCsvFile(MultipartFile file, ProductListImportDTO metaData, ImportProductContext context) throws ImportProductException {
		List<ProductFeaturesEntity> orgFeatures = featureRepo.findByShopId( metaData.getShopId() );
		
		ByteArrayInputStream in = readCsvFile(file, context);		
		BeanListProcessor<CsvRow> rowProcessor = createRowProcessor(metaData, orgFeatures);
		RowParseErrorHandler rowParsingErrHandler = new RowParseErrorHandler(context);
		CsvParserSettings settings = createParsingSettings(rowProcessor, rowParsingErrHandler);
		settings.setLineSeparatorDetectionEnabled(true);
		
		CsvParser parser = new CsvParser(settings);
		
		runCsvParser(in, rowParsingErrHandler, parser, metaData.getEncoding());
		
		return rowProcessor.getBeans();
	}

	private void runCsvParser(ByteArrayInputStream in, RowParseErrorHandler rowParsingErrHandler, CsvParser parser, String encoding)
			throws ImportProductException {
		ImportProductContext context = rowParsingErrHandler.getImportContext();
		try {
			parser.parse(in, encoding);
		}catch(Exception e) {
			logger.error(e,e);
			throw new ImportProductException(e, context); 
		}		
		
		if(!context.isSuccess()) {
			throw new ImportProductException(context);
		}
	}

	private CsvParserSettings createParsingSettings(BeanListProcessor<CsvRow> rowProcessor,
			RowParseErrorHandler rowParsingErrHandler) {
		CsvParserSettings settings = new CsvParserSettings();
		settings.setLineSeparatorDetectionEnabled(true);
		settings.setHeaderExtractionEnabled(true);
		settings.setProcessor(rowProcessor);		
		settings.setProcessorErrorHandler(rowParsingErrHandler);
		settings.setMaxCharsPerColumn(-1);
		return settings;
	}

	private BeanListProcessor<CsvRow> createRowProcessor(ProductListImportDTO metaData, List<ProductFeaturesEntity> orgFeatures) {
		List<String> defaultTemplateHeaders = getProductImportTemplateHeadersWithoutExtraAttributes();
		
		ColumnMapping mapper = createAttrToColMapping(metaData);		
		
		BeanListProcessor<CsvRow> rowProcessor =
				new ProductCsvRowProcessor<CsvRow>(CsvRow.class, orgFeatures, defaultTemplateHeaders);
		rowProcessor.setColumnMapper(mapper);
		rowProcessor.setStrictHeaderValidationEnabled(true);
		return rowProcessor;
	}

	private ByteArrayInputStream readCsvFile(MultipartFile file, ImportProductContext context) throws ImportProductException {
		byte[] bytes = new byte[0];
		try {
			bytes = file.getBytes();
		} catch (IOException e) {
			logger.error(e,e);
			context.logNewError(e, "csvFile", 0);
			throw new ImportProductException(e, context);
		}
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		return in;
	}

	private ColumnMapping createAttrToColMapping(ProductListImportDTO metaData) {
		ColumnMapping mapping = new ColumnMapping();
		mapping.attributesToColumnNames(PRODUCT_DATA_TO_COLUMN_MAPPING);
		
		return mapping;
	}


	 protected ByteArrayOutputStream writeFileHeaders(List<String> headers, Boolean addExcelDataValidation) throws IOException {
		ByteArrayOutputStream csvResult = new ByteArrayOutputStream();
		Writer outputWriter = new OutputStreamWriter(csvResult);

		CsvWriter writer = new CsvWriter(outputWriter, createWritingSettings());

		writer.writeHeaders(headers);
		writer.close();
		csvResult.close();

		return csvResult;
	}

	private CsvWriterSettings createWritingSettings() {
		CsvWriterSettings settings = new CsvWriterSettings();
		return settings;
	}

	@Override
	public boolean isFileSupported(MultipartFile file) {
		return FilesUtils.isCsv(file);
	}
}

@Data
class RowParseErrorHandler implements RowProcessorErrorHandler {
	private ImportProductContext importContext;

	public RowParseErrorHandler(ImportProductContext context) {
		this.importContext = context;
	}

	@Override
	public void handleError(DataProcessingException error, Object[] inputRow, ParsingContext context) {
			importContext.logNewError(error, Arrays.toString(inputRow), (int)(context.currentLine())+1);	
	}
}
