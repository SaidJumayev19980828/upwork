package com.nasnav.service;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_INVALID_ENCODING;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_NO_FILE_UPLOADED;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_PRODUCT_IMPORT_MISSING_PARAM;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_SHOP_ID_NOT_EXIST;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_USER_CANNOT_CHANGE_OTHER_ORG_SHOP;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.persistence.ShopsEntity;
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
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import lombok.Data;

@Service
public class CsvDataImportServiceImpl implements CsvDataImportService {

	
	@Autowired
	private ShopsRepository shopRepo;
	
	@Autowired
	private EmployeeUserRepository empRepo;
	
	@Autowired
	private SecurityService security;

	
	@Autowired
	private ProductFeaturesRepository featureRepo;

	@Autowired
	private ProductFeaturesRepository prodcutFeaturesRepo;

	@Autowired
	private DataImportService dataImportService;
	
	
	
	
	
	private Logger logger = Logger.getLogger(getClass());

	
	
	
	@Transactional(rollbackFor = Throwable.class)
	public ImportProductContext importProductListFromCSV(@Valid MultipartFile file,
			@Valid ProductListImportDTO csvImportMetaData) throws BusinessException, ImportProductException {
		
		validateProductImportMetaData(csvImportMetaData);
		validateProductImportCsvFile(file);

		ProductImportMetadata importMetadata = getImportMetaData(csvImportMetaData);
		ImportProductContext initialContext = new ImportProductContext(emptyList(), importMetadata);
		
		List<CsvRow> rows = parseCsvFile(file, csvImportMetaData, initialContext);
		
		List<ProductImportDTO> productsData = 
				rows
				.stream()
				.map(CsvRow::toProductImportDto)
				.collect(toList());
		return dataImportService.importProducts(productsData, importMetadata);
	}
	
	
	

	private ProductImportMetadata getImportMetaData(ProductListImportDTO csvImportMetaData) {
		ProductImportMetadata importMetadata = new ProductImportMetadata();

		importMetadata.setDryrun(csvImportMetaData.isDryrun());
		importMetadata.setUpdateProduct(csvImportMetaData.isUpdateProduct());
		importMetadata.setUpdateStocks(csvImportMetaData.isUpdateStocks());
		importMetadata.setShopId(csvImportMetaData.getShopId());
		importMetadata.setCurrency(csvImportMetaData.getCurrency());
		importMetadata.setEncoding(csvImportMetaData.getEncoding());
		importMetadata.setDeleteOldProducts(csvImportMetaData.isDeleteOldProducts());
		importMetadata.setResetTags(csvImportMetaData.isResetTags());

		return importMetadata;
	}
	
	
	
	

	private List<CsvRow> parseCsvFile(MultipartFile file, ProductListImportDTO metaData, ImportProductContext context) throws ImportProductException {
		List<ProductFeaturesEntity> orgFeatures = featureRepo.findByShopId( metaData.getShopId() );
		
		ByteArrayInputStream in = readCsvFile(file, context);		
		BeanListProcessor<CsvRow> rowProcessor = createRowProcessor(metaData, orgFeatures);
		RowParseErrorHandler rowParsingErrHandler = new RowParseErrorHandler(context);
		CsvParserSettings settings = createParsingSettings(rowProcessor, rowParsingErrHandler);
		
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
		settings.setHeaderExtractionEnabled(true);
		settings.setProcessor(rowProcessor);		
		settings.setProcessorErrorHandler(rowParsingErrHandler);
		settings.setMaxCharsPerColumn(-1);
		return settings;
	}



	private BeanListProcessor<CsvRow> createRowProcessor(ProductListImportDTO metaData, List<ProductFeaturesEntity> orgFeatures) {
		List<String> defaultTemplateHeaders = getProductImportTemplateHeaders();
		
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





	private void validateProductImportCsvFile(@Valid MultipartFile file) throws BusinessException{
		if(file == null || file.isEmpty()) {
			throw new BusinessException(
					ERR_NO_FILE_UPLOADED
					, "INVALID PARAM"
					, HttpStatus.NOT_ACCEPTABLE); 
		}
		
	}





	private void validateProductImportMetaData(@Valid ProductListImportDTO metaData) throws BusinessException{
		Long shopId = metaData.getShopId();
		String encoding = metaData.getEncoding();
		Integer currency = metaData.getCurrency();
		
		if( anyIsNull(shopId, encoding, currency)) {
			throw new BusinessException(
					ERR_PRODUCT_IMPORT_MISSING_PARAM
					, "MISSING PARAM"
					, HttpStatus.NOT_ACCEPTABLE);
		}		
		
		validateShopId(shopId);		
		
		validateEncodingCharset(encoding);		
		
		validateStockCurrency(currency);		
	}





	private void validateEncodingCharset(String encoding) throws BusinessException {
		try {
			if( !Charset.isSupported(encoding)) {
				throw new IllegalStateException();
			}			
		}catch(Exception e) {
			logger.error(e,e);
			throw new BusinessException(
					String.format(ERR_INVALID_ENCODING, encoding)
					, "MISSING PARAM:encoding"
					, HttpStatus.NOT_ACCEPTABLE);
		}
	}





	private void validateShopId(Long shopId) throws BusinessException {
		if( !shopRepo.existsById( shopId ) ) {
			throw new BusinessException(
					String.format(ERR_SHOP_ID_NOT_EXIST, shopId)
					, "MISSING PARAM:shop_id"
					, HttpStatus.NOT_ACCEPTABLE);
		}
		
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		EmployeeUserEntity user =  empRepo.getOneByEmail(auth.getName());
		Long userOrgId = user.getOrganizationId();

		ShopsEntity shop = shopRepo.findById(shopId).get();
		Long shopOrgId = shop.getOrganizationEntity().getId();
		
		if(!Objects.equals(shopOrgId, userOrgId)) {
			throw new BusinessException(
					String.format(ERR_USER_CANNOT_CHANGE_OTHER_ORG_SHOP, userOrgId, shopOrgId)
					, "MISSING PARAM:shop_id"
					, HttpStatus.NOT_ACCEPTABLE);
		}
	}
	
	
	
	
	
	private void validateStockCurrency(Integer currency) throws BusinessException {
		boolean invalidCurrency = Arrays.asList( TransactionCurrency.values() )
										.stream()
										.map(TransactionCurrency::getValue)
										.map(Integer::valueOf)
										.noneMatch(val -> Objects.equals(currency, val));
		if(invalidCurrency ) {
			throw new BusinessException(
					String.format("Invalid Currency code [%d]!", currency)
					, "INVALID_PARAM:currency" 
					, HttpStatus.NOT_ACCEPTABLE);
		}
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
	
	

	@Override
	public ByteArrayOutputStream generateProductsCsvTemplate() throws IOException{		
		List<String> baseHeaders = getProductImportTemplateHeaders();

		return writeCsvHeaders(baseHeaders);
	}




	@Override
	public List<String> getProductImportTemplateHeaders() {
		Long orgId = security.getCurrentUserOrganizationId();
		List<String> features = 
				prodcutFeaturesRepo
					.findByOrganizationId(orgId)
					.stream()
					.map(ProductFeaturesEntity::getName)
					.sorted()
					.collect(toList());
		
		List<String> baseHeaders = new ArrayList<>(CSV_BASE_HEADERS);
		baseHeaders.addAll(features);
		return baseHeaders;
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
